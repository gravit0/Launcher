package launchserver;

public interface Module {
    String getName();

    String getVersion();

    void init(LaunchServer lsrv);
  
    void preInit(LaunchServer lsrv);
}
