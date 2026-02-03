package dev.emortal.minestom.lobby.emote;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public class EmoteInventory extends Inventory {

    private static final Tag<String> EMOTE_TAG = Tag.String("emoteName");

    public EmoteInventory() {
        super(InventoryType.CHEST_1_ROW, Component.text("Emotes", NamedTextColor.AQUA));

        int slotI = 0;
        for (Emote.Type value : Emote.Type.values()) {
            ItemStack itemStack = ItemStack.builder(Material.DIAMOND)
                    .set(DataComponents.ITEM_NAME, Component.text(value.getFriendlyName(), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                    .set(EMOTE_TAG, value.name())
                    .build();
            this.setItemStack(slotI, itemStack);

            slotI++;
        }

        this.setItemStack(8, ItemStack.builder(Material.BARRIER)
                .set(DataComponents.ITEM_NAME, Component.text("Stop", NamedTextColor.RED))
                .build());

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getPlayer().getOpenInventory() != this) return;
            e.setCancelled(true);
        });

        this.eventNode().addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);

            Player plr = event.getPlayer();
            int slot = event.getSlot();

            if (slot == 8) {
                Emote.stop(plr);
                return;
            }

            ItemStack item = this.getItemStack(slot);
            if (!item.hasTag(EMOTE_TAG)) return;

            Emote.Type type = Emote.Type.valueOf(item.getTag(EMOTE_TAG));
            Emote.stop(plr);
            plr.scheduleNextTick((i) -> {
                Emote.play(plr, type);
                plr.setHeldItemSlot((byte)2);
            });
            plr.closeInventory();
        });

    }

}
