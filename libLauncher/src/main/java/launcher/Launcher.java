package launcher;

import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import launcher.helper.*;
import launcher.serialize.HInput;

public final class Launcher {
    private static final AtomicReference<LauncherConfig> CONFIG = new AtomicReference<>();

    // Version info
    @LauncherAPI
    public static final String VERSION = "15.4";
    @LauncherAPI
    public static final String BUILD = readBuildNumber();
    @LauncherAPI
    public static final int PROTOCOL_MAGIC = 0x724724_00 + 24;

    // Constants
    @LauncherAPI
    public static final String RUNTIME_DIR = "runtime";
    @LauncherAPI
    public static final String CONFIG_FILE = "config.bin";
    @LauncherAPI
    public static final String INIT_SCRIPT_FILE = "init.js";

    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);

    @LauncherAPI
    public static String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }

    @LauncherAPI
    public static LauncherConfig getConfig() {
        LauncherConfig config = CONFIG.get();
        if (config == null) {
            try (HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL(CONFIG_FILE)))) {
                config = new LauncherConfig(input);
            } catch (IOException | InvalidKeySpecException e) {
                throw new SecurityException(e);
            }
            CONFIG.set(config);
        }
        return config;
    }

    @LauncherAPI
    public static URL getResourceURL(String name) throws IOException {
        LauncherConfig config = getConfig();
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
    @SuppressWarnings({"SameReturnValue", "MethodReturnAlwaysConstant"})
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

}
