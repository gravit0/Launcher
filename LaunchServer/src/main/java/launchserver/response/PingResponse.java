package launchserver.response;

import java.io.IOException;

import launcher.request.PingRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;

public final class PingResponse extends Response {
    public PingResponse(LaunchServer server, long id, HInput input, HOutput output,String ip) {
        super(server, id, input, output,ip);
    }

    @Override
    public void reply() throws IOException {
        output.writeUnsignedByte(PingRequest.EXPECTED_BYTE);
    }
}
