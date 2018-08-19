package launchserver.manangers;

import launcher.LauncherAPI;
import launcher.LauncherClassLoader;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launchserver.LaunchServer;
import launchserver.Module;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModulesManager {
    public static ArrayList<Module> modules = new ArrayList<>();
    public static LauncherClassLoader classloader = new LauncherClassLoader(new URL[0],ClassLoader.getSystemClassLoader());
    public static LaunchServer launchserver;
    @LauncherAPI
    public static void loadModule(URL jarpath) throws ClassNotFoundException, IllegalAccessException, InstantiationException, URISyntaxException, IOException {
        JarFile f = new JarFile(Paths.get(jarpath.toURI()).toString());
        Manifest m = f.getManifest();
        String mainclass = m.getMainAttributes().getValue("Main-Class");
        loadModule(jarpath,mainclass);
        f.close();
    }
    @LauncherAPI
    public static void loadModule(URL jarpath, String classname) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        classloader.addURL(jarpath);
        Class moduleclass = Class.forName(classname,true,classloader);
        Module module = (Module) moduleclass.newInstance();
        modules.add(module);
        LogHelper.info("Module %s version: %s loaded",module.getName(),module.getVersion());
    }
    @LauncherAPI
    public static void registerModule(Module module)
    {
        modules.add(module);
        LogHelper.info("Module %s version: %s registered",module.getName(),module.getVersion());
    }
    @LauncherAPI
    public static void printModules() {
        for(Module m : modules)
        {
            LogHelper.info("Module %s version: %s",m.getName(),m.getVersion());
        }
    }
    public static void setLaunchServer(LaunchServer server)
    {
        launchserver = server;
    }
    @LauncherAPI
    public static void autoload() throws IOException {
        LogHelper.info("Load modules");
        IOHelper.walk(Paths.get("modules"),new ModulesVisitor(),false);
    }
    private static final class ModulesVisitor extends SimpleFileVisitor<Path> {
        private ModulesVisitor() {
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                JarFile f = new JarFile(file.toString());
                Manifest m = f.getManifest();
                String mainclass = m.getMainAttributes().getValue("Main-Class");
                loadModule(file.toUri().toURL(),mainclass);
                f.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            // Return result
            return super.visitFile(file, attrs);
        }
    }
}
