package launchserver.modules;

import launcher.helper.CommonHelper;
import launchserver.LaunchServer;
import launchserver.modules.Module;

public class CoreModule implements Module {
	@Override
	public String getName() {
		return "LaunchServer";
	}

	@Override
	public String getVersion() {
		return CommonHelper.formatVars("%VERSION%, build %BUILDNUMBER%");
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
