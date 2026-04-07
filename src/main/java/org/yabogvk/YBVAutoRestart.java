package org.yabogvk;

import java.util.Objects;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.command.AutoRestartCommand;
import org.yabogvk.config.ConfigManager;
import org.yabogvk.config.LoadedConfiguration;
import org.yabogvk.notification.NotificationService;
import org.yabogvk.restart.RestartCommandRunner;
import org.yabogvk.scheduler.CountdownTask;
import org.yabogvk.scheduler.RestartScheduler;

public final class YBVAutoRestart extends JavaPlugin {

    private ConfigManager configManager;
    private NotificationService notificationService;
    private RestartScheduler restartScheduler;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.ensureDefaultFiles();

        final LoadedConfiguration configuration = this.configManager.load();
        this.notificationService = new NotificationService(this);

        final RestartCommandRunner restartCommandRunner = new RestartCommandRunner(this);
        final CountdownTask countdownTask = new CountdownTask(this);
        this.restartScheduler = new RestartScheduler(
            this,
            this.notificationService,
            restartCommandRunner,
            countdownTask
        );
        countdownTask.bind(this.restartScheduler);
        this.restartScheduler.applyConfiguration(configuration);

        this.registerCommands();
        this.getLogger().info("YBVAutoRestart enabled.");
    }

    @Override
    public void onDisable() {
        if (this.restartScheduler != null) {
            this.restartScheduler.shutdown();
        }
    }

    public boolean reloadPluginConfiguration() {
        try {
            final LoadedConfiguration configuration = this.configManager.load();
            this.restartScheduler.applyConfiguration(configuration);
            return true;
        } catch (RuntimeException exception) {
            this.getLogger().severe("Failed to reload configuration: " + exception.getMessage());
            return false;
        }
    }

    public RestartScheduler getRestartScheduler() {
        return this.restartScheduler;
    }

    public NotificationService getNotificationService() {
        return this.notificationService;
    }

    private void registerCommands() {
        final PluginCommand command = Objects.requireNonNull(this.getCommand("ybvautorestart"));
        final AutoRestartCommand autoRestartCommand = new AutoRestartCommand(this);
        command.setExecutor(autoRestartCommand);
        command.setTabCompleter(autoRestartCommand);
    }
}
