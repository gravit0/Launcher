package launcher.server;

import launcher.LauncherClassLoader;
import launcher.modules.ModuleContext;

public class ServerModuleContext implements ModuleContext {
    public final LauncherClassLoader classLoader;
    public final ServerWrapper wrapper;

    public ServerModuleContext(ServerWrapper wrapper, LauncherClassLoader classLoader) {
        this.classLoader = classLoader;
        this.wrapper = wrapper;
    }

    @Override
    public Type getType() {
        return Type.SERVER;
    }
}
