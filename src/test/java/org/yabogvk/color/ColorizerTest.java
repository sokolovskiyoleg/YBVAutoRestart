package org.yabogvk.color;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.yabogvk.color.impl.LegacyColorizer;
import org.yabogvk.color.impl.MiniMessageColorizer;

class ColorizerTest {

    @Test
    void shouldColorizeLegacyText() {
        final Colorizer colorizer = new LegacyColorizer();

        assertEquals("§fРестарт через §d10s§f.", colorizer.colorize("&fРестарт через &d10s&f."));
    }

    @Test
    void shouldColorizeMiniMessageTextToLegacySectionString() {
        final Colorizer colorizer = new MiniMessageColorizer();

        assertEquals("§fРестарт через §d10s§f.", colorizer.colorize("<white>Рестарт через <light_purple>10s</light_purple>.</white>"));
    }
}
