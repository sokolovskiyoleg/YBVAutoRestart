package org.yabogvk.scheduler;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

public record ScheduleEntry(boolean daily, DayOfWeek dayOfWeek, LocalTime time) {

    public ScheduleEntry {
        Objects.requireNonNull(time, "time");
    }

    public static ScheduleEntry daily(final LocalTime time) {
        return new ScheduleEntry(true, null, time);
    }

    public static ScheduleEntry weekly(final DayOfWeek dayOfWeek, final LocalTime time) {
        return new ScheduleEntry(false, Objects.requireNonNull(dayOfWeek, "dayOfWeek"), time);
    }
}
