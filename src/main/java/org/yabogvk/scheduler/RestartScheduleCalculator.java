package org.yabogvk.scheduler;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RestartScheduleCalculator {

    private final List<ScheduleEntry> entries;

    public RestartScheduleCalculator(final List<ScheduleEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public Optional<LocalDateTime> findNextAfter(final LocalDateTime referenceTime) {
        Objects.requireNonNull(referenceTime, "referenceTime");

        return this.entries.stream()
            .map(entry -> this.resolveCandidate(entry, referenceTime))
            .min(Comparator.naturalOrder());
    }

    private LocalDateTime resolveCandidate(final ScheduleEntry entry, final LocalDateTime referenceTime) {
        if (entry.daily()) {
            LocalDateTime candidate = referenceTime.with(entry.time()).withSecond(0).withNano(0);
            if (!candidate.isAfter(referenceTime)) {
                candidate = candidate.plusDays(1);
            }
            return candidate;
        }

        final int current = referenceTime.getDayOfWeek().getValue();
        final int target = entry.dayOfWeek().getValue();
        int daysAhead = target - current;
        if (daysAhead < 0) {
            daysAhead += 7;
        }

        LocalDateTime candidate = referenceTime.plusDays(daysAhead).with(entry.time()).withSecond(0).withNano(0);
        if (!candidate.isAfter(referenceTime)) {
            candidate = candidate.plusWeeks(1);
        }
        return candidate;
    }
}
