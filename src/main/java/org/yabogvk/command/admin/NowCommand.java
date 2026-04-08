package org.yabogvk.command.admin;

import java.util.Map;
import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;
import org.yabogvk.util.TimeFormatUtil;

public final class NowCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public NowCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "now";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.now";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.now.no-permission"));
            return;
        }

        if (args.length != 1) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.now.usage"));
            return;
        }

        final int seconds = this.plugin.getRestartScheduler().getNowCountdownSeconds();
        this.plugin.getRestartScheduler().forceRestart(seconds);
        final String formatted = TimeFormatUtil.formatDuration(seconds, this.plugin.getRestartScheduler().getTimeFormatConfiguration());

        this.plugin.getNotificationService().sendInfo(
            sender,
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage("command.now.success", Map.of("{TIME}", formatted))
        );
        this.plugin.getNotificationService().broadcastInfo(
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage(
                "command.now.broadcast",
                Map.of("{PLAYER}", sender.getName(), "{TIME}", formatted)
            ),
            true
        );
    }
}
