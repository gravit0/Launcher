package launchserver.command.auth;

import java.util.List;

import launchserver.LaunchServer;
import launchserver.auth.hwid.HWID;
import launchserver.command.Command;

public class BanCommand extends Command {
    public BanCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[username]";
    }

    @Override
    public String getUsageDescription() {
        return "Ban username for HWID";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args,1);
        List<HWID> target = server.config.hwidHandler.getHwid(args[0]);
        server.config.hwidHandler.ban(target);
    }
}
