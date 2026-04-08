package org.yabogvk.command.admin;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.yabogvk.YBVAutoRestart;
import org.yabogvk.command.AdminSubcommand;
import org.yabogvk.util.DurationParser;
import org.yabogvk.util.TimeFormatUtil;

public final class ScheduleCommand implements AdminSubcommand {

    private final YBVAutoRestart plugin;

    public ScheduleCommand(final YBVAutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public String getPermission() {
        return "ybvautorestart.admin.schedule";
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            this.plugin.getNotificationService().sendError(
                sender,
                this.plugin.getRestartScheduler().getPrefix(),
                this.plugin.getRestartScheduler().formatUserMessage("command.schedule.no-permission")
            );
            return;
        }

        if (args.length != 2) {
            this.plugin.getNotificationService().sendError(
                sender,
                this.plugin.getRestartScheduler().getPrefix(),
                this.plugin.getRestartScheduler().formatUserMessage("command.schedule.usage")
            );
            return;
        }

        final Duration duration;
        try {
            duration = DurationParser.parse(args[1]);
        } catch (IllegalArgumentException exception) {
            this.plugin.getNotificationService().sendError(
                sender,
                this.plugin.getRestartScheduler().getPrefix(),
                this.plugin.getRestartScheduler().formatUserMessage(
                    "command.schedule.invalid-duration",
                    Map.of("{VALUE}", args[1])
                )
            );
            return;
        }

        this.plugin.getRestartScheduler().scheduleRestartAfter(duration);
        final String formatted = TimeFormatUtil.formatDuration(duration.getSeconds());

        this.plugin.getNotificationService().sendInfo(
            sender,
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage(
                "command.schedule.success",
                Map.of("{TIME}", formatted)
            )
        );
        this.plugin.getNotificationService().broadcastInfo(
            this.plugin.getRestartScheduler().getPrefix(),
            this.plugin.getRestartScheduler().formatUserMessage(
                "command.schedule.broadcast",
                Map.of("{PLAYER}", sender.getName(), "{TIME}", formatted)
            ),
            true
        );
    }

    @Override
    public List<String> tabComplete(final String[] args) {
        if (args.length == 2) {
            return List.of("10m", "1h30m", "2m10s");
        }
        return List.of();
    }
}
