package org.yabogvk.config;

import java.io.File;
import java.util.HashMap;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yabogvk.action.ActionParser;
import org.yabogvk.action.ScheduledAction;
import org.yabogvk.scheduler.ScheduleEntry;

public final class ConfigManager {

    private final JavaPlugin plugin;

    public ConfigManager(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void ensureDefaultFiles() {
        this.plugin.saveDefaultConfig();
        this.saveResourceIfMissing("messages.yml");
    }

    public LoadedConfiguration load() {
        this.plugin.reloadConfig();

        final FileConfiguration config = this.plugin.getConfig();
        final FileConfiguration messages = YamlConfiguration.loadConfiguration(
            new File(this.plugin.getDataFolder(), "messages.yml")
        );

        final List<ScheduleEntry> scheduleEntries = this.loadScheduleEntries(config.getStringList("schedule.restarts"));
        final Map<Long, List<ScheduledAction>> actions = new ActionParser(this.plugin).parse(config.getStringList("actions"));

        if (actions.isEmpty()) {
            this.plugin.getLogger().warning("No valid actions were found. Countdown will run without executing notifications or restart commands.");
        }

        return new LoadedConfiguration(
            List.copyOf(scheduleEntries),
            Map.copyOf(actions),
            Math.max(1, config.getInt("admin.now-countdown-seconds", 10)),
            this.loadPrefix(config, messages),
            this.loadUserMessages(messages)
        );
    }

    private List<ScheduleEntry> loadScheduleEntries(final List<String> rawEntries) {
        final List<ScheduleEntry> entries = new ArrayList<>();

        for (final String rawEntry : rawEntries) {
            try {
                entries.add(this.parseScheduleEntry(rawEntry));
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Skipping invalid schedule entry '" + rawEntry + "': " + exception.getMessage());
            }
        }

        if (entries.isEmpty()) {
            this.plugin.getLogger().warning("No valid schedule entries were found. Auto-restart countdown will stay idle.");
        }

        return entries;
    }

    private String loadPrefix(final FileConfiguration config, final FileConfiguration messages) {
        final ConfigurationSection messageSection = messages.getConfigurationSection("messages");
        if (messageSection != null) {
            return messageSection.getString("prefix", "&d&lYBVAutoRestart&r");
        }

        return config.getString("messages.prefix", "&d&lYBVAutoRestart&r");
    }

    private Map<String, String> loadUserMessages(final FileConfiguration messages) {
        final ConfigurationSection section = messages.getConfigurationSection("messages.templates");
        if (section == null) {
            return Map.of();
        }

        final Map<String, String> loadedMessages = new HashMap<>();
        this.flattenMessages(section, "", loadedMessages);
        return Map.copyOf(loadedMessages);
    }

    private void flattenMessages(
        final ConfigurationSection section,
        final String prefix,
        final Map<String, String> loadedMessages
    ) {
        for (final String key : section.getKeys(false)) {
            final String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (section.isConfigurationSection(key)) {
                this.flattenMessages(section.getConfigurationSection(key), fullKey, loadedMessages);
                continue;
            }

            loadedMessages.put(fullKey, section.getString(key, ""));
        }
    }

    private ScheduleEntry parseScheduleEntry(final String rawEntry) {
        final String[] parts = rawEntry.trim().split(";");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("expected DAY;HH:MM, DAY;HH;MM, DAILY;HH:MM, or DAILY;HH;MM");
        }

        final String dayToken = parts[0].trim().toUpperCase(Locale.ROOT);
        final boolean daily = dayToken.equals("DAILY");

        final ParsedTime parsedTime = this.parseTime(parts);
        if (daily) {
            return ScheduleEntry.daily(parsedTime.time());
        }

        final DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayToken);
        if (parsedTime.rollToNextDay()) {
            return ScheduleEntry.weekly(dayOfWeek.plus(1L), parsedTime.time());
        }

        return ScheduleEntry.weekly(dayOfWeek, parsedTime.time());
    }

    private ParsedTime parseTime(final String[] parts) {
        if (parts.length == 2) {
            final String[] timeParts = parts[1].trim().split(":");
            if (timeParts.length != 2) {
                throw new IllegalArgumentException("time must be in HH:MM format");
            }
            return this.buildParsedTime(timeParts[0], timeParts[1]);
        }

        return this.buildParsedTime(parts[1].trim(), parts[2].trim());
    }

    private ParsedTime buildParsedTime(final String rawHour, final String rawMinute) {
        final int hour = Integer.parseInt(rawHour);
        final int minute = Integer.parseInt(rawMinute);

        if (hour == 24 && minute == 0) {
            return new ParsedTime(LocalTime.MIDNIGHT, true);
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("time must be between 00:00 and 23:59, or exactly 24:00");
        }

        return new ParsedTime(LocalTime.of(hour, minute), false);
    }

    private void saveResourceIfMissing(final String fileName) {
        final File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }

    private record ParsedTime(LocalTime time, boolean rollToNextDay) {
    }
}
