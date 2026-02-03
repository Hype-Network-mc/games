package dev.emortal.minestom.lobby.util;

import net.hollowcube.polar.PolarDataConverter;
import org.jetbrains.annotations.NotNull;

public class PolarChainFix implements PolarDataConverter {

    @Override
    public void convertBlockPalette(@NotNull String[] palette, int fromVersion, int toVersion) {

        for (int i = 0; i < palette.length; i++) {
            String s = palette[i];

            if (s.contains("minecraft:chain")) {
//                System.out.println(s);
                palette[i] = s.replace("minecraft:chain", "minecraft:iron_chain");
            }
        }
    }
}
