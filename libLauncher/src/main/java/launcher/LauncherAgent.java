package launcher;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

@LauncherAPI
public class LauncherAgent {
    public static Instrumentation inst;

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        System.out.println("Launcher Agent");
        inst = instrumentation;
    }

    public static void addJVMClassPath(String path) throws IOException {
        System.out.println("Launcher Agent");
        inst.appendToSystemClassLoaderSearch(new JarFile(path));
    }
}
