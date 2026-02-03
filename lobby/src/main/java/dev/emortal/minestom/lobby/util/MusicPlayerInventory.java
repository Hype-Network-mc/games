package dev.emortal.minestom.lobby.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PatternValidation")
public class MusicPlayerInventory {

    private static final Tag<@NotNull String> PLAYING_DISC_TAG = Tag.Transient("playingDisc");

    private static MusicPlayerInventory INSTANCE;
    private final Inventory inventory;

    public MusicPlayerInventory() {
        Component inventoryTitle = Component.text("Music Discs", NamedTextColor.BLACK);
        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, inventoryTitle);

        Registries registries = MinecraftServer.process();

        var i = 10;
        for (RegistryKey<@NotNull JukeboxSong> song : registries.jukeboxSong().keys()) {
            if ((i + 1) % 9 == 0) i += 2;

            inventory.setItemStack(i, itemFromJukeboxSong(song));

            i++;
        }

        inventory.setItemStack(49, ItemStack.builder(Material.BARRIER)
                .set(DataComponents.ITEM_NAME, Component.text("Stop", NamedTextColor.RED, TextDecoration.BOLD))
                .build());

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getPlayer().getOpenInventory() != INSTANCE.inventory) return;
            e.setCancelled(true);
        });

        inventory.eventNode().addListener(InventoryPreClickEvent.class, e -> {
            e.setCancelled(true);

            if (e.getClickedItem() == ItemStack.AIR) return;

            String currentlyPlaying = e.getPlayer().getTag(PLAYING_DISC_TAG);
            if (currentlyPlaying != null) {
                e.getPlayer().stopSound(SoundStop.named(Key.key(currentlyPlaying)));
            }
            if (e.getSlot() == 49) return;

            RegistryKey<@NotNull JukeboxSong> songKey = e.getClickedItem().get(DataComponents.JUKEBOX_PLAYABLE);
            JukeboxSong song = registries.jukeboxSong().get(songKey);
            e.getPlayer().playSound(Sound.sound(song.soundEvent(), Sound.Source.RECORD, 1f, 1f), Sound.Emitter.self());
            e.getPlayer().setTag(PLAYING_DISC_TAG, song.soundEvent().name());
        });

        this.inventory = inventory;
    }

    public static Inventory getInventory() {
        if (INSTANCE == null) {
            INSTANCE = new MusicPlayerInventory();
        }

        return INSTANCE.inventory;
    }

    private static ItemStack itemFromJukeboxSong(RegistryKey<@NotNull JukeboxSong> songKey) {
        Registries registries = MinecraftServer.process();
        JukeboxSong song = registries.jukeboxSong().get(songKey);

        return ItemStack.builder(Material.fromKey(song.soundEvent().name().replace(".", "_")))
                .set(DataComponents.JUKEBOX_PLAYABLE, songKey)
                .build();
    }
}