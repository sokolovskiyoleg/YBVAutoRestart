package org.yabogvk.action;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.notification.NotificationService;
import org.yabogvk.restart.RestartCommandRunner;
import org.yabogvk.util.TimeFormatUtil;

public final class ActionExecutor {

    private final JavaPlugin plugin;
    private final NotificationService notificationService;
    private final RestartCommandRunner restartCommandRunner;
    private final org.yabogvk.config.TimeFormatConfiguration timeFormatConfiguration;

    public ActionExecutor(
        final JavaPlugin plugin,
        final NotificationService notificationService,
        final RestartCommandRunner restartCommandRunner,
        final org.yabogvk.config.TimeFormatConfiguration timeFormatConfiguration
    ) {
        this.plugin = plugin;
        this.notificationService = notificationService;
        this.restartCommandRunner = restartCommandRunner;
        this.timeFormatConfiguration = timeFormatConfiguration;
    }

    public void execute(final List<ScheduledAction> actions, final String prefix, final long remainingSeconds) {
        final Map<String, String> placeholders = Map.of(
            "{PREFIX}", prefix,
            "{TIME}", TimeFormatUtil.formatDuration(remainingSeconds, this.timeFormatConfiguration),
            "{SECONDS}", Long.toString(remainingSeconds)
        );

        for (final ScheduledAction action : actions) {
            this.executeAction(action, placeholders);
        }
    }

    private void executeAction(final ScheduledAction action, final Map<String, String> placeholders) {
        switch (action.type()) {
            case MESSAGE -> this.notificationService.broadcastChat(this.applyPlaceholders(action.payload(), placeholders));
            case ACTIONBAR -> this.notificationService.broadcastActionBar(this.applyPlaceholders(action.payload(), placeholders));
            case SOUND -> this.executeSound(action.payload());
            case COMMAND -> this.restartCommandRunner.run(List.of(this.applyPlaceholders(action.payload(), placeholders)));
        }
    }

    private void executeSound(final String payload) {
        final String[] parts = payload.split("\\s+");

        try {
            final Sound sound = Sound.valueOf(parts[0].toUpperCase(Locale.ROOT));
            final float volume = Float.parseFloat(parts[1]);
            final float pitch = Float.parseFloat(parts[2]);
            this.notificationService.broadcastSound(sound, volume, pitch);
        } catch (IllegalArgumentException exception) {
            this.plugin.getLogger().warning("Failed to execute sound action '" + payload + "': " + exception.getMessage());
        }
    }

    private String applyPlaceholders(final String template, final Map<String, String> placeholders) {
        String resolved = template;
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace(entry.getKey(), entry.getValue());
        }
        return resolved;
    }
}
