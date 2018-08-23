package launchserver.response.auth;

import java.io.IOException;

import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.auth.JoinServerRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;
import launchserver.LaunchServer;
import launchserver.auth.AuthException;
import launchserver.response.Response;

public final class JoinServerResponse extends Response {

    public JoinServerResponse(LaunchServer server, long session, HInput input, HOutput output, String ip) {
        super(server, session, input, output, ip);
    }

    @Override
    public void reply() throws IOException {
        String username = VerifyHelper.verifyUsername(input.readString(SerializeLimits.MAX_LOGIN));
        String accessToken = SecurityHelper.verifyToken(input.readASCII(-SecurityHelper.TOKEN_STRING_LENGTH));
        String serverID = JoinServerRequest.verifyServerID(input.readASCII(SerializeLimits.MAX_SERVERID)); // With minus sign

        // Try join server with auth handler
        debug("Username: '%s', Access token: %s, Server ID: %s", username, accessToken, serverID);
        boolean success;
        try {
            success = server.config.authHandler.joinServer(username, accessToken, serverID);
        } catch (AuthException e) {
            requestError(e.getMessage());
            return;
        } catch (Exception e) {
            LogHelper.error(e);
            requestError("Internal auth handler error");
            return;
        }
        writeNoError(output);

        // Write response
        output.writeBoolean(success);
    }
}
