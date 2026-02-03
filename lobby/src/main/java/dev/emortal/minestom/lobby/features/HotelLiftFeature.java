package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.LobbyEvents;
import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

public final class HotelLiftFeature implements LobbyFeature {
    private static final Tag<Boolean> PLAYER_TELEPORTING_TAG = Tag.Boolean("teleportingTag");

    private static final Vec BUTTON_POS = new Vec(4, 69, 41);
    private static final Pos LIFT_TELEPORT_POS = new Pos(12, 64, 42);
    private static final Vec DOOR_SCALE = new Vec(1.5, 3, 0.15);
    private static final Pos LEFT_DOOR_POS = new Pos(2.5, 68, 42.01 + 0.15);
    private static final Pos RIGHT_DOOR_POS = new Pos(1.0, 68, 42.01 + 0.15);
    private static final Pos APARTMENT_SPAWN_POS = new Pos(-31.5, 52, 15);
    private static final Pos EXIT_SPAWN_POS = new Pos(2.5, 68.06250, 40, 180f, 0f);

    private static final Title FADE_TITLE = Title.title(
            Component.text("\uE000"),
            Component.empty(),
            Title.Times.times(Duration.ofMillis(1700), Duration.ofMillis(100), Duration.ofMillis(1700))
    );

    @Override
    public void register(@NotNull Instance instance) {
        // Block the door's entrance while the doors are closed
        for (int x = 1; x <= 3; x++) {
            for (int y = 68; y < 70; y++) {
                instance.setBlock(x, y, 42, Block.BARRIER);
            }
        }

        Entity leftDoor = this.createLiftDoor(instance, LEFT_DOOR_POS);
        Entity rightDoor = this.createLiftDoor(instance, RIGHT_DOOR_POS);

        // Button click listener
        instance.eventNode().addListener(PlayerBlockInteractEvent.class, e -> {
            if (e.getBlockPosition().sameBlock(BUTTON_POS)) {
                if (e.getPlayer().hasTag(PLAYER_TELEPORTING_TAG)) return;

                e.getPlayer().setTag(PLAYER_TELEPORTING_TAG, true);
                this.animateLift(e.getPlayer(), leftDoor.getEntityId(), rightDoor.getEntityId());
            }
            if (e.getBlock().compare(Block.BIRCH_DOOR)) {
                if (e.getPlayer().hasTag(PLAYER_TELEPORTING_TAG)) return;

                e.getPlayer().setTag(PLAYER_TELEPORTING_TAG, true);
                e.getPlayer().showTitle(FADE_TITLE);
                e.getPlayer().scheduler().buildTask(() -> this.exit(e.getPlayer())).delay(TaskSchedule.tick(41)).schedule();
            }
        });
    }

    private @NotNull Entity createLiftDoor(@NotNull Instance instance, @NotNull Pos pos) {
        BetterEntity door = new BetterEntity(EntityType.BLOCK_DISPLAY);
        door.setTicking(false);

        BlockDisplayMeta meta = (BlockDisplayMeta) door.getEntityMeta();
        meta.setScale(DOOR_SCALE);
        meta.setTransformationInterpolationDuration(8);
        meta.setBlockState(Block.LIGHT_GRAY_CONCRETE);

        door.setInstance(instance, pos);
        return door;
    }

    public void animateLift(@NotNull Player player, int leftDoorId, int rightDoorId) {
        this.playLiftCallSound(player);
        // Allow the player to enter the lift
        this.changeLiftEntranceBlocks(player, Block.AIR);

        // Start moving the doors
        player.scheduler().submitTask(new CloseDoorAnimationTask(player, leftDoorId, 1));
        player.scheduler().submitTask(new CloseDoorAnimationTask(player, rightDoorId, -1));

        player.scheduler().submitTask(new LiftAnimationTask(player, leftDoorId, rightDoorId));
    }

