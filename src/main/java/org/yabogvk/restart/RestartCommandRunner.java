package org.yabogvk.restart;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class RestartCommandRunner {

    private final JavaPlugin plugin;

    public RestartCommandRunner(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void run(final List<String> commands) {
        final ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (final String command : commands) {
            try {
                Bukkit.dispatchCommand(console, command);
            } catch (RuntimeException exception) {
                this.plugin.getLogger().warning("Failed to execute restart command '" + command + "': " + exception.getMessage());
            }
        }
    }
}
