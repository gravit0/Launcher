package launcher.request;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import launcher.Launcher;
import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.SecurityHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public abstract class Request<R> {
    @LauncherAPI
    protected final LauncherConfig config;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private static final long session = SecurityHelper.secureRandom.nextLong();

    @LauncherAPI
    protected Request(LauncherConfig config) {
        this.config = config == null ? Launcher.getConfig() : config;
    }

    @LauncherAPI
    protected Request() {
        this(null);
    }

    @LauncherAPI
    public abstract Integer getType();

    @LauncherAPI
    protected abstract R requestDo(HInput input, HOutput output) throws Exception;

    @LauncherAPI
    @SuppressWarnings("DesignForExtension")
    public R request() throws Exception {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Request already started");
        }

        // Make request to LaunchServer
        try (Socket socket = IOHelper.newSocket()) {
            socket.connect(IOHelper.resolve(config.address));
            try (HInput input = new HInput(socket.getInputStream());
                 HOutput output = new HOutput(socket.getOutputStream())) {
                writeHandshake(input, output);
                return requestDo(input, output);
            }
        }
    }

    @LauncherAPI
    protected final void readError(HInput input) throws IOException {
        String error = input.readString(0);
        if (!error.isEmpty()) {
            requestError(error);
        }
    }

    private void writeHandshake(HInput input, HOutput output) throws IOException {
        // Write handshake
        output.writeInt(Launcher.PROTOCOL_MAGIC);
        output.writeBigInteger(config.publicKey.getModulus(), SecurityHelper.RSA_KEY_LENGTH + 1);
        output.writeLong(session);
        output.writeVarInt(getType());
        output.flush();

        // Verify is accepted
        if (!input.readBoolean()) {
            requestError("Serverside not accepted this connection");
        }
    }

    @LauncherAPI
    public static void requestError(String message) throws RequestException {
        throw new RequestException(message);
    }

}
