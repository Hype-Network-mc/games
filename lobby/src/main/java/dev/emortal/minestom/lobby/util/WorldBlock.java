package dev.emortal.minestom.lobby.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A block in a world, that has a position and the block.
 */
public record WorldBlock(@NotNull Point position, @NotNull Block block) {
}
