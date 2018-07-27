package launcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import launcher.helper.*;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.StreamObject;

public final class Launcher {
    private static final AtomicReference<Config> CONFIG = new AtomicReference<>();

    // Version info
    @LauncherAPI public static final String VERSION = "15.4";
    @LauncherAPI public static final String BUILD = readBuildNumber();
    @LauncherAPI public static final int PROTOCOL_MAGIC = 0x724724_00 + 23;

    // Constants
    @LauncherAPI public static final String RUNTIME_DIR = "runtime";
    @LauncherAPI public static final String CONFIG_FILE = "config.bin";
    @LauncherAPI public static final String INIT_SCRIPT_FILE = "init.js";

    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);


    @LauncherAPI
    public static String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }
    @LauncherAPI
    public static String jvmProperty(String name, String value) {
        return String.format("-D%s=%s", name, value);
    }
    @LauncherAPI
    public static Config getConfig() {
        Config config = CONFIG.get();
        if (config == null) {
            try (HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL(CONFIG_FILE)))) {
                config = new Config(input);
            } catch (IOException | InvalidKeySpecException e) {
                throw new SecurityException(e);
            }
            CONFIG.set(config);
        }
        return config;
    }

    @LauncherAPI
    public static URL getResourceURL(String name) throws IOException {
        Config config = getConfig();
        byte[] validDigest = config.runtime.get(name);
        if (validDigest == null) { // No such resource digest
            throw new NoSuchFileException(name);
        }

        // Resolve URL and verify digest
        URL url = IOHelper.getResourceURL(RUNTIME_DIR + '/' + name);
        if (!Arrays.equals(validDigest, SecurityHelper.digest(SecurityHelper.DigestAlgorithm.MD5, url))) {
            throw new NoSuchFileException(name); // Digest mismatch
        }

        // Return verified URL
        return url;
    }

    @LauncherAPI
    @SuppressWarnings({ "SameReturnValue", "MethodReturnAlwaysConstant" })
    public static String getVersion() {
        return VERSION; // Because Java constants are known at compile-time
    }

    private static String readBuildNumber() {
        try {
            return IOHelper.request(IOHelper.getResourceURL("buildnumber"));
        } catch (IOException ignored) {
            return "dev"; // Maybe dev env?
        }
    }

    public static final class Config extends StreamObject {
        @LauncherAPI public static final String ADDRESS_OVERRIDE_PROPERTY = "launcher.addressOverride";
        @LauncherAPI public static final String ADDRESS_OVERRIDE = System.getProperty(ADDRESS_OVERRIDE_PROPERTY, null);

        // Instance
        @LauncherAPI public final InetSocketAddress address;
        @LauncherAPI public final RSAPublicKey publicKey;
        @LauncherAPI public final Map<String, byte[]> runtime;

        @LauncherAPI
        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public Config(String address, int port, RSAPublicKey publicKey, Map<String, byte[]> runtime) {
            this.address = InetSocketAddress.createUnresolved(address, port);
            this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
            this.runtime = Collections.unmodifiableMap(new HashMap<>(runtime));
        }

        @LauncherAPI
        public Config(HInput input) throws IOException, InvalidKeySpecException {
            String localAddress = input.readASCII(255);
            address = InetSocketAddress.createUnresolved(
                ADDRESS_OVERRIDE == null ? localAddress : ADDRESS_OVERRIDE, input.readLength(65535));
            publicKey = SecurityHelper.toPublicRSAKey(input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH));

            // Read signed runtime
            int count = input.readLength(0);
            Map<String, byte[]> localResources = new HashMap<>(count);
            for (int i = 0; i < count; i++) {
                String name = input.readString(255);
                VerifyHelper.putIfAbsent(localResources, name,
                    input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH),
                    String.format("Duplicate runtime resource: '%s'", name));
            }
            runtime = Collections.unmodifiableMap(localResources);

            // Print warning if address override is enabled
            if (ADDRESS_OVERRIDE != null) {
                LogHelper.warning("Address override is enabled: '%s'", ADDRESS_OVERRIDE);
            }
        }

        @Override
        public void write(HOutput output) throws IOException {
            output.writeASCII(address.getHostString(), 255);
            output.writeLength(address.getPort(), 65535);
            output.writeByteArray(publicKey.getEncoded(), SecurityHelper.CRYPTO_MAX_LENGTH);

            // Write signed runtime
            Set<Entry<String, byte[]>> entrySet = runtime.entrySet();
            output.writeLength(entrySet.size(), 0);
            for (Entry<String, byte[]> entry : runtime.entrySet()) {
                output.writeString(entry.getKey(), 255);
                output.writeByteArray(entry.getValue(), SecurityHelper.CRYPTO_MAX_LENGTH);
            }
        }
    }
}
