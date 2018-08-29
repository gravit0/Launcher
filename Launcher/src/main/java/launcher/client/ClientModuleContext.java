package launcher.client;

import launcher.LauncherEngine;
import launcher.modules.ModuleContext;

public class ClientModuleContext implements ModuleContext {
    public final LauncherEngine engine;
    ClientModuleContext(LauncherEngine engine)
    {
        this.engine = engine;
    }
    @Override
    public Type getType() {
        return Type.CLIENT;
    }
}
