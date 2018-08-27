package launchserver.modules;

import launcher.LauncherClassLoader;
import launcher.modules.ModuleContext;
import launchserver.LaunchServer;

public class LaunchServerModuleContext implements ModuleContext {
    public LaunchServerModuleContext(LaunchServer server, LauncherClassLoader classloader)
    {
        this.launchServer = server;
        this.classloader = classloader;
    }
    public final LaunchServer launchServer;
    public final LauncherClassLoader classloader;
    @Override
    public Type getType() {
        return Type.LAUNCHSERVER;
    }
}
