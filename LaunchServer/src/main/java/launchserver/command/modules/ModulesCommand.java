package launchserver.command.modules;

import launchserver.LaunchServer;
import launchserver.command.Command;

public class ModulesCommand extends Command {
    public ModulesCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "get all modules";
    }

    @Override
    public void invoke(String... args) throws Exception {
    	server.modulesManager.printModules();
    }
}
