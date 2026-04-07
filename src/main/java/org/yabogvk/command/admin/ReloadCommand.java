package org.yabogvk.command.admin;

import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;

public final class ReloadCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public ReloadCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.reload";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.reload.no-permission"));
            return;
        }

        if (this.plugin.reloadPluginConfiguration()) {
            this.plugin.getNotificationService().sendInfo(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.reload.success"));
            return;
        }

        this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.reload.failed"));
    }
}
