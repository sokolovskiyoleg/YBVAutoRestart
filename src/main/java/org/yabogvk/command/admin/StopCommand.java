package org.yabogvk.command.admin;

import java.util.Map;
import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;

public final class StopCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public StopCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.stop";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.stop.no-permission"));
            return;
        }

        if (args.length != 1) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.stop.usage"));
            return;
        }

        if (!this.plugin.getRestartScheduler().skipCurrentRestart()) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.stop.no-active"));
            return;
        }

        this.plugin.getNotificationService().sendInfo(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.stop.success"));
        this.plugin.getNotificationService().broadcastInfo(
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage("command.stop.broadcast", Map.of("{PLAYER}", sender.getName())),
            true
        );
    }
}
