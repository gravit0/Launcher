package launcher.request.auth;

import java.io.IOException;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.client.ClientLauncher;
import launcher.profiles.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.request.auth.JoinServerRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;

public final class CheckServerRequest extends Request<PlayerProfile> {
    private final String username;
    private final String serverID;

    @LauncherAPI
    public CheckServerRequest(LauncherConfig config, String username, String serverID) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.serverID = JoinServerRequest.verifyServerID(serverID);
    }

    @LauncherAPI
    public CheckServerRequest(String username, String serverID) {
        this(null, username, serverID);
    }

    @Override
    public Type getType() {
        return Type.CHECK_SERVER;
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeString(username, SerializeLimits.MAX_LOGIN);
        output.writeASCII(serverID, SerializeLimits.MAX_SERVERID); // 1 char for minus sign
        output.writeString(ClientLauncher.profile.getTitle(), 64);
        output.flush();

        // Read response
        readError(input);
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
