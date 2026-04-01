package dev.emortal.minestom.lobby.features;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public final class ArmorStandsFeature implements LobbyFeature {
    private static final Tag<Boolean> GLASSES_ARMOR_STAND_TAG = Tag.Boolean("glassesArmorStand");
    private static final ItemStack SUNGLASSES_HELMET = ItemStack.builder(Material.LEATHER_HELMET)
            .set(DataComponents.DYED_COLOR, new Color(0, 0, 0))
            .build();

    @Override
    public void register(@NotNull Instance instance) {
        for (int x = -15; x <= -11; x++) {
            this.spawnArmorStand(instance, new Pos(x + 0.5, 67.1875, 24.5, 180f, 0f));
            this.spawnArmorStand(instance, new Pos(x + 0.5, 65, 24.5, 180f, 0f));
        }

        instance.eventNode().addListener(PlayerEntityInteractEvent.class, e -> {
            if (!e.getTarget().hasTag(GLASSES_ARMOR_STAND_TAG)) return;

            e.getPlayer().setHelmet(SUNGLASSES_HELMET);
            e.getPlayer().playSound(Sound.sound(SoundEvent.ENTITY_CAT_AMBIENT, Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        });
    }

    private void spawnArmorStand(@NotNull Instance instance, @NotNull Pos pos) {
        LivingEntity armorStand = new ArmorStandEntity();
        armorStand.setHelmet(SUNGLASSES_HELMET);

        armorStand.setTag(GLASSES_ARMOR_STAND_TAG, true);

        ArmorStandMeta meta = (ArmorStandMeta) armorStand.getEntityMeta();
        meta.setHasArms(true);

        armorStand.setInstance(instance, pos);
    }

    private static final class ArmorStandEntity extends LivingEntity {

        ArmorStandEntity() {
            super(EntityType.ARMOR_STAND);
        }

        @Override
        public void tick(long time) {
            // Do nothing on tick at all
        }
    }
}
