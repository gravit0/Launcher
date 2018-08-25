package launcher.client;

import launcher.LauncherEngine;
import launcher.modules.ModuleContext;

public class ClientModuleContext implements ModuleContext {
    public final ClientLauncher launcher;
    public final LauncherEngine engine;
    ClientModuleContext(ClientLauncher launcher, LauncherEngine engine)
    {
        this.launcher = launcher;
        this.engine = engine;
    }
    @Override
    public Type getType() {
        return Type.CLIENT;
    }
}
