package launcher.request.update;

import java.io.IOException;
import java.nio.file.Path;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import launcher.Launcher;
import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.client.ClientProfile;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.request.Request;
import launcher.request.update.LauncherRequest.Result;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

public final class LauncherRequest extends Request<Result> {
    @LauncherAPI public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
    @LauncherAPI public static final boolean EXE_BINARY = IOHelper.hasExtension(BINARY_PATH, "exe");

    @LauncherAPI
    public LauncherRequest(LauncherConfig config) {
        super(config);
    }

    @LauncherAPI
    public LauncherRequest() {
        this(null);
    }

    @Override
    public Type getType() {
        return Type.LAUNCHER;
    }

    @Override
    public int getSize() {
        return Long.BYTES;
    }

    @Override
    @SuppressWarnings("CallToSystemExit")
    protected Result requestDo(HInput input, HOutput output) throws Exception {
        output.writeLong(System.currentTimeMillis());
        output.flush();
        readError(input);

        boolean shouldUpdate = input.readBoolean();
        // Update launcher if need
        output.writeBoolean(shouldUpdate);
        output.flush();
        if (shouldUpdate) {
            byte[] binary = input.readByteArray(0);
            return new Result(binary, Collections.emptyList());
        }

        // Read clients profiles list
        int count = input.readLength(0);
        List<ClientProfile> profiles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            profiles.add(new ClientProfile(input, true));
        }

        // Return request result
        return new Result(null, profiles);
    }

    @LauncherAPI
    public static void update(LauncherConfig config, Result result) throws SignatureException, IOException {

        // Prepare process builder to start new instance (java -jar works for Launch4J's EXE too)
        List<String> args = new ArrayList<>(8);
        args.add(IOHelper.resolveJavaBin(null).toString());
        if (LogHelper.isDebugEnabled()) {
            args.add(Launcher.jvmProperty(LogHelper.DEBUG_PROPERTY, Boolean.toString(LogHelper.isDebugEnabled())));
        }
        if (LauncherConfig.ADDRESS_OVERRIDE != null) {
            args.add(Launcher.jvmProperty(LauncherConfig.ADDRESS_OVERRIDE_PROPERTY, LauncherConfig.ADDRESS_OVERRIDE));
        }
        args.add("-jar");
        args.add(BINARY_PATH.toString());
        ProcessBuilder builder = new ProcessBuilder(args.toArray(new String[0]));
        builder.inheritIO();

        // Rewrite and start new instance
        IOHelper.write(BINARY_PATH, result.binary);
        builder.start();

        // Kill current instance
        JVMHelper.RUNTIME.exit(255);
        throw new AssertionError("Why Launcher wasn't restarted?!");
    }

    public static final class Result {
        @LauncherAPI public final List<ClientProfile> profiles;
        private final byte[] binary;

        private Result(byte[] binary, List<ClientProfile> profiles) {
            this.binary = binary == null ? null : binary.clone();
            for(ClientProfile p : profiles)
            {
                System.out.println(p.getServerAddress());
            }
            this.profiles = Collections.unmodifiableList(profiles);
        }

        @LauncherAPI
        public byte[] getBinary() {
            return binary == null ? null : binary.clone();
        }
    }
}
