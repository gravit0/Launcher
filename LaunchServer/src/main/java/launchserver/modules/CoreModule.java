package launchserver.modules;

import launcher.LauncherVersion;
import launcher.modules.Module;
import launcher.modules.ModuleContext;

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
		// nothing to do
	}

	@Override
	public void preInit(ModuleContext context) {
		// nothing to do
	}

	@Override
	public void postInit(ModuleContext context) {
		// nothing to do
	}


	@Override
	public void close() {
		// nothing to do
	}
}
