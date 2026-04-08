package org.yabogvk.color;

import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;
import org.yabogvk.color.impl.LegacyColorizer;
import org.yabogvk.color.impl.MiniMessageColorizer;

public final class ColorizerProvider {

    private static Colorizer colorizer = new LegacyColorizer();
    private static String serializer = "LEGACY";

    private ColorizerProvider() {
    }

    public static void init(final ConfigurationSection config) {
        final String serializerType = config == null
            ? "LEGACY"
            : config.getString("serializer", config.getString("mode", "LEGACY")).toUpperCase(Locale.ENGLISH);

        serializer = serializerType;
        colorizer = "MINIMESSAGE".equals(serializerType)
            ? new MiniMessageColorizer()
            : new LegacyColorizer();
    }

    public static Colorizer getColorizer() {
        return colorizer;
    }

    public static boolean isMiniMessage() {
        return "MINIMESSAGE".equals(serializer);
    }
}
