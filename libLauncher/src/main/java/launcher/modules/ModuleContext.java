package launcher.modules;

public interface ModuleContext {
    enum Type {
        SERVER,CLIENT,LAUNCHSERVER
    }
    Type getType();
}
