package launchserver.response.update;

import java.io.IOException;
import java.util.Collection;

import launcher.helper.LogHelper;
import launcher.profiles.ClientProfile;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.signed.SignedObjectHolder;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class ProfilesResponse extends Response {

    public ProfilesResponse(LaunchServer server, long session, HInput input, HOutput output, String ip) {
        super(server, session, input, output, ip);
    }

    @Override
    public void reply() throws IOException {
        // Resolve launcher binary
        input.readBoolean();
        writeNoError(output);
        Collection<SignedObjectHolder<ClientProfile>> profiles = server.getProfiles();
        output.writeLength(profiles.size(), 0);
        for (SignedObjectHolder<ClientProfile> profile : profiles) {
            LogHelper.debug("Writted profile: %s",profile.object.getTitle());
            profile.write(output);
        }
    }
}
