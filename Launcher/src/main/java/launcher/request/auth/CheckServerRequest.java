package launcher.request.auth;

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

public final class CheckServerRequest extends Request<PlayerProfile> {
    private final String username;
    private final String serverID;

    @LauncherAPI
    public CheckServerRequest(LauncherConfig config, String username, String serverID) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }

    @LauncherAPI
    public CheckServerRequest(String username, String serverID) {
        this(null, username, serverID);
    }

    @Override
    public Integer getType() {
        return RequestType.CHECK_SERVER.getNumber();
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException {
        output.writeString(username, SerializeLimits.MAX_LOGIN);
        output.writeASCII(serverID, SerializeLimits.MAX_SERVERID); // 1 char for minus sign
        output.writeString(ClientLauncher.title, 64);
        output.flush();

        // Read response
        readError(input);
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
