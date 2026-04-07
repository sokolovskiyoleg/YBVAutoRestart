package org.yabogvk.action;

public record ScheduledAction(long triggerTimeSeconds, ScheduledActionType type, String payload) {
}
