package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.commands.TrainCommand;
import dev.emortal.minestom.lobby.util.entity.SeatEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.client.play.ClientInputPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SeatingFeature implements LobbyFeature {
    public static final Map<Point, Entity> armorStandSeats = new HashMap<>();

    @Override
    public void register(@NotNull Instance instance) {
        instance.eventNode().addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != PlayerHand.MAIN) return;
            if (!event.getBlock().name().toLowerCase().contains("stair")) return;
            this.handleStairClick(event.getPlayer(), event.getInstance(), event.getBlock(), event.getBlockPosition());
        });

        // Dismount event
        instance.eventNode().addListener(PlayerPacketEvent.class, event -> {
            if (!(event.getPacket() instanceof ClientInputPacket packet)) return;
            if (!packet.shift()) return; // shift = sneak = dismount

            Player player = event.getPlayer();
            Entity vehicle = player.getVehicle();
            if (vehicle != null && !(vehicle instanceof Player)) {
                if (vehicle.hasTag(TrainCommand.TRAIN_TAG)) {
                    if (vehicle.getTag(TrainCommand.TRAIN_TAG) == player.getUuid()) vehicle.removePassenger(player);
                    else return;
                }
                vehicle.removePassenger(player);
            }
        });
    }

    private void handleStairClick(@NotNull Player player, @NotNull Instance instance, @NotNull Block clicked, @NotNull Point clickedPos) {
        if (player.getVehicle() != null) return;
        if (Objects.equals(clicked.getProperty("half"), "top")) return;
        if (!instance.getBlock(clickedPos.add(0, 1, 0), Block.Getter.Condition.TYPE).compare(Block.STRUCTURE_VOID)) return;

        if (armorStandSeats.containsKey(clickedPos)) {
            player.sendActionBar(Component.text("You can't sit on someone's lap", NamedTextColor.RED));
            return;
        }


        SeatEntity seatEntity = new SeatEntity(() -> armorStandSeats.remove(clickedPos));
        Point spawnPos = clickedPos.add(0.5, 0.3, 0.5);
//        float yaw = switch (event.getBlock().getProperty("facing")) {
//            case "east": yield 90f;
//            case "south": yield 180f;
//            case "west": yield -90f;
//            default:
//                yield 0f;
//        };

        //new Pos(spawnPos, yaw, 0f)
        seatEntity.setInstance(instance, spawnPos).thenRun(() -> seatEntity.addPassenger(player));

        armorStandSeats.put(clickedPos, seatEntity);
    }
}
