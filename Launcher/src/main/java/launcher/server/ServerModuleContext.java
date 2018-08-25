package launcher.server;

import launcher.LauncherClassLoader;
import launcher.modules.ModuleContext;

public class ServerModuleContext implements ModuleContext {
    public final LauncherClassLoader classLoader;

    public ServerModuleContext(LauncherClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Type getType() {
        return Type.SERVER;
    }
}
