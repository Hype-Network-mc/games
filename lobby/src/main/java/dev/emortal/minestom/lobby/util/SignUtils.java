package dev.emortal.minestom.lobby.util;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;

import java.util.List;

public class SignUtils {
    /**
     * Gets very angry if fed bland components!! Give them a colour!!!
     * (minestom bug)
     */
    public static CompoundBinaryTag createNBT(boolean hasGlowingText, String colour, List<Component> lines) {
        if (lines.size() > 4) throw new IllegalArgumentException("Max 4 lines on a sign");

        ListBinaryTag.Builder<BinaryTag> linesTag = ListBinaryTag.builder();

        for (Component line : lines) {
            BinaryTag binaryTag = Codec.COMPONENT.encode(Transcoder.NBT, line).orElseThrow();
            linesTag.add(binaryTag);
        }

        return CompoundBinaryTag.builder()
                .putBoolean("has_glowing_text", hasGlowingText)
                .putString("color", colour)
                .put("messages", linesTag.build())
                .build();
    }
}
