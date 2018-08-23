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
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.EnumSerializer.Itf;

public abstract class Request<R> {
    @LauncherAPI protected final LauncherConfig config;
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
    public enum Type implements Itf {
        PING(0), // Ping request
        LAUNCHER(1), UPDATE(2), UPDATE_LIST(3), // Update requests
        AUTH(4), JOIN_SERVER(5), CHECK_SERVER(6), // Auth requests
        PROFILE_BY_USERNAME(7), PROFILE_BY_UUID(8), BATCH_PROFILE_BY_USERNAME(9), // Profile requests
        CUSTOM(255); // Custom requests
        private static final EnumSerializer<Type> SERIALIZER = new EnumSerializer<>(Type.class);
        private final int n;

        Type(int n) {
            this.n = n;
        }

        @Override
        public int getNumber() {
            return n;
        }

        @LauncherAPI
        public static Type read(HInput input) throws IOException {
            return SERIALIZER.read(input);
        }
    }
}