    private void changeLiftEntranceBlocks(@NotNull Player player, @NotNull Block newBlock) {
        for (int x = 1; x <= 3; x++) {
            for (int y = 68; y < 70; y++) {
                player.sendPacket(new BlockChangePacket(new Vec(x, y, 42), newBlock));
            }
        }
    }

    private void playLiftCallSound(@NotNull Player player) {
        player.playSound(Sound.sound(SoundEvent.BLOCK_BELL_USE, Sound.Source.MASTER, 1f, 1f), new Pos(12, 65, 37));
    }

    private @NotNull EntityMetaDataPacket createDoorMetaPacket(int doorEntityId, double translationX) {
        return new EntityMetaDataPacket(doorEntityId, Map.of(
                MetadataDef.Display.TRANSLATION.index(), Metadata.Vector3(new Vec(translationX, 0, 0)), // translation
                MetadataDef.Display.INTERPOLATION_DELAY.index(), Metadata.VarInt(0) // interpolation delay
        ));
    }

    private void exit(@NotNull Player player) {
        player.teleport(EXIT_SPAWN_POS);
        LobbyEvents.onSpawn(player, player.getInstance(), player.getInstance());
        player.removeTag(PLAYER_TELEPORTING_TAG);
    }

    private final class LiftAnimationTask implements Supplier<TaskSchedule> {

        private final @NotNull Player player;
        private final int leftDoorId;
        private final int rightDoorId;

        private int tick = 0;

        LiftAnimationTask(@NotNull Player player, int leftDoorId, int rightDoorId) {
            this.player = player;
            this.leftDoorId = leftDoorId;
            this.rightDoorId = rightDoorId;
        }

        @Override
        public TaskSchedule get() {
            if (!this.isReadyForTeleportation()) {
                this.tick++;
                return TaskSchedule.tick(1);
            }

            if (this.tick < 100) { // If elevator didn't close before player entered, teleport player to new world
                this.player.getInventory().clear();
                this.player.scheduler().buildTask(() -> this.player.showTitle(FADE_TITLE)).delay(TaskSchedule.tick(25)).schedule();
                this.player.scheduler().buildTask(this::sendToApartment).delay(TaskSchedule.tick(61)).schedule();
            } else {
                this.player.removeTag(PLAYER_TELEPORTING_TAG);

                if (player.getPosition().z() > 42.2) {
                    player.teleport(player.getPosition().withZ(40));
                }
            }

            // Stop the player being able to exit the lift while teleporting
            HotelLiftFeature.this.changeLiftEntranceBlocks(this.player, Block.BARRIER);

            // Close the doors
            player.scheduler().submitTask(new CloseDoorAnimationTask(player, this.leftDoorId, 0));
            player.scheduler().submitTask(new CloseDoorAnimationTask(player, this.rightDoorId, 0));

            return TaskSchedule.stop();
        }

        private boolean isReadyForTeleportation() {
            Pos pos = this.player.getPosition();
            return this.tick > 100 || (pos.x() > 1 && pos.x() < 4 && pos.z() > 43.2);
        }

        private void sendToApartment() {
            this.player.teleport(APARTMENT_SPAWN_POS);
            player.removeTag(PLAYER_TELEPORTING_TAG);
        }
    }

    private final class CloseDoorAnimationTask implements Supplier<TaskSchedule> {

        private final @NotNull Player player;
        private final int doorId;
        private final double translationX;
        private int tick = 0;

        CloseDoorAnimationTask(@NotNull Player player, int doorId, double translationX) {
            this.player = player;
            this.doorId = doorId;
            this.translationX = translationX;
        }

        @Override
        public TaskSchedule get() {
            // Close the doors
            EntityMetaDataPacket doorMeta = HotelLiftFeature.this.createDoorMetaPacket(doorId, translationX);
            this.player.sendPacket(doorMeta);

            if (tick > 60) {
                return TaskSchedule.stop();
            }

            tick++;
            return TaskSchedule.tick(1);
        }
    }
}
