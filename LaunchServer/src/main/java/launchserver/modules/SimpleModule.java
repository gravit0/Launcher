package launchserver.modules;

import launcher.LauncherVersion;
import launchserver.LaunchServer;

public class SimpleModule implements Module {
    @Override
    public String getName() {
        return "SimpleModule";
    }

    @Override
    public LauncherVersion getVersion() {
        return new LauncherVersion(1,0,0);
    }

	@Override
	public void preInit(LaunchServer lsrv) {
		// pre-init code
	}
    
    @Override
    public void init(LaunchServer lsrv) {
        // init code
    }

	@Override
	public void postInit(LaunchServer lsrv) {
		// post-init code
	}
    
    public static void main(String[] args) {
        System.err.println("Модуль нельзя запускать как программу");
        System.exit(0);
    }

	@Override
	public void close() {
		// on stop
	}
}
