package launcher.neverdecomp;

import launcher.LauncherVersion;
import launcher.modules.Module;
import launcher.modules.ModuleContext;
import launchserver.modules.LaunchServerModuleContext;

public class NeverDecompModule implements Module {
	@Override
	public void close() {

	}

	@Override
	public String getName() {
		return "NeverDecomp";
	}

	@Override
	public LauncherVersion getVersion() {
		return new LauncherVersion(1, 0, 0, 3);
	}

	@SuppressWarnings("unused")
	@Override
	public void init(ModuleContext context1) {
		if (context1.getType().equals(ModuleContext.Type.LAUNCHSERVER)) {
			LaunchServerModuleContext context = (LaunchServerModuleContext) context1;
			context.launchServer.buildHookManager.registerClassTransformer(new TransformerClass());
		}
	}

	@Override
	public void preInit(ModuleContext context1) {
		
	}

	@Override
	public void postInit(ModuleContext context1) {
		
	}

}
