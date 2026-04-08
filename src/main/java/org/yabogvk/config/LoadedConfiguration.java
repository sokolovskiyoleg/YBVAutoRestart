package org.yabogvk.config;

import java.util.List;
import java.util.Map;
import org.yabogvk.action.ScheduledAction;
import org.yabogvk.scheduler.ScheduleEntry;

public record LoadedConfiguration(
    List<ScheduleEntry> scheduleEntries,
    Map<Long, List<ScheduledAction>> actions,
    int nowCountdownSeconds,
    String prefix,
    Map<String, String> userMessages,
    TimeFormatConfiguration timeFormat
) {
}
