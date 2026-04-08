package org.yabogvk.config;

public record TimeFormatConfiguration(
    TimeUnitFormat second,
    TimeUnitFormat minute,
    TimeUnitFormat hour,
    TimeUnitFormat day,
    String splitter
) {
}
