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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModulesManager {
    public static ArrayList<Module> modules = new ArrayList<>();
    public static LauncherClassLoader classloader = new LauncherClassLoader(new URL[0],ClassLoader.getSystemClassLoader());
    public static LaunchServer launchserver;
    @LauncherAPI
    public static void loadModule(URL jarpath,boolean preload) throws ClassNotFoundException, IllegalAccessException, InstantiationException, URISyntaxException, IOException {
        JarFile f = new JarFile(Paths.get(jarpath.toURI()).toString());
        Manifest m = f.getManifest();
        String mainclass = m.getMainAttributes().getValue("Main-Class");
        loadModule(jarpath,mainclass,preload);
        f.close();
    }
    @LauncherAPI
    public static void loadModule(URL jarpath, String classname,boolean preload) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        classloader.addURL(jarpath);
        Class moduleclass = Class.forName(classname,true,classloader);
        Module module = (Module) moduleclass.newInstance();
        modules.add(module);
        if(!preload) module.init();
        LogHelper.info("Module %s version: %s loaded",module.getName(),module.getVersion());
    }
    @LauncherAPI
    public static void registerModule(Module module,boolean preload)
    {
        modules.add(module);
        if(!preload) module.init();
        LogHelper.info("Module %s version: %s registered",module.getName(),module.getVersion());
    }
    @LauncherAPI
    public static void initModules()
    {
        for(Module m : modules)
        {
            m.init();
            LogHelper.info("Module %s version: %s init",m.getName(),m.getVersion());
        }
    }
    @LauncherAPI
    public static void printModules() {
        for(Module m : modules)
        {
            LogHelper.info("Module %s version: %s",m.getName(),m.getVersion());
        }
        LogHelper.info("Loaded %d modules",modules.size());
    }
    public static void setLaunchServer(LaunchServer server)
    {
        launchserver = server;
    }
    @LauncherAPI
    public static void autoload() throws IOException {
        LogHelper.info("Load modules");
        Path modules = Paths.get("modules");
        if(Files.notExists(modules)) Files.createDirectory(modules);
        IOHelper.walk(modules,new ModulesVisitor(),true);
        LogHelper.info("Loaded %d modules",ModulesManager.modules.size());
        initModules();
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
                loadModule(file.toUri().toURL(),mainclass,true);
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
