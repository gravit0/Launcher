package launchserver;

import launcher.helper.CommonHelper;

public class CoreModule implements Module {

	@Override
	public String getName() {
		return "LaunchServer";
	}

	@Override
	public String getVersion() {
		return CommonHelper.formatVars("$VERSION$, build $BUILDNUMBER$");
	}

	@Override
	public void preInit() {
		// nothing to do
	}
	
	@Override
	public void init() {
		// nothing to do
	}
}
