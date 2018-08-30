package launcher.modules;

import launcher.LauncherVersion;

public interface Module extends AutoCloseable {

    String getName();

    LauncherVersion getVersion();

    void init(ModuleContext context);
  
    void postInit(ModuleContext context);
    
    void preInit(ModuleContext context);
}
