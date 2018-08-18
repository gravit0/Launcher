package launchserver.response.profile;

import java.io.IOException;
import java.util.UUID;

import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class ProfileByUsernameResponse extends Response {
    public ProfileByUsernameResponse(LaunchServer server, long id, HInput input, HOutput output) {
        super(server, id, input, output);
    }

    @Override
    public void reply() throws IOException {
        String username = VerifyHelper.verifyUsername(input.readString(64));
        debug("Username: " + username);
        String client = input.readString(SerializeLimits.MAX_CLIENT);
        // Write response
        writeProfile(server, output, username,client);
    }

    public static void writeProfile(LaunchServer server, HOutput output, String username,String client) throws IOException {
        UUID uuid = server.config.authHandler.usernameToUUID(username);
        if (uuid == null) {
            output.writeBoolean(false);
            return;
        }

        // Write profile
        output.writeBoolean(true);
        ProfileByUUIDResponse.getProfile(server, uuid, username, client).write(output);
    }
}
