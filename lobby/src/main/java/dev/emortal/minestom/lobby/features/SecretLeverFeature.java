package dev.emortal.minestom.lobby.features;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SecretLeverFeature implements LobbyFeature {

    private static final Lever[] LEVERS = new Lever[] {
            new Lever(-20, 65, -8, Direction.SOUTH),
            new Lever(37, 65, 18, Direction.SOUTH),
            new Lever(14, 64, 24, Direction.EAST),
            new Lever(0, 64, -32, Direction.DOWN),
    };

    private static final Tag<List<Integer>> LEVER_INDEXES_TAG = Tag.Integer("leverIndexes").list();

    @Override
    public void register(@NotNull Instance instance) {
        for (Lever lever : LEVERS) {
            lever.place(instance);
        }

        instance.eventNode().addListener(PlayerBlockInteractEvent.class, this::onLeverInteract);
    }

    public void onLeverInteract(PlayerBlockInteractEvent event) {
        int leverIndex = leverIndex(event.getBlockPosition());
        if (leverIndex == -1) return;

        Player player = event.getPlayer();

        List<Integer> leverIndexesTag = player.getTag(LEVER_INDEXES_TAG);
        List<Integer> leverIndexesList = new ArrayList<>(LEVERS.length);
        if (leverIndexesTag != null) leverIndexesList.addAll(leverIndexesTag);

        if (leverIndexesList.contains(leverIndex)) {
            player.sendActionBar(Component.text("You've already found this lever!", NamedTextColor.RED));
            return;
        }

        leverIndexesList.add(leverIndex);

        player.setTag(LEVER_INDEXES_TAG, leverIndexesList);

        player.sendActionBar(
                Component.text()
                        .append(Component.text("You found a secret lever! ", NamedTextColor.AQUA))
                        .append(Component.text("(", NamedTextColor.GRAY))
                        .append(Component.text(leverIndexesList.size(), NamedTextColor.GRAY))
                        .append(Component.text("/", NamedTextColor.GRAY))
                        .append(Component.text(LEVERS.length, NamedTextColor.GRAY))
                        .append(Component.text(")", NamedTextColor.GRAY))
                        .build()
        );

        // if player has found all the levers
        if (LEVERS.length == leverIndexesList.size()) {
//            player.sendActionBar(
//                    Component.text()
//                            .append(Component.text("You hear movement from inside of the library...", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))
//                            .build()
//            );
            player.sendActionBar(
                    Component.text()
                            .append(Component.text("You found all the levers. A door opens...", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))
                            .build()
            );

            player.sendPacket(new BlockChangePacket(ABSSecretFeature.DOOR_TOP_POS, Block.IRON_DOOR.withProperties(Map.of("powered", "true", "half", "upper"))));
            player.sendPacket(new BlockChangePacket(ABSSecretFeature.DOOR_BOTTOM_POS, Block.IRON_DOOR.withProperties(Map.of("powered", "true", "half", "lower"))));
            player.playSound(Sound.sound(SoundEvent.BLOCK_IRON_DOOR_OPEN, Sound.Source.MASTER, 16f, 1f), ABSSecretFeature.DOOR_TOP_POS);
            player.setTag(ABSSecretFeature.DOOR_UNLOCKED_TAG, true);
        }

        player.playSound(Sound.sound(SoundEvent.BLOCK_PISTON_EXTEND, Sound.Source.MASTER, 0.6f, 0.6f), event.getBlockPosition().add(0.5));
        player.playSound(Sound.sound(SoundEvent.BLOCK_LEVER_CLICK, Sound.Source.MASTER, 1f, 1.2f), event.getBlockPosition().add(0.5));
        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1.3f, 2f), event.getBlockPosition().add(0.5));

        LEVERS[leverIndex].flick(player);
    }

    public int leverIndex(Point pos) {
        int i = 0;
        for (Lever lever : LEVERS) {
            if (pos.sameBlock(lever.x(), lever.y(), lever.z())) return i;
            i++;
        }
        return -1;
    }

    private record Lever(int x, int y, int z, Direction direction) {

        public void place(Instance instance) {
            String faceProperty = switch (direction) {
                case UP -> "ceiling";
                case DOWN -> "floor";
                default -> "wall";
            };
            String facingProperty = switch (direction) {
                case UP, DOWN -> "north";
                default -> direction.name().toLowerCase();
            };

            instance.setBlock(x, y, z, Block.LEVER.withProperty("facing", facingProperty).withProperty("face", faceProperty));
        }

        public void flick(Player player) {
            String faceProperty = switch (direction) {
                case UP -> "ceiling";
                case DOWN -> "floor";
                default -> "wall";
            };
            String facingProperty = switch (direction) {
                case UP, DOWN -> "north";
                default -> direction.name().toLowerCase();
            };

            BlockChangePacket packet = new BlockChangePacket(new Vec(x, y, z), Block.LEVER.withProperty("facing", facingProperty).withProperty("face", faceProperty).withProperty("powered", "true").stateId());
            player.sendPacket(packet);
        }

    }
}
