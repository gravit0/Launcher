package launchserver.response.auth;

import java.io.IOException;
import java.util.UUID;

import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;
import launchserver.LaunchServer;
import launchserver.auth.AuthException;
import launchserver.response.Response;
import launchserver.response.profile.ProfileByUUIDResponse;

public final class CheckServerResponse extends Response {

    public CheckServerResponse(LaunchServer server, long session, HInput input, HOutput output, String ip) {
        super(server, session, input, output, ip);
    }

    @Override
    public void reply() throws IOException {
        String username = VerifyHelper.verifyUsername(input.readString(SerializeLimits.MAX_LOGIN));
        String serverID = VerifyHelper.verifyServerID(input.readASCII(41)); // With minus sign
        String client = input.readString(SerializeLimits.MAX_CLIENT);
        debug("Username: %s, Server ID: %s", username, serverID);

        // Try check server with auth handler
        UUID uuid;
        try {
            uuid = server.config.authHandler.checkServer(username, serverID);
        } catch (AuthException e) {
            requestError(e.getMessage());
            return;
        } catch (Exception e) {
            LogHelper.error(e);
            requestError("Internal auth handler error");
            return;
        }
        writeNoError(output);

        // Write profile and UUID
        output.writeBoolean(uuid != null);
        if (uuid != null) {
            ProfileByUUIDResponse.getProfile(server, uuid, username, client).write(output);
        }
    }
}
