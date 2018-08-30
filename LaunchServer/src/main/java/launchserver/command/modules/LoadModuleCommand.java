package launchserver.command.modules;

import java.net.URI;
import java.nio.file.Paths;

import launchserver.LaunchServer;
import launchserver.command.Command;

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
        verifyArgs(args, 1);
        URI uri = Paths.get(args[0]).toUri();
        server.modulesManager.loadModule(uri.toURL(), false);
    }
}
