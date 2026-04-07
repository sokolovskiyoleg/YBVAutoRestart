package org.yabogvk.command.admin;

import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;
import org.yabogvk.util.TimeFormatUtil;

public final class DelayCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public DelayCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delay";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.delay";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.delay.no-permission"));
            return;
        }

        if (args.length != 2) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.delay.usage"));
            return;
        }

        final long extraSeconds;
        try {
            extraSeconds = Long.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            this.plugin.getNotificationService().sendError(
                sender,
                this.plugin.getRestartScheduler().getPrefix(),
                this.plugin.getRestartScheduler().formatUserMessage("command.delay.invalid-number", Map.of("{VALUE}", args[1]))
            );
            return;
        }

        if (extraSeconds <= 0) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.delay.non-positive"));
            return;
        }

        if (!this.plugin.getRestartScheduler().delayRestart(extraSeconds)) {
            this.plugin.getNotificationService().sendError(sender, this.plugin.getRestartScheduler().getPrefix(), this.plugin.getRestartScheduler().formatUserMessage("command.delay.no-active"));
            return;
        }

        final String formatted = TimeFormatUtil.formatDuration(extraSeconds);
        this.plugin.getNotificationService().sendInfo(
            sender,
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage("command.delay.success", Map.of("{TIME}", formatted))
        );
        this.plugin.getNotificationService().broadcastInfo(
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage(
                "command.delay.broadcast",
                Map.of("{TIME}", formatted, "{PLAYER}", sender.getName())
            ),
            true
        );
    }

    @Override
    public List<String> tabComplete(final String[] args) {
        if (args.length == 2) {
            return List.of("60", "300", "600");
        }
        return List.of();
    }
}
