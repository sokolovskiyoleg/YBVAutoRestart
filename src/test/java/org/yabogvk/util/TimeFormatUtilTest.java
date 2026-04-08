package org.yabogvk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.yabogvk.config.TimeFormatConfiguration;
import org.yabogvk.config.TimeUnitFormat;

class TimeFormatUtilTest {

    private static final TimeFormatConfiguration CONFIGURATION = new TimeFormatConfiguration(
        new TimeUnitFormat("секунду", "секунды", "секунд"),
        new TimeUnitFormat("минуту", "минуты", "минут"),
        new TimeUnitFormat("час", "часа", "часов"),
        new TimeUnitFormat("день", "дня", "дней"),
        " "
    );

    @Test
    void shouldFormatSingleUnitsWithRussianForms() {
        assertEquals("1 секунду", TimeFormatUtil.formatDuration(1, CONFIGURATION));
        assertEquals("2 секунды", TimeFormatUtil.formatDuration(2, CONFIGURATION));
        assertEquals("5 секунд", TimeFormatUtil.formatDuration(5, CONFIGURATION));
        assertEquals("1 минуту", TimeFormatUtil.formatDuration(60, CONFIGURATION));
        assertEquals("2 минуты", TimeFormatUtil.formatDuration(120, CONFIGURATION));
        assertEquals("5 минут", TimeFormatUtil.formatDuration(300, CONFIGURATION));
        assertEquals("1 час", TimeFormatUtil.formatDuration(3600, CONFIGURATION));
        assertEquals("2 часа", TimeFormatUtil.formatDuration(7200, CONFIGURATION));
        assertEquals("5 часов", TimeFormatUtil.formatDuration(18000, CONFIGURATION));
        assertEquals("1 день", TimeFormatUtil.formatDuration(86400, CONFIGURATION));
        assertEquals("2 дня", TimeFormatUtil.formatDuration(172800, CONFIGURATION));
        assertEquals("5 дней", TimeFormatUtil.formatDuration(432000, CONFIGURATION));
    }

    @Test
    void shouldHandleRussianEdgeCases() {
        assertEquals("11 секунд", TimeFormatUtil.formatDuration(11, CONFIGURATION));
        assertEquals("12 минут", TimeFormatUtil.formatDuration(12 * 60L, CONFIGURATION));
        assertEquals("14 часов", TimeFormatUtil.formatDuration(14 * 3600L, CONFIGURATION));
        assertEquals("21 секунду", TimeFormatUtil.formatDuration(21, CONFIGURATION));
        assertEquals("22 секунды", TimeFormatUtil.formatDuration(22, CONFIGURATION));
        assertEquals("25 секунд", TimeFormatUtil.formatDuration(25, CONFIGURATION));
    }

    @Test
    void shouldFormatCombinedDuration() {
        assertEquals("1 час 30 минут", TimeFormatUtil.formatDuration(5400, CONFIGURATION));
        assertEquals("2 дня 5 часов 10 минут", TimeFormatUtil.formatDuration(191400, CONFIGURATION));
    }
}
