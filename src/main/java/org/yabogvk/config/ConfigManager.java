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
import org.yabogvk.action.ScheduledActionType;
import org.yabogvk.color.ColorizerProvider;
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
        ColorizerProvider.init(config.getConfigurationSection("formatting"));

        final List<ScheduleEntry> scheduleEntries = this.loadScheduleEntries(config.getStringList("schedule.restarts"));
        final Map<Long, List<ScheduledAction>> actions = new ActionParser(this.plugin).parse(config.getStringList("actions"));

        if (actions.isEmpty()) {
            this.plugin.getLogger().warning("No valid actions were found. Countdown will run without executing notifications or restart commands.");
        }

        final String prefix = this.loadPrefix(config, messages);
        final Map<String, String> userMessages = this.loadUserMessages(messages);

        this.warnAboutLegacyFormattingInMiniMessageMode(prefix, userMessages, actions);

        return new LoadedConfiguration(
            List.copyOf(scheduleEntries),
            Map.copyOf(actions),
            Math.max(1, config.getInt("admin.now-countdown-seconds", 10)),
            prefix,
            userMessages,
            this.loadTimeFormat(config)
        );
    }

    private TimeFormatConfiguration loadTimeFormat(final FileConfiguration config) {
        return new TimeFormatConfiguration(
            this.loadTimeUnitFormat(config, "format.second", "секунду", "секунды", "секунд"),
            this.loadTimeUnitFormat(config, "format.minute", "минуту", "минуты", "минут"),
            this.loadTimeUnitFormat(config, "format.hour", "час", "часа", "часов"),
            this.loadTimeUnitFormat(config, "format.day", "день", "дня", "дней"),
            config.getString("format.splitter", " ")
        );
    }

    private TimeUnitFormat loadTimeUnitFormat(
        final FileConfiguration config,
        final String path,
        final String defaultOne,
        final String defaultFew,
        final String defaultMany
    ) {
        return new TimeUnitFormat(
            config.getString(path + ".one", defaultOne),
            config.getString(path + ".few", defaultFew),
            config.getString(path + ".many", defaultMany)
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

    private void warnAboutLegacyFormattingInMiniMessageMode(
        final String prefix,
        final Map<String, String> userMessages,
        final Map<Long, List<ScheduledAction>> actions
    ) {
        if (!ColorizerProvider.isMiniMessage()) {
            return;
        }

        if (this.looksLikeLegacyFormatting(prefix)) {
            this.plugin.getLogger().warning("messages.prefix looks like legacy formatting while formatting.mode=minimessage.");
        }

        for (final Map.Entry<String, String> entry : userMessages.entrySet()) {
            if (this.looksLikeLegacyFormatting(entry.getValue())) {
                this.plugin.getLogger().warning("messages.templates." + entry.getKey() + " looks like legacy formatting while formatting.mode=minimessage.");
            }
        }

        for (final Map.Entry<Long, List<ScheduledAction>> entry : actions.entrySet()) {
            for (final ScheduledAction action : entry.getValue()) {
                if (action.type() == ScheduledActionType.MESSAGE || action.type() == ScheduledActionType.ACTIONBAR) {
                    if (this.looksLikeLegacyFormatting(action.payload())) {
                        this.plugin.getLogger().warning(
                            "Action at time:" + entry.getKey() + " of type " + action.type().name().toLowerCase(Locale.ROOT)
                                + " looks like legacy formatting while formatting.mode=minimessage."
                        );
                    }
                }
            }
        }
    }

    private boolean looksLikeLegacyFormatting(final String value) {
        return value != null && value.matches(".*&[0-9a-fk-orA-FK-OR].*");
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
