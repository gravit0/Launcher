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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import launcher.Launcher;
import launcher.LauncherConfig;
import launcher.LauncherAPI;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.SecurityHelper.DigestAlgorithm;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.manangers.BuildHookManager;

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
        try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(binaryFile))) {
            BuildHookManager.preHook(output);
            //ClassPool pool = ClassPool.getDefault();
            //CtClass ctClass = pool.get(JAConfig.class.getCanonicalName());
            //CtConstructor ctConstructor = ctClass.getDeclaredConstructor(null);
            //ctConstructor.setBody("{ this.address = \""+server.config.getAddress()+"\";"+
            //        "this.port = "+server.config.port+"; }");
            //String findName = "launcher/"+JAConfig.class.getSimpleName()+".class";
            //System.out.println(findName);
            try (ZipInputStream input = new ZipInputStream(IOHelper.newInput(IOHelper.getResourceURL("Launcher.jar")))) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    //if(e.getName().equals(findName))
                    //{
                    //    System.out.println("FOUND!");
                    //    ZipEntry en = new ZipEntry(e.getName());
                    //    output.putNextEntry(en);
                    //    output.write(ctClass.toBytecode());
                    //}
                    //else {
                    output.putNextEntry(e);
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
            output.putNextEntry(IOHelper.newZipEntry(Launcher.CONFIG_FILE));
            output.write(launcherConfigBytes);
            BuildHookManager.postHook(output);
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
        return IOHelper.newZipEntry(Launcher.RUNTIME_DIR + IOHelper.CROSS_SEPARATOR + fileName);
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
