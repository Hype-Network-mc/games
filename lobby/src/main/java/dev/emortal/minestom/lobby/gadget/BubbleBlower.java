package dev.emortal.minestom.lobby.gadget;

import dev.emortal.minestom.lobby.util.CustomModels;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public final class BubbleBlower extends Gadget {
    private static final ItemStack ITEM = ItemStack.builder(Material.PHANTOM_MEMBRANE)
            .set(DataComponents.CUSTOM_NAME, Component.text("Bubble Blower").decoration(TextDecoration.ITALIC, false))
            .itemModel(CustomModels.TRUMPET.getModelId())
            .build();

    public BubbleBlower() {
        super("Bubble Blower", ITEM, "bubbleblower");
    }

    @Override
    protected void onUse(@NotNull Player user, @NotNull Instance instance) {
        shootBubble(user);
    }

    private void shootBubble(Player user) {
        Pos eyePos = user.getPosition().add(0, user.getEyeHeight(), 0);
        Vec eyeDir = eyePos.direction();
        user.playSound(Sound.sound(SoundEvent.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, Sound.Source.MASTER, 0.5f, 0.5f), eyePos);

        ParticlePacket particlePacket = new ParticlePacket(Particle.BUBBLE, eyePos, eyeDir, 1f, 0);
        user.sendPacketToViewersAndSelf(particlePacket);
    }

}
