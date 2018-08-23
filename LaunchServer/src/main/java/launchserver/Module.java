package launchserver;

public interface Module extends AutoCloseable {
    String getName();

    String getVersion();

    void init(LaunchServer lsrv);
  
    void preInit(LaunchServer lsrv);
}
