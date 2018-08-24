package launchserver.modules;

import launchserver.LaunchServer;

public interface Module extends AutoCloseable {
    String getName();

    String getVersion();

    void init(LaunchServer lsrv);
  
    void preInit(LaunchServer lsrv);
    
    void postInit(LaunchServer lsrv);
}
