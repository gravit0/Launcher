package launchserver.binary;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javassist.*;
import launcher.AutogenConfig;
import launcher.Launcher;
import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.SecurityHelper.DigestAlgorithm;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;

import proguard.*;

import static launcher.helper.IOHelper.newZipEntry;

public final class JARLauncherBinary extends LauncherBinary {
	@LauncherAPI
	public final Path runtimeDir;
	@LauncherAPI
	public final Path initScriptFile;

	@LauncherAPI
	public JARLauncherBinary(LaunchServer server) throws IOException {
		super(server, server.dir.resolve(server.config.binaryName + ".jar"),
				server.dir.resolve(server.config.binaryName + "-obf.jar"));
		runtimeDir = server.dir.resolve(Launcher.RUNTIME_DIR);
		initScriptFile = runtimeDir.resolve(Launcher.INIT_SCRIPT_FILE);
		tryUnpackRuntime();
	}

	@Override
	public void build() throws IOException {
		tryUnpackRuntime();

		// Build launcher binary
		LogHelper.info("Building launcher binary file");
		if (server.config.sign.enabled) signBuild();
		else stdBuild();

		// ProGuard
		Configuration proguard_cfg = new Configuration();
		ConfigurationParser parser = new ConfigurationParser(
				server.proguardConf.confStrs.toArray(new String[server.proguardConf.confStrs.size()]),
				server.proguardConf.proguard.toFile(), 
				System.getProperties());
		try {
			parser.parse(proguard_cfg);
			ProGuard proGuard = new ProGuard(proguard_cfg);
			proGuard.execute();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	}

	private void signBuild() throws IOException {
		try (SignerJar output = new SignerJar
				(IOHelper.newOutput(binaryFile), 
						SignerJar.getStore(server.config.sign.key,
				server.config.sign.storepass, server.config.sign.algo), 
				server.config.sign.keyalias, server.config.sign.pass);
				JAConfigurator jaConfigurator = new JAConfigurator(AutogenConfig.class)) {
			Map<String, byte[]> outputM1 = new HashMap<>();
			server.buildHookManager.preHook(outputM1);
			for (Entry<String, byte[]> e : outputM1.entrySet()) {
				output.addFileContents(e.getKey(), e.getValue());
			}
			outputM1.clear();
			jaConfigurator.setAddress(server.config.getAddress());
			jaConfigurator.setPort(server.config.port);
			server.buildHookManager.registerAllClientModuleClass(jaConfigurator);
			try (ZipInputStream input = new ZipInputStream(
					IOHelper.newInput(IOHelper.getResourceURL("Launcher.jar")))) {
				ZipEntry e = input.getNextEntry();
				while (e != null) {
					String filename = e.getName();
					if (server.buildHookManager.isContainsBlacklist(filename)) {
						e = input.getNextEntry();
						continue;
					}
					if (filename.endsWith(".class")) {
						CharSequence classname = filename.replace('/', '.').subSequence(0,
								filename.length() - ".class".length());
						byte[] bytes;
						try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048)) {
							IOHelper.transfer(input, outputStream);
							bytes = outputStream.toByteArray();
						}
						bytes = server.buildHookManager.classTransform(bytes, classname);
						output.addFileContents(e, bytes);
					} else
						output.addFileContents(e, input);
					// }
					e = input.getNextEntry();
				}
			}
			// map for runtime
			Map<String, byte[]> runtime = new HashMap<>(256);
			if (server.buildHookManager.buildRuntime()) {
				// Verify has init script file
				if (!IOHelper.isFile(initScriptFile)) {
					throw new IOException(String.format("Missing init script file ('%s')", Launcher.INIT_SCRIPT_FILE));
				}
				// Write launcher runtime dir
				IOHelper.walk(runtimeDir, new RuntimeDirVisitor(output.getZos(), runtime), false);
			}
			// Create launcher config file
			byte[] launcherConfigBytes;
			try (ByteArrayOutputStream configArray = IOHelper.newByteArrayOutput()) {
				try (HOutput configOutput = new HOutput(configArray)) {
					new LauncherConfig(server.config.getAddress(), server.config.port, server.publicKey, runtime)
							.write(configOutput);
				}
				launcherConfigBytes = configArray.toByteArray();
			}

			// Write launcher config file
			output.addFileContents(Launcher.CONFIG_FILE, launcherConfigBytes);
			output.addFileContents(jaConfigurator.getZipEntryPath(), jaConfigurator.getBytecode());
			server.buildHookManager.postHook(outputM1);
			for (Entry<String, byte[]> e1 : outputM1.entrySet()) {
				output.addFileContents(e1.getKey(), e1.getValue());
			}
			outputM1.clear();
		} catch (CannotCompileException | NotFoundException e) {
			LogHelper.error(e);
		}
	}

	private void stdBuild() throws IOException {
		try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(binaryFile));
				JAConfigurator jaConfigurator = new JAConfigurator(AutogenConfig.class)) {
			Map<String, byte[]> outputM1 = new HashMap<>();
			server.buildHookManager.preHook(outputM1);
			for (Entry<String, byte[]> e : outputM1.entrySet()) {
				output.putNextEntry(newZipEntry(e.getKey()));
				output.write(e.getValue());
			}
			outputM1.clear();
			jaConfigurator.setAddress(server.config.getAddress());
			jaConfigurator.setPort(server.config.port);
			server.buildHookManager.registerAllClientModuleClass(jaConfigurator);
			try (ZipInputStream input = new ZipInputStream(
					IOHelper.newInput(IOHelper.getResourceURL("Launcher.jar")))) {
				ZipEntry e = input.getNextEntry();
				while (e != null) {
					String filename = e.getName();
					if (server.buildHookManager.isContainsBlacklist(filename)) {
						e = input.getNextEntry();
						continue;
					}
					try {
						output.putNextEntry(e);
					} catch (ZipException ex) {
						LogHelper.error(ex);
						e = input.getNextEntry();
						continue;
					}
					if (filename.endsWith(".class")) {
						CharSequence classname = filename.replace('/', '.').subSequence(0,
								filename.length() - ".class".length());
						byte[] bytes;
						try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048)) {
							IOHelper.transfer(input, outputStream);
							bytes = outputStream.toByteArray();
						}
						bytes = server.buildHookManager.classTransform(bytes, classname);
						try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
							IOHelper.transfer(inputStream, output);
						}
					} else
						IOHelper.transfer(input, output);
					// }
					e = input.getNextEntry();
				}
			}
			// map for runtime
			Map<String, byte[]> runtime = new HashMap<>(256);
			if (server.buildHookManager.buildRuntime()) {
				// Verify has init script file
				if (!IOHelper.isFile(initScriptFile)) {
					throw new IOException(String.format("Missing init script file ('%s')", Launcher.INIT_SCRIPT_FILE));
				}
				// Write launcher runtime dir
				IOHelper.walk(runtimeDir, new RuntimeDirVisitor(output, runtime), false);
			}
			// Create launcher config file
			byte[] launcherConfigBytes;
			try (ByteArrayOutputStream configArray = IOHelper.newByteArrayOutput()) {
				try (HOutput configOutput = new HOutput(configArray)) {
					new LauncherConfig(server.config.getAddress(), server.config.port, server.publicKey, runtime)
							.write(configOutput);
				}
				launcherConfigBytes = configArray.toByteArray();
			}

			// Write launcher config file
			output.putNextEntry(newZipEntry(Launcher.CONFIG_FILE));
			output.write(launcherConfigBytes);
			ZipEntry e = newZipEntry(jaConfigurator.getZipEntryPath());
			output.putNextEntry(e);
			output.write(jaConfigurator.getBytecode());
			server.buildHookManager.postHook(outputM1);
			for (Entry<String, byte[]> e1 : outputM1.entrySet()) {
				output.putNextEntry(newZipEntry(e1.getKey()));
				output.write(e1.getValue());
			}
			outputM1.clear();
		} catch (CannotCompileException | NotFoundException e) {
			LogHelper.error(e);
		}
	}

	@LauncherAPI
	public void tryUnpackRuntime() throws IOException {
		// Verify is runtime dir unpacked
		if (IOHelper.isDir(runtimeDir)) {
			return; // Already unpacked
		}

		// Unpack launcher runtime files
		Files.createDirectory(runtimeDir);
		LogHelper.info("Unpacking launcher runtime files");
		try (ZipInputStream input = IOHelper.newZipInput(IOHelper.getResourceURL("runtime.zip"))) {
			for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
				if (entry.isDirectory()) {
					continue; // Skip dirs
				}

				// Unpack runtime file
				IOHelper.transfer(input, runtimeDir.resolve(IOHelper.toPath(entry.getName())));
			}
		}
	}

	private static ZipEntry newEntry(String fileName) {
		return newZipEntry(Launcher.RUNTIME_DIR + IOHelper.CROSS_SEPARATOR + fileName);
	}

	private final class RuntimeDirVisitor extends SimpleFileVisitor<Path> {
		private final ZipOutputStream output;
		private final Map<String, byte[]> runtime;

		private RuntimeDirVisitor(ZipOutputStream output, Map<String, byte[]> runtime) {
			this.output = output;
			this.runtime = runtime;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			String dirName = IOHelper.toString(runtimeDir.relativize(dir));
			output.putNextEntry(newEntry(dirName + '/'));
			return super.preVisitDirectory(dir, attrs);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			String fileName = IOHelper.toString(runtimeDir.relativize(file));
			runtime.put(fileName, SecurityHelper.digest(DigestAlgorithm.MD5, file));

			// Create zip entry and transfer contents
			output.putNextEntry(newEntry(fileName));
			IOHelper.transfer(file, output);

			// Return result
			return super.visitFile(file, attrs);
		}
	}
}
