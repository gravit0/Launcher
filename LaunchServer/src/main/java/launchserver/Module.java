package launchserver;

public interface Module {
    String getName();

    String getVersion();

    void init();
  
    void preInit();
}
