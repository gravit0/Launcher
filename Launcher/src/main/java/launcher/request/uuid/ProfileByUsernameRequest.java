package launcher.request.uuid;

import java.io.IOException;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.client.ClientLauncher;
import launcher.profiles.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.request.RequestType;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;

public final class ProfileByUsernameRequest extends Request<PlayerProfile> {
    private final String username;

    @LauncherAPI
    public ProfileByUsernameRequest(LauncherConfig config, String username) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
    }

    @LauncherAPI
    public ProfileByUsernameRequest(String username) {
        this(null, username);
    }

    @Override
    public Integer getType() {
        return RequestType.PROFILE_BY_USERNAME.getNumber();
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeString(username, SerializeLimits.MAX_LOGIN);
        output.writeString(ClientLauncher.title, SerializeLimits.MAX_CLIENT);
        output.flush();
        // Return profile
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
