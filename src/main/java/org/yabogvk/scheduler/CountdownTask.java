package org.yabogvk.scheduler;

import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class CountdownTask {

    private final JavaPlugin plugin;
    private RestartScheduler restartScheduler;
    private BukkitTask task;

    public CountdownTask(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void bind(final RestartScheduler restartScheduler) {
        this.restartScheduler = restartScheduler;
    }

    public void start() {
        if (this.task != null) {
            return;
        }

        this.task = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this.restartScheduler::tick, 20L, 20L);
    }

    public void stop() {
        if (this.task == null) {
            return;
        }

        this.task.cancel();
        this.task = null;
    }
}
