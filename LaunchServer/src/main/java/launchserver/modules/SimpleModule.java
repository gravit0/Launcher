package launchserver.modules;

import launcher.LauncherVersion;
import launcher.modules.Module;
import launcher.modules.ModuleContext;

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
    public void init(ModuleContext context) {

    }

    @Override
    public void preInit(ModuleContext context) {

    }

    @Override
    public void postInit(ModuleContext context) {

    }


    @Override
	public void close() {
		// on stop
	}
}
