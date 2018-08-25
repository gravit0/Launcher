package launcher.modules;

import launcher.LauncherVersion;
import launcher.modules.ModuleContext;

public interface Module extends AutoCloseable {

    String getName();

    LauncherVersion getVersion();

    void init(ModuleContext context);
  
    void preInit(ModuleContext context);
    
    void postInit(ModuleContext context);
}
