package launchserver.modules;

import launcher.LauncherClassLoader;
import launcher.modules.ModuleContext;
import launchserver.LaunchServer;

public class LaunchServerModuleContext implements ModuleContext {
    public final LaunchServer launchServer;
    public final LauncherClassLoader classloader;
    public LaunchServerModuleContext(LaunchServer server, LauncherClassLoader classloader)
    {
        launchServer = server;
        this.classloader = classloader;
    }
    @Override
    public Type getType() {
        return Type.LAUNCHSERVER;
    }
}
