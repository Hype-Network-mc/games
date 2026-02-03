package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public final class TelephoneWiresFeature implements LobbyFeature {
    private static final Point TELEPHONE_WIRE_TOP_POS = new Vec(7.5, 76, 7.5);

    @Override
    public void register(@NotNull Instance instance) {
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(-5.5, 72, 18.5));
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(-13.5, 73, 3.5));
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(19.5, 72, 14.5));
//        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(8.5, 73, -18.5));
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(6.5, 76, 23.5));
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(-11.5, 77, -18.5));
        spawnTelephoneWire(instance, TELEPHONE_WIRE_TOP_POS, new Vec(10.5, 79.7, -26.5));
    }

    private void spawnTelephoneWire(@NotNull Instance instance, @NotNull Point start, @NotNull Point end) {
        BetterEntity telephoneEnd = new BetterEntity(EntityType.CHICKEN);
        telephoneEnd.setInvisible(true);
        telephoneEnd.setTicking(false);

        BetterEntity telephoneStart = new BetterEntity(EntityType.LEASH_KNOT);
        telephoneStart.setTicking(false);
        telephoneEnd.setLeashHolder(telephoneStart);

        telephoneEnd.setInstance(instance, end);
        telephoneStart.setInstance(instance, start);
    }

}
