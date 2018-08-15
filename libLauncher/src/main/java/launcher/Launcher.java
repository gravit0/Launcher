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
	public static final int PROTOCOL_MAGIC = 0x724724_00 + 23;

	// Constants
	@LauncherAPI
	public static final String RUNTIME_DIR = "runtime";
	@LauncherAPI
	public static final String CONFIG_FILE = "config.bin";
	@LauncherAPI
	public static final String INIT_SCRIPT_FILE = "init.js";
	@LauncherAPI
	public static final String VERSIONREPLACE = "$VERSION$";
	@LauncherAPI
	public static final String BUILDREPLACE = "$BUILDNUMBER$";

	private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);
	@LauncherAPI
	public static final String[] repArray = genReps();

	@LauncherAPI
	public static String toHash(UUID uuid) {
		return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
	}

	private static String[] genReps() {
		Replace[] replace = new Replace[] { new Replace(Launcher.VERSIONREPLACE, Launcher.VERSION),
				new Replace(Launcher.BUILDREPLACE, Launcher.BUILD), };
		String[] repArray = new String[replace.length * 2];
		int i = 0;
		for (Replace r : replace) {
			repArray[i] = r.getSearch();
			repArray[i + 1] = r.getReplacement();
			i += 2;
		}
		return repArray;
	}

	@LauncherAPI
	public static String jvmProperty(String name, String value) {
		return String.format("-D%s=%s", name, value);
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

	@LauncherAPI
	public static String formatVars(String in) {
		return CommonHelper.replace(in, repArray);
	}

	public static final class Replace {
		private final String search;
		private final String replacement;

		public Replace(String search, String replacement) {
			this.search = search;
			this.replacement = replacement;
		}

		public String getReplacement() {
			return replacement;
		}

		public String getSearch() {
			return search;
		}
	}
}
