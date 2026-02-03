package dev.emortal.minestom.lobby.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class SphereUtil {

    public static @NotNull List<WorldBlock> getNearbyBlocks(@NotNull Point pos, Set<Point> blocksInSphere, @NotNull Instance instance,
                                                            @NotNull Predicate<Block> predicate) {
        List<WorldBlock> filteredBlocks = new ArrayList<>();
        for (Point block : blocksInSphere) {
            Point blockPos = block.add(pos);
            Block currentBlock;
            try {
                currentBlock = instance.getBlock(blockPos, Block.Getter.Condition.TYPE);
            } catch (Exception ignored) {
                continue;
            }
            if (!predicate.test(currentBlock)) continue;

            filteredBlocks.add(new WorldBlock(blockPos, currentBlock));
        }

        Collections.shuffle(filteredBlocks);
        return filteredBlocks;
    }

    public static @NotNull Set<Point> getBlocksInSphere(double radius) {
        Set<Point> points = new HashSet<>();

        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    if ((x * x) + (y * y) + (z * z) > radius * radius) continue;
                    points.add(new Vec(x, y, z));
                }
            }
        }

        return points;
    }

    public static List<Byte> getSphereExplosionOffsets(double radius) {
        List<Byte> points = new ArrayList<>();

        for (byte x = (byte) -radius; x <= radius; x++) {
            for (byte y = (byte) -radius; y <= radius; y++) {
                for (byte z = (byte) -radius; z <= radius; z++) {
                    if ((x * x) + (y * y) + (z * z) > radius * radius) continue;

                    points.add(x);
                    points.add(y);
                    points.add(z);
                }
            }
        }

        return points;
    }

    public static @NotNull Set<Point> fibonacciSpherePoints(int points) {
        Set<Point> positions = new HashSet<>();

        double phi = Math.PI * (Math.sqrt(5.0) - 1.0);

        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (points - 1.0)) * 2;
            double yRad = Math.sqrt(1 - y * y);
            double theta = phi * i;

            double x = Math.cos(theta) * yRad;
            double z = Math.sin(theta) * yRad;

            positions.add(new Vec(x, y, z));
        }

        return positions;
    }

    private SphereUtil() {
    }
}
