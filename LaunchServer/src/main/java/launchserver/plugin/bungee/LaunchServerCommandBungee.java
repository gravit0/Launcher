package launchserver.plugin.bungee;

import launchserver.plugin.LaunchServerPluginBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public final class LaunchServerCommandBungee extends Command {
	public static final BaseComponent[] NOT_CONSOLE_MESSAGE = TextComponent.fromLegacyText(ChatColor.RED + "Эту команду можно использовать только из консоли");
	public static final BaseComponent[] NOT_INITIALIZED_MESSAGE = TextComponent.fromLegacyText(ChatColor.RED + "Лаунчсервер не был полностью загружен");

    // Instance
    public final LaunchServerPluginBungee plugin;

    public LaunchServerCommandBungee(LaunchServerPluginBungee plugin) {
        super("launchserver", "launchserver.command", "launcher", "ls", "l");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        /*if (!(sender.getClass().equals("net.md_5.bungee.command.ConsoleCommandSender"))) {
            sender.sendMessage(NOT_CONSOLE_MESSAGE);
            return;
        }*/

        // Eval command
        LaunchServerPluginBridge bridge = plugin.bridge;
        if (bridge == null) {
            sender.sendMessage(NOT_INITIALIZED_MESSAGE);
        } else {
            bridge.eval(args);
        }
    }
}
