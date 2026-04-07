package org.yabogvk.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActionParser {

    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\[time:(\\d+)]\\s+(\\S+)\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private final JavaPlugin plugin;

    public ActionParser(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<Long, List<ScheduledAction>> parse(final List<String> rawActions) {
        final Map<Long, List<ScheduledAction>> actionsByTime = new LinkedHashMap<>();

        for (final String rawAction : rawActions) {
            final String trimmedAction = rawAction.trim();
            if (trimmedAction.isEmpty()) {
                continue;
            }

            try {
                final ScheduledAction action = this.parseAction(trimmedAction);
                actionsByTime.computeIfAbsent(action.triggerTimeSeconds(), ignored -> new ArrayList<>()).add(action);
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Skipping invalid action '" + rawAction + "': " + exception.getMessage());
            }
        }

        return actionsByTime;
    }

    private ScheduledAction parseAction(final String rawAction) {
        final Matcher matcher = ACTION_PATTERN.matcher(rawAction);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("expected format [time:N] <message|actionbar|sound|command> <payload>");
        }

        final long triggerTimeSeconds = Long.parseLong(matcher.group(1));
        final ScheduledActionType type = this.parseType(matcher.group(2));
        final String payload = matcher.group(3).trim();
        this.validatePayload(type, payload);

        return new ScheduledAction(triggerTimeSeconds, type, payload);
    }

    private ScheduledActionType parseType(final String rawType) {
        try {
            return ScheduledActionType.valueOf(rawType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("unknown action type '" + rawType + "'");
        }
    }

    private void validatePayload(final ScheduledActionType type, final String payload) {
        if (payload.isEmpty()) {
            throw new IllegalArgumentException("payload cannot be empty");
        }

        if (type == ScheduledActionType.SOUND) {
            final String[] parts = payload.split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("sound action must be '<SOUND> <volume> <pitch>'");
            }

            try {
                Float.parseFloat(parts[1]);
                Float.parseFloat(parts[2]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("sound volume and pitch must be numbers");
            }
        }
    }
}
