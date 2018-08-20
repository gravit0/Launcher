package launchserver.command.modules;

import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.manangers.ModulesManager;

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
        ModulesManager.printModules();
    }
}
