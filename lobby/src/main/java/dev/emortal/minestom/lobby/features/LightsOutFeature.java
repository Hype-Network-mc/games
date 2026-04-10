package dev.emortal.minestom.lobby.features;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class LightsOutFeature implements LobbyFeature {

    private static final int HEIGHT = 64;
    private static final Point TOP_LEFT = new Vec(9, HEIGHT, -8);
    private static final Point BOTTOM_RIGHT = new Vec(5, HEIGHT, -12);

    private static final boolean[][] SWASTIKA_PATTERN = new boolean[][]{
            {true, true, true, false, true},
            {false, false, true, false, true},
            {true, true, true, true, true},
            {true, false, true, false, false},
            {true, false, true, true, true},
    };

    private static final boolean[][][] BANNED_PATTERNS = new boolean[][][]{
            SWASTIKA_PATTERN,
    };

    // New lobby positions \/
//    private static final Point TOP_LEFT = new Vec(15, HEIGHT, -10);
//    private static final Point BOTTOM_RIGHT = new Vec(11, HEIGHT, -14);

    private static final Direction[] DIRECTIONS = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.UP}; // Y value is ignored, so UP is equivalent to 0,0

    private final boolean[][] lightsOutGrid = new boolean[5][5];
    private final AtomicLong buttonLastClicked = new AtomicLong(0);

    @Override
    public void register(@NotNull Instance instance) {
        this.buildLightsOut(instance);

        instance.eventNode().addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != PlayerHand.MAIN) return;
            Block block = event.getBlock();

            if (block.compare(Block.BIRCH_BUTTON)) {
                this.handleResetButtonClick(event);
            }

            if (block.compare(Block.REDSTONE_LAMP) && inArea(event.getBlockPosition())) {
                this.handleLampClick(event);
            }
        });
    }

    private boolean inArea(BlockVec blockPos) {
        return blockPos.blockX() >= BOTTOM_RIGHT.blockX() && blockPos.blockX() <= TOP_LEFT.blockX()
                && blockPos.blockY() >= BOTTOM_RIGHT.blockY() && blockPos.blockY() <= TOP_LEFT.blockY()
                && blockPos.blockZ() >= BOTTOM_RIGHT.blockZ() && blockPos.blockZ() <= TOP_LEFT.blockZ();
    }

    private void handleLampClick(@NotNull PlayerBlockInteractEvent event) {
        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();

        Point relativePos = event.getBlockPosition().sub(BOTTOM_RIGHT);
        this.lightsOutClick(batch, lightsOutGrid, relativePos.blockX(), relativePos.blockZ());

        batch.apply(event.getInstance(), null);

        event.getPlayer().playSound(Sound.sound(SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_ON, Sound.Source.MASTER, 1f, 1.5f), Sound.Emitter.self());
    }

    private void handleResetButtonClick(@NotNull PlayerBlockInteractEvent event) {
        event.setCancelled(true);
        if (System.currentTimeMillis() - 1500 < this.buttonLastClicked.get()) return;

        this.buttonLastClicked.set(System.currentTimeMillis());

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch(new BatchOption().setSendUpdate(false));
        this.reset(batch);
        batch.apply(event.getInstance(), (_) -> event.getInstance().getChunkAt(BOTTOM_RIGHT).sendChunk());
    }

    private void reset(@NotNull AbsoluteBlockBatch batch) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < 20; i++) {
            this.lightsOutClick(batch, this.lightsOutGrid, rand.nextInt(0, 5), rand.nextInt(0, 5));
        }
    }

    private void buildLightsOut(@NotNull Instance instance) {
        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch();
        for (int x = BOTTOM_RIGHT.blockX(); x <= TOP_LEFT.blockX(); x++) {
            for (int z = BOTTOM_RIGHT.blockZ(); z <= TOP_LEFT.blockZ(); z++) {
                blockBatch.setBlock(x, HEIGHT, z, Block.REDSTONE_LAMP);
            }
        }
        blockBatch.setBlock(10, HEIGHT + 1, -12, Block.BIRCH_BUTTON.withProperty("face", "floor"));

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < 20; i++) {
            this.lightsOutClick(blockBatch, this.lightsOutGrid, rand.nextInt(0, 5), rand.nextInt(0, 5));
        }

        blockBatch.apply(instance, null);
    }

    private void lightsOutClick(@NotNull AbsoluteBlockBatch batch, boolean[][] grid, int x, int y) {
        for (Direction direction : DIRECTIONS) {
            int newX = x + direction.normalX();
            int newY = y + direction.normalZ();
            if (newX >= 5 || newX < 0) continue;
            if (newY >= 5 || newY < 0) continue;

            boolean newValue = !grid[newX][newY];
            grid[newX][newY] = newValue;

            batch.setBlock(BOTTOM_RIGHT.add(newX, 0, newY), Block.REDSTONE_LAMP.withProperty("lit", String.valueOf(newValue)));
        }

        this.handleIfBanned(batch, grid);
    }

    private void handleIfBanned(@NotNull AbsoluteBlockBatch batch, boolean[][] grid) {
        for (boolean[][] bannedPattern : BANNED_PATTERNS) {
            if (this.gridMatches(grid, bannedPattern)) {
                this.reset(batch);
                return;
            }
        }
    }

    private boolean gridMatches(boolean[][] gridA, boolean[][] gridB) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (gridA[x][y] != gridB[x][y]) {
                    return false;
                }
            }
        }

        return true;
    }
}
