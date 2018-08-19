package launchserver.command.modules;

import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.manangers.ModulesManager;

import java.net.URI;

public class LoadModuleCommand extends Command {
    public LoadModuleCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[jar]";
    }

    @Override
    public String getUsageDescription() {
        return "Module jar file";
    }

    @Override
    public void invoke(String... args) throws Exception {
        URI uri = new URI(args[0]);
        ModulesManager.loadModule(uri.toURL());
    }
}
