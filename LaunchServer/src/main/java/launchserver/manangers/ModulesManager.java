package launchserver.manangers;

import launcher.LauncherAPI;
import launcher.LauncherClassLoader;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launchserver.CoreModule;
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
    public ArrayList<Module> modules;
    public LauncherClassLoader classloader;
	private LaunchServer lsrv;

    public ModulesManager(LaunchServer lsrv) {
    	this.lsrv = lsrv;
    	modules = new ArrayList<Module>();
    	classloader = new LauncherClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
    }
    
    @LauncherAPI
    public void loadModule(URL jarpath, boolean preload) throws ClassNotFoundException, IllegalAccessException, InstantiationException, URISyntaxException, IOException {
        JarFile f = new JarFile(Paths.get(jarpath.toURI()).toString());
        Manifest m = f.getManifest();
        String mainclass = m.getMainAttributes().getValue("Main-Class");
        loadModule(jarpath, mainclass, preload);
        f.close();
    }

    @LauncherAPI
    public void loadModule(URL jarpath, String classname, boolean preload) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        classloader.addURL(jarpath);
        Class moduleclass = Class.forName(classname, true, classloader);
        Module module = (Module) moduleclass.newInstance();
        modules.add(module);
        module.preInit(lsrv);
        if(!preload) module.init(lsrv);
        LogHelper.info("Module %s version: %s loaded",module.getName(),module.getVersion());
    }

    @LauncherAPI
    public void registerModule(Module module,boolean preload)
    {
        load(module, preload);
        LogHelper.info("Module %s version: %s registered",module.getName(),module.getVersion());
    }

    @LauncherAPI
    public void initModules() {
        for (Module m : modules) {
            m.init(lsrv);
            LogHelper.info("Module %s version: %s init", m.getName(), m.getVersion());
        }
    }

    @LauncherAPI
    public void preInitModules() {
        for (Module m : modules) {
            m.preInit(lsrv);
            LogHelper.info("Module %s version: %s pre-init", m.getName(), m.getVersion());
        }
    }

    
    @LauncherAPI
    public void printModules() {
        for (Module m : modules) {
            LogHelper.info("Module %s version: %s", m.getName(), m.getVersion());
        }
        LogHelper.info("Loaded %d modules", modules.size());
    }

    @LauncherAPI
    public void autoload() throws IOException {
        LogHelper.info("Load modules");
        registerCoreModule();
        Path modules = lsrv.dir.resolve("modules");
        if (Files.notExists(modules)) Files.createDirectory(modules);
        IOHelper.walk(modules, new ModulesVisitor(), true);
        LogHelper.info("Loaded %d modules", this.modules.size());
    }
    private void registerCoreModule() {
    	load(new CoreModule());
	}
    
    @LauncherAPI
	public void load(Module module) {
		modules.add(module);
	}
    
    
    @LauncherAPI
	public void load(Module module, boolean preload) {
		load(module);
		if (!preload) module.init(lsrv);
	}

    private final class ModulesVisitor extends SimpleFileVisitor<Path> {
        private ModulesVisitor() {
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                JarFile f = new JarFile(file.toString());
                Manifest m = f.getManifest();
                String mainclass = m.getMainAttributes().getValue("Main-Class");
                loadModule(file.toUri().toURL(), mainclass, true);
                f.close();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }

            // Return result
            return super.visitFile(file, attrs);
        }
    }
}
