package org.yabogvk.util;

import java.util.ArrayList;
import java.util.List;
import org.yabogvk.config.TimeFormatConfiguration;
import org.yabogvk.config.TimeUnitFormat;

public final class TimeFormatUtil {

    private TimeFormatUtil() {
    }

    public static String formatDuration(final long totalSeconds, final TimeFormatConfiguration configuration) {
        long seconds = totalSeconds;
        final long days = seconds / 86400;
        seconds %= 86400;
        final long hours = seconds / 3600;
        seconds %= 3600;
        final long minutes = seconds / 60;
        seconds %= 60;

        final List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(formatPart(days, configuration.day()));
        }
        if (hours > 0) {
            parts.add(formatPart(hours, configuration.hour()));
        }
        if (minutes > 0) {
            parts.add(formatPart(minutes, configuration.minute()));
        }
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(formatPart(seconds, configuration.second()));
        }

        return String.join(configuration.splitter(), parts);
    }

    private static String formatPart(final long value, final TimeUnitFormat format) {
        return value + " " + resolveWordForm(value, format);
    }

    private static String resolveWordForm(final long value, final TimeUnitFormat format) {
        final long normalized = Math.abs(value) % 100;
        final long lastDigit = normalized % 10;

        if (normalized >= 11 && normalized <= 14) {
            return format.many();
        }
        if (lastDigit == 1) {
            return format.one();
        }
        if (lastDigit >= 2 && lastDigit <= 4) {
            return format.few();
        }
        return format.many();
    }
}
