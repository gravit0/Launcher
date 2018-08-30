package launchserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;

public class ProguardConf {
	private static final String charsFirst = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
	private static final String chars = "1aAbBcC2dDeEfF3gGhHiI4jJkKl5mMnNoO6pPqQrR7sStT8uUvV9wWxX0yYzZ";
	private static String generateString(SecureRandom rand, int il) {
		StringBuffer sb = new StringBuffer(il);
		sb.append(charsFirst.charAt(rand.nextInt(charsFirst.length())));
		for (int i = 0; i < il; i++) sb.append(chars.charAt(rand.nextInt(chars.length())));
		return sb.toString();
	}
	private final LaunchServer srv;
	public final Path proguard;
	public final Path config;
	public final Path mappings;
	public final Path words;
	public final Set<String> confStrs;
	
	public ProguardConf(LaunchServer srv) {
		this.srv = srv;
		proguard =  this.srv.dir.resolve("proguard");
		config = proguard.resolve("proguard.config");
		mappings = proguard.resolve("mappings.pro");
		words = proguard.resolve("random.pro");
		confStrs = new HashSet<>();
		checkDirs();
		prepare(false);
		confStrs.add(readConf());
		if (this.srv.config.genMappings) confStrs.add("-printmapping \'" + mappings.toFile().getName() + "\'");
		confStrs.add("-obfuscationdictionary \'" + words.toFile().getName() + "\'");
		confStrs.add("-classobfuscationdictionary \'" + words.toFile().getName() + "\'");

	}

	private void checkDirs() {
		try {
			IOHelper.createParentDirs(config);
		} catch (IOException ign) {
			
		}
	}

	private void genConfig(boolean force) throws IOException {
		if (IOHelper.exists(config) || !force) return;
		Files.deleteIfExists(config);
		List<String> lines = Files.readAllLines(config, IOHelper.UNICODE_CHARSET);
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(IOHelper.newOutput(config), IOHelper.UNICODE_CHARSET))) {
			lines.forEach(out::println);
		}
	}

	private void genWords(boolean force) throws IOException {
		if (IOHelper.exists(words) || !force) return;
		Files.deleteIfExists(words);
		SecureRandom rand = SecurityHelper.newRandom();
		rand.setSeed(SecureRandom.getSeed(32));
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(IOHelper.newOutput(words), IOHelper.UNICODE_CHARSET))) {
			for (int i = 0; i < Short.MAX_VALUE; i++) out.println(generateString(rand, 24));
		}
	}

	public void prepare(boolean force) {
		if (IOHelper.exists(config) && IOHelper.exists(words) && !force) return; 
		try {
			genConfig(force);
			genWords(force);
		} catch (IOException e) {
			LogHelper.error(e);
		}
	}

	private String readConf() {
		return "@".concat(config.toString());
	}
}
