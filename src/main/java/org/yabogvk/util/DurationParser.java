package org.yabogvk.util;

import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("(\\d+)([dhmsDHMS])");

    private DurationParser() {
    }

    public static Duration parse(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Duration cannot be empty.");
        }

        final Matcher matcher = TOKEN_PATTERN.matcher(rawValue.trim());
        final Set<Character> usedUnits = new HashSet<>();
        Duration result = Duration.ZERO;
        int matchedLength = 0;

        while (matcher.find()) {
            if (matcher.start() != matchedLength) {
                throw new IllegalArgumentException("Invalid duration format.");
            }

            final long value = Long.parseLong(matcher.group(1));
            final char unit = Character.toLowerCase(matcher.group(2).charAt(0));
            if (!usedUnits.add(unit)) {
                throw new IllegalArgumentException("Duration units cannot repeat.");
            }

            result = result.plus(switch (unit) {
                case 'd' -> Duration.ofDays(value);
                case 'h' -> Duration.ofHours(value);
                case 'm' -> Duration.ofMinutes(value);
                case 's' -> Duration.ofSeconds(value);
                default -> throw new IllegalArgumentException("Unsupported duration unit: " + unit);
            });
            matchedLength = matcher.end();
        }

        if (matchedLength != rawValue.trim().length()) {
            throw new IllegalArgumentException("Invalid duration format.");
        }

        if (result.isZero() || result.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive.");
        }

        return result;
    }
}
