package launchserver.response.profile;

import java.io.IOException;
import java.util.Arrays;

import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class BatchProfileByUsernameResponse extends Response {

    public BatchProfileByUsernameResponse(LaunchServer server, long session, HInput input, HOutput output, String ip) {
        super(server, session, input, output, ip);
    }

    @Override
    public void reply() throws IOException {
        int length = input.readLength(SerializeLimits.MAX_BATCH_SIZE);
        String[] usernames = new String[length];
        String[] clients = new String[length];
        for (int i = 0; i < usernames.length; i++) {
            usernames[i] = VerifyHelper.verifyUsername(input.readString(64));
            clients[i] = input.readString(SerializeLimits.MAX_CLIENT);
        }
        debug("Usernames: " + Arrays.toString(usernames));

        // Respond with profiles array
        for (int i = 0; i < usernames.length; i++)
			ProfileByUsernameResponse.writeProfile(server, output, usernames[i], clients[i]);
    }
}
