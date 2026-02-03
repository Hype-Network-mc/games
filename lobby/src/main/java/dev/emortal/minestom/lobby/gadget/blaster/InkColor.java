package dev.emortal.minestom.lobby.gadget.blaster;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public enum InkColor {

    ORANGE(Block.ORANGE_WOOL, Material.ORANGE_DYE),
    MAGENTA(Block.MAGENTA_WOOL, Material.MAGENTA_DYE),
    LIGHT_BLUE(Block.LIGHT_BLUE_WOOL, Material.LIGHT_BLUE_DYE),
    YELLOW(Block.YELLOW_WOOL, Material.YELLOW_DYE),
    LIME(Block.LIME_WOOL, Material.LIME_DYE),
    PINK(Block.PINK_WOOL, Material.PINK_DYE),
    CYAN(Block.CYAN_WOOL, Material.CYAN_DYE),
    PURPLE(Block.PURPLE_WOOL, Material.PURPLE_DYE),
    BLUE(Block.BLUE_WOOL, Material.BLUE_DYE),
    RED(Block.RED_WOOL, Material.RED_DYE);

    private final @NotNull Block wool;
    private final @NotNull Material dye;

    InkColor(@NotNull Block wool, @NotNull Material dye) {
        this.wool = wool;
        this.dye = dye;
    }

    public @NotNull Block getWool() {
        return wool;
    }

    public @NotNull Material getDye() {
        return dye;
    }
}
