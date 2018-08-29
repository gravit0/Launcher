package launchserver.binary;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
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
        super(server, server.dir.resolve(server.config.binaryName + ".jar"));
        runtimeDir = server.dir.resolve(Launcher.RUNTIME_DIR);
        initScriptFile = runtimeDir.resolve(Launcher.INIT_SCRIPT_FILE);
        tryUnpackRuntime();
    }

    @Override
    public void build() throws IOException {
        tryUnpackRuntime();

        // Build launcher binary
        LogHelper.info("Building launcher binary file");
        try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(binaryFile));
             JAConfigurator jaConfigurator = new JAConfigurator(AutogenConfig.class)) {
            server.buildHookManager.preHook(output);
            jaConfigurator.setAddress(server.config.getAddress());
            jaConfigurator.setPort(server.config.port);
            server.buildHookManager.registerAllClientModuleClass(jaConfigurator);
            try (ZipInputStream input = new ZipInputStream(IOHelper.newInput(IOHelper.getResourceURL("Launcher.jar")))) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    String filename = e.getName();
                    if(server.buildHookManager.isContainsBlacklist(filename)) {
                        e = input.getNextEntry();
                        continue;
                    }
                    try {
                        output.putNextEntry(e);
                    } catch (ZipException ex)
                    {
                        LogHelper.error(ex);
                        e = input.getNextEntry();
                        continue;
                    }
                    if(filename.endsWith(".class"))
                    {
                        CharSequence classname = filename.replace('/','.').subSequence(0,filename.length() - ".class".length());
                        byte[] bytes;
                        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048)) {
                            IOHelper.transfer(input, outputStream);
                            bytes = outputStream.toByteArray();
                        }
                        bytes = server.buildHookManager.classTransform(bytes, classname);
                        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                            IOHelper.transfer(inputStream, output);
                        }
                    }
                    else
                    IOHelper.transfer(input, output);
                    //}
                    e = input.getNextEntry();
                }
            }
            // Verify has init script file
            if (!IOHelper.isFile(initScriptFile)) {
                throw new IOException(String.format("Missing init script file ('%s')", Launcher.INIT_SCRIPT_FILE));
            }
            // Write launcher runtime dir
            Map<String, byte[]> runtime = new HashMap<>(256);
            IOHelper.walk(runtimeDir, new RuntimeDirVisitor(output, runtime), false);
            // Create launcher config file
            byte[] launcherConfigBytes;
            try (ByteArrayOutputStream configArray = IOHelper.newByteArrayOutput()) {
                try (HOutput configOutput = new HOutput(configArray)) {
                    new LauncherConfig(server.config.getAddress(), server.config.port, server.publicKey, runtime).write(configOutput);
                }
                launcherConfigBytes = configArray.toByteArray();
            }

            // Write launcher config file
            output.putNextEntry(newZipEntry(Launcher.CONFIG_FILE));
            output.write(launcherConfigBytes);
            ZipEntry e = newZipEntry(jaConfigurator.getZipEntryPath());
            output.putNextEntry(e);
            output.write(jaConfigurator.getBytecode());
            server.buildHookManager.postHook(output);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        //ProGuard
        Configuration proguard_cfg = new Configuration();
        String[] args = new String[1];
        args[0] = "@".concat("proguard.pro");
        ConfigurationParser parser = new ConfigurationParser(args,
                System.getProperties());
        try {
            parser.parse(proguard_cfg);
            ProGuard proGuard = new ProGuard(proguard_cfg);
            proGuard.execute();
        } catch (ParseException e1) {
            e1.printStackTrace();
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
