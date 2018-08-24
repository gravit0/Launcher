package launchserver.modules;

import launcher.LauncherVersion;
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
	public void preInit(LaunchServer lsrv) {
		// nothing to do
	}
	
	@Override
	public void init(LaunchServer lsrv) {
		// nothing to do
	}

	@Override
	public void postInit(LaunchServer lsrv) {
		// nothing to do
	}
	
	@Override
	public void close() {
		// nothing to do
	}
}
