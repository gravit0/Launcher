package launcher.request.uuid;

import java.io.IOException;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.profiles.PlayerProfile;
import launcher.helper.IOHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.request.RequestType;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;

public final class BatchProfileByUsernameRequest extends Request<PlayerProfile[]> {
    private final String[] usernames;

    @LauncherAPI
    public BatchProfileByUsernameRequest(LauncherConfig config, String... usernames) throws IOException {
        super(config);
        this.usernames = usernames.clone();
        IOHelper.verifyLength(this.usernames.length, SerializeLimits.MAX_BATCH_SIZE);
        for (String username : this.usernames) {
            VerifyHelper.verifyUsername(username);
        }
    }

    @LauncherAPI
    public BatchProfileByUsernameRequest(String... usernames) throws IOException {
        this(null, usernames);
    }

    @Override
    public Integer getType() {
        return RequestType.BATCH_PROFILE_BY_USERNAME.getNumber();
    }

    @Override
    protected PlayerProfile[] requestDo(HInput input, HOutput output) throws IOException {
        output.writeLength(usernames.length, SerializeLimits.MAX_BATCH_SIZE);
        for (String username : usernames) {
            output.writeString(username, SerializeLimits.MAX_LOGIN);
            output.writeString("", SerializeLimits.MAX_CLIENT); //TODO: Что это за запрос и для чего он нужен?
        }
        output.flush();

        // Read profiles response
        PlayerProfile[] profiles = new PlayerProfile[usernames.length];
        for (int i = 0; i < profiles.length; i++) {
            profiles[i] = input.readBoolean() ? new PlayerProfile(input) : null;
        }

        // Return result
        return profiles;
    }
}
