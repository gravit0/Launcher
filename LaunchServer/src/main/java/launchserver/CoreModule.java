package launchserver;

import launcher.helper.CommonHelper;

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
	public void close() {
		// nothing to do
	}
}
