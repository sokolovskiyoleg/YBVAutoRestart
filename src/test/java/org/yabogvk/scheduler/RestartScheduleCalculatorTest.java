package org.yabogvk.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class RestartScheduleCalculatorTest {

    @Test
    void shouldResolveNextDailyRestartAfterReferenceTime() {
        final RestartScheduleCalculator calculator = new RestartScheduleCalculator(
            java.util.List.of(ScheduleEntry.daily(LocalTime.of(6, 0)))
        );

        final LocalDateTime reference = LocalDateTime.of(2026, 4, 7, 5, 30);
        final LocalDateTime next = calculator.findNextAfter(reference).orElseThrow();

        assertEquals(LocalDateTime.of(2026, 4, 7, 6, 0), next);
    }

    @Test
    void shouldRollWeeklyRestartToNextWeekWhenSlotAlreadyPassed() {
        final RestartScheduleCalculator calculator = new RestartScheduleCalculator(
            java.util.List.of(ScheduleEntry.weekly(DayOfWeek.TUESDAY, LocalTime.of(12, 0)))
        );

        final LocalDateTime reference = LocalDateTime.of(2026, 4, 7, 13, 0);
        final LocalDateTime next = calculator.findNextAfter(reference).orElseThrow();

        assertEquals(LocalDateTime.of(2026, 4, 14, 12, 0), next);
    }
}
