package launchserver.manangers;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

public class BuildHookManager {
    private static final Set<PostBuildHook> POST_HOOKS = new HashSet<>(4);
    private static final Set<PreBuildHook> PRE_HOOKS = new HashSet<>(4);
    public static void registerPostHook(PostBuildHook hook)
    {
        POST_HOOKS.add(hook);
    }
    public static void postHook(ZipOutputStream output)
    {
        for(PostBuildHook hook : POST_HOOKS) hook.build(output);
    }
    public static void preHook(ZipOutputStream output)
    {
        for(PreBuildHook hook : PRE_HOOKS) hook.build(output);
    }
    public static void registerPreHook(PreBuildHook hook)
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
