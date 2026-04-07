package org.yabogvk.command.admin;

import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;

public final class StatusCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public StatusCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.status";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.status.no-permission"));
            return;
        }

        this.plugin.getNotificationService().sendInfo(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().getFormattedStatus());
    }
}
