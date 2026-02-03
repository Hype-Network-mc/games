package dev.emortal.minestom.lobby.gadget;

import dev.emortal.minestom.lobby.LobbyTags;
import dev.emortal.minestom.lobby.util.CustomModels;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class Trumpet extends Gadget {
    private static final Tag<Boolean> TRUMPET_TAG = Tag.Boolean("dooter");
    private static final ItemStack ITEM = ItemStack.builder(Material.PHANTOM_MEMBRANE)
            .set(DataComponents.ITEM_NAME, Component.text("Trumpet"))
            .itemModel(CustomModels.TRUMPET.getModelId())
            .set(TRUMPET_TAG, true)
            .build();

    public Trumpet() {
        super("Tooter", ITEM, "trumpet");
    }

    @Override
    protected void onUse(@NotNull Player user, @NotNull Instance instance) {
        this.playTrumpetSound(user, instance);
        this.throwNearbyEntities(user, instance);
    }

    private void playTrumpetSound(@NotNull Player user, @NotNull Instance instance) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        instance.playSound(Sound.sound(Key.key("item.trumpet.doot"), Sound.Source.MASTER, 1F, random.nextFloat(0.8F, 1.2F)), user.getPosition());
    }

    private void throwNearbyEntities(@NotNull Player user, @NotNull Instance instance) {
        for (Entity nearby : instance.getNearbyEntities(user.getPosition(), 8.0)) {
            if (nearby == user) continue;
            if (!nearby.hasTag(LobbyTags.LOBBABLE)) continue;
            if (nearby.getPosition().distanceSquared(user.getPosition()) == 0) continue;

            Vec throwVelocity = this.calculateThrowVelocity(user, nearby);
            nearby.setVelocity(throwVelocity);
        }
    }

    private @NotNull Vec calculateThrowVelocity(@NotNull Player user, @NotNull Entity target) {
        Vec distance = target.getPosition().sub(user.getPosition()).asVec();
        return distance.normalize().mul(60.0).add(0, 6, 0);
    }
}
