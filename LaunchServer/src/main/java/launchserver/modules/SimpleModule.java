package launchserver.modules;

import launchserver.LaunchServer;
import launchserver.modules.Module;

public class SimpleModule implements Module {
    @Override
    public String getName() {
        return "SimpleModule";
    }

    @Override
    public String getVersion() {
        return "1";
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
