package dev.emortal.minestom.lazertag.gun;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public record GunItemInfo(@NotNull Material material, @NotNull ItemRarity rarity, float damage, double distance, int burstAmount, int ammo,
                          long reloadTime, long shootDelay, long burstDelay, double spread, int bullets, @NotNull SoundPlayer soundPlayer) {

    public GunItemInfo(@NotNull Material material, @NotNull ItemRarity rarity, float damage, double distance, int burstAmount, int ammo,
                       long reloadTime, long shootDelay, long burstDelay, double spread, int bullets, @NotNull Sound sound) {
        this(material, rarity, damage, distance, burstAmount, ammo, reloadTime, shootDelay, burstDelay, spread, bullets, (a, pos) -> a.playSound(sound, pos));
    }

    public interface SoundPlayer {
        void play(PacketGroupingAudience audience, Point position);
    }

}