package launchserver.manangers;

import launcher.AutogenConfig;
import launchserver.binary.JAConfigurator;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

public class BuildHookManager {
    private final Set<PostBuildHook> POST_HOOKS;
    private final Set<PreBuildHook> PRE_HOOKS;
    private final Set<String> CLASS_BLACKLIST;
    private final Set<String> MODULE_CLASS;
	public BuildHookManager() {
		POST_HOOKS = new HashSet<>(4);
		PRE_HOOKS = new HashSet<>(4);
		CLASS_BLACKLIST = new HashSet<>(4);
		MODULE_CLASS = new HashSet<>(4);
		autoRegisterIgnoredClass(AutogenConfig.class.getName());
	}
    public void registerPostHook(PostBuildHook hook)
    {
        POST_HOOKS.add(hook);
    }
    public void registerIgnoredClass(String clazz)
    {
        CLASS_BLACKLIST.add(clazz);
    }
    public void autoRegisterIgnoredClass(String clazz)
    {
        CLASS_BLACKLIST.add(clazz.replace('.','/').concat(".class"));
    }
    public void registerClientModuleClass(String clazz)
    {
        MODULE_CLASS.add(clazz);
    }
    public void registerAllClientModuleClass(JAConfigurator cfg)
    {
        for(String clazz : MODULE_CLASS) cfg.addModuleClass(clazz);
    }
    public boolean isContainsBlacklist(String clazz)
    {
        return CLASS_BLACKLIST.contains(clazz);
    }
    public void postHook(ZipOutputStream output)
    {
        for(PostBuildHook hook : POST_HOOKS) hook.build(output);
    }
    public void preHook(ZipOutputStream output)
    {
        for(PreBuildHook hook : PRE_HOOKS) hook.build(output);
    }
    public void registerPreHook(PreBuildHook hook)
    {
        PRE_HOOKS.add(hook);
    }
    @FunctionalInterface
    public interface PostBuildHook
    {
        void build(ZipOutputStream output);
    }
    @FunctionalInterface
    public interface PreBuildHook
    {
        void build(ZipOutputStream output);
    }
}
