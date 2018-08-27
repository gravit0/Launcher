package launchserver.command.basic;

import launcher.helper.JVMHelper;
import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.command.Command;
import launcher.managers.GarbageManager;

public final class GCCommand extends Command {
    public GCCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Perform Garbage Collection and print memory usage";
    }

    @Override
    public void invoke(String... args) {
        LogHelper.subInfo("Performing full GC");
        JVMHelper.fullGC();
        GarbageManager.gc();
        // Print memory usage
        long max = JVMHelper.RUNTIME.maxMemory() >> 20;
        long free = JVMHelper.RUNTIME.freeMemory() >> 20;
        long total = JVMHelper.RUNTIME.totalMemory() >> 20;
        long used = total - free;
        LogHelper.subInfo("Heap usage: %d / %d / %d MiB", used, total, max);
    }
}
