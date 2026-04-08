package org.yabogvk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationParserTest {

    @Test
    void shouldParseComposedDuration() {
        assertEquals(Duration.ofHours(1).plusMinutes(30), DurationParser.parse("1h30m"));
        assertEquals(Duration.ofMinutes(2).plusSeconds(10), DurationParser.parse("2m10s"));
        assertEquals(Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4), DurationParser.parse("1d2h3m4s"));
    }

    @Test
    void shouldRejectRepeatedUnits() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("1h2h"));
    }

    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("10"));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("m10"));
        assertThrows(IllegalArgumentException.class, () -> DurationParser.parse("0s"));
    }
}
