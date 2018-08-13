package launchserver.response.update;

import java.io.IOException;
import java.util.Collection;

import launcher.client.ClientProfile;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class LauncherResponse extends Response {
    public LauncherResponse(LaunchServer server, long id, HInput input, HOutput output) {
        super(server, id, input, output);
    }

    @Override
    public void reply() throws IOException {
        // Resolve launcher binary
        byte[] bytes = (input.readBoolean() ? server.launcherEXEBinary : server.launcherBinary).getBytes();
        if (bytes == null) {
            requestError("Missing launcher binary");
            return;
        }
        writeNoError(output);

        // Update launcher binary
        output.flush();
        if (input.readBoolean()) {
            output.writeByteArray(bytes, 0);
            return; // Launcher will be restarted
        }

        // Write clients profiles list
        Collection<ClientProfile> profiles = server.getProfiles();
        output.writeLength(profiles.size(), 0);
        for (ClientProfile profile : profiles) {
            profile.write(output);
        }
    }
}
