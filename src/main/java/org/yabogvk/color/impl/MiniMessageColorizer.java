package org.yabogvk.color.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.yabogvk.color.Colorizer;

public final class MiniMessageColorizer implements Colorizer {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    @Override
    public String colorize(@Nullable final String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Component component = MINI_MESSAGE.deserialize(message);
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }
}
