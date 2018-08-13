package launchserver.response.update;

import java.util.Map.Entry;
import java.util.Set;

import launcher.hasher.HashedDir;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response;

public final class UpdateListResponse extends Response {
    public UpdateListResponse(LaunchServer server, long id, HInput input, HOutput output) {
        super(server, id, input, output);
    }

    @Override
    public void reply() throws Exception {
        Set<Entry<String, HashedDir>> updateDirs = server.getUpdateDirs();

        // Write all update dirs names
        output.writeLength(updateDirs.size(), 0);
        for (Entry<String, HashedDir> entry : updateDirs) {
            output.writeString(entry.getKey(), 255);
        }
    }
}
