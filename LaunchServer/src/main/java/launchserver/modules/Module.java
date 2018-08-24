package launchserver.modules;

import launcher.LauncherVersion;
import launchserver.LaunchServer;

public interface Module extends AutoCloseable {
    String getName();

    LauncherVersion getVersion();

    void init(LaunchServer lsrv);
  
    void preInit(LaunchServer lsrv);
    
    void postInit(LaunchServer lsrv);
}
