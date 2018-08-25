package launchserver.modules;

import launcher.LauncherVersion;
import launcher.modules.Module;
import launcher.modules.ModuleContext;
import launchserver.LaunchServer;

public class CoreModule implements Module {
	@Override
	public String getName() {
		return "LaunchServer";
	}

	@Override
	public LauncherVersion getVersion() {
		return LauncherVersion.getVersion();
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
		// nothing to do
	}
}
