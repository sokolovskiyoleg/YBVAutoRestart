package org.yabogvk.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.action.ActionExecutor;
import org.yabogvk.action.ScheduledAction;
import org.yabogvk.config.LoadedConfiguration;
import org.yabogvk.notification.NotificationService;
import org.yabogvk.restart.RestartCommandRunner;
import org.yabogvk.util.TimeFormatUtil;

public final class RestartScheduler {

    private static final DateTimeFormatter TARGET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NotificationService notificationService;
    private final ActionExecutor actionExecutor;
    private final CountdownTask countdownTask;

    private LoadedConfiguration configuration;
    private RestartScheduleCalculator scheduleCalculator;
    private LocalDateTime nextRestart;
    private boolean manualTarget;
    private final Set<Long> deliveredIntervals = new HashSet<>();

    public RestartScheduler(
        final JavaPlugin plugin,
        final NotificationService notificationService,
        final RestartCommandRunner restartCommandRunner,
        final CountdownTask countdownTask
    ) {
        this.notificationService = notificationService;
        this.actionExecutor = new ActionExecutor(plugin, notificationService, restartCommandRunner);
        this.countdownTask = countdownTask;
    }

    public synchronized void applyConfiguration(final LoadedConfiguration configuration) {
        this.configuration = configuration;
        this.scheduleCalculator = new RestartScheduleCalculator(configuration.scheduleEntries());
        this.manualTarget = false;
        this.deliveredIntervals.clear();
        this.nextRestart = this.scheduleCalculator.findNextAfter(LocalDateTime.now()).orElse(null);
        this.updateTaskState();
    }

    public synchronized void tick() {
        if (this.nextRestart == null || this.configuration == null) {
            return;
        }

        final long remainingSeconds = Math.max(0L, Duration.between(LocalDateTime.now(), this.nextRestart).getSeconds());
        if (this.deliveredIntervals.add(remainingSeconds)) {
            final List<ScheduledAction> actions = this.configuration.actions().get(remainingSeconds);
            if (actions != null && !actions.isEmpty()) {
                this.actionExecutor.execute(actions, this.configuration.prefix(), remainingSeconds);
                this.notificationService.logInfo(this.configuration.prefix(), "Executed " + actions.size() + " action(s) at T-" + remainingSeconds + "s.");
            }
        }

        if (remainingSeconds == 0L) {
            this.prepareNextRestart(this.nextRestart);
        }
    }

    public synchronized Optional<LocalDateTime> getNextRestart() {
        return Optional.ofNullable(this.nextRestart);
    }

    public synchronized Optional<Long> getRemainingSeconds() {
        if (this.nextRestart == null) {
            return Optional.empty();
        }

        return Optional.of(Math.max(0L, Duration.between(LocalDateTime.now(), this.nextRestart).getSeconds()));
    }

    public synchronized String getFormattedStatus() {
        if (this.nextRestart == null) {
            return this.formatUserMessage("status.none");
        }

        final long remainingSeconds = this.getRemainingSeconds().orElse(0L);
        return this.formatUserMessage(
            "status.next",
            Map.of(
                "{TIME}", TimeFormatUtil.formatDuration(remainingSeconds),
                "{DATETIME}", TARGET_FORMATTER.format(this.nextRestart)
            )
        );
    }

    public synchronized String getPrefix() {
        return this.configuration != null ? this.configuration.prefix() : "&d&lYBVAutoRestart&r";
    }

    public synchronized String formatUserMessage(final String key) {
        return this.formatUserMessage(key, Map.of());
    }

    public synchronized String formatUserMessage(final String key, final Map<String, String> placeholders) {
        final String template;
        if (this.configuration == null) {
            template = key;
        } else {
            template = this.configuration.userMessages().getOrDefault(key, key);
        }

        final Map<String, String> values = new LinkedHashMap<>();
        values.put("{PREFIX}", this.getPrefix());
        values.putAll(placeholders);

        String resolved = template;
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            resolved = resolved.replace(entry.getKey(), entry.getValue());
        }
        return resolved;
    }

    public synchronized int getNowCountdownSeconds() {
        return this.configuration.nowCountdownSeconds();
    }

    public synchronized void forceRestart(final long secondsUntilRestart) {
        this.nextRestart = LocalDateTime.now().plusSeconds(Math.max(1L, secondsUntilRestart));
        this.manualTarget = true;
        this.deliveredIntervals.clear();
        this.updateTaskState();
    }

    public synchronized boolean delayRestart(final long extraSeconds) {
        if (this.nextRestart == null) {
            return false;
        }

        this.nextRestart = this.nextRestart.plusSeconds(extraSeconds);
        this.manualTarget = true;
        this.deliveredIntervals.clear();
        return true;
    }

    public synchronized boolean skipCurrentRestart() {
        if (this.nextRestart == null) {
            return false;
        }

        final LocalDateTime referenceTime = this.manualTarget ? LocalDateTime.now() : this.nextRestart;
        this.nextRestart = this.scheduleCalculator.findNextAfter(referenceTime).orElse(null);
        this.manualTarget = false;
        this.deliveredIntervals.clear();
        this.updateTaskState();
        return true;
    }

    public synchronized void shutdown() {
        this.countdownTask.stop();
    }

    private void prepareNextRestart(final LocalDateTime completedTarget) {
        final LocalDateTime referenceTime = this.manualTarget ? LocalDateTime.now() : completedTarget;
        this.nextRestart = this.scheduleCalculator.findNextAfter(referenceTime).orElse(null);
        this.manualTarget = false;
        this.deliveredIntervals.clear();
        this.updateTaskState();
    }

    private void updateTaskState() {
        if (this.nextRestart == null) {
            this.countdownTask.stop();
            return;
        }

        this.countdownTask.start();
    }
}
