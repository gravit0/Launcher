package launcher.neverdecomp;

import launcher.LauncherVersion;
import launcher.modules.Module;
import launcher.modules.ModuleContext;
import launcher.neverdecomp.asm.TransformerClass;
import launcher.serialize.config.entry.BooleanConfigEntry;
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
		return new LauncherVersion(1, 0, 1, 2);
	}

	@Override
	public void init(ModuleContext context1) {
		if (context1.getType().equals(ModuleContext.Type.LAUNCHSERVER)) {
			// Config may has boolean variable "hardAntiDecomp", which enables hard mode (needs -noverify to JVM)
			LaunchServerModuleContext context = (LaunchServerModuleContext) context1;
			boolean hobf = context.launchServer.config.block.hasEntry("hardAntiDecomp") ? context.launchServer.config.block.getEntryValue("hardAntiDecomp", BooleanConfigEntry.class) : false;
			context.launchServer.buildHookManager.registerClassTransformer(new TransformerClass(hobf));
		}
	}

	@Override
	public void preInit(ModuleContext context1) {
		
	}

	@Override
	public void postInit(ModuleContext context1) {
		
	}
}
