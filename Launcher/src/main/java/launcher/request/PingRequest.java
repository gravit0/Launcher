package launcher.request;

import java.io.IOException;

import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.SerializeLimits;

public final class PingRequest extends Request<Void> {

    @LauncherAPI
    public PingRequest(LauncherConfig config) {
        super(config);
    }

    @LauncherAPI
    public PingRequest() {
        this(null);
    }

    @Override
    public Integer getType() {
        return RequestType.PING.getNumber();
    }

    @Override
    protected Void requestDo(HInput input, HOutput output) throws IOException {
        byte pong = (byte) input.readUnsignedByte();
        if (pong != SerializeLimits.EXPECTED_BYTE) {
            throw new IOException("Illegal ping response: " + pong);
        }
        return null;
    }
}
