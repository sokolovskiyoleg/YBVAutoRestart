package org.yabogvk.util;

import java.util.ArrayList;
import java.util.List;

public final class TimeFormatUtil {

    private TimeFormatUtil() {
    }

    public static String formatDuration(final long totalSeconds) {
        long seconds = totalSeconds;
        final long days = seconds / 86400;
        seconds %= 86400;
        final long hours = seconds / 3600;
        seconds %= 3600;
        final long minutes = seconds / 60;
        seconds %= 60;

        final List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + "d");
        }
        if (hours > 0) {
            parts.add(hours + "h");
        }
        if (minutes > 0) {
            parts.add(minutes + "m");
        }
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(seconds + "s");
        }

        return String.join(" ", parts);
    }
}
