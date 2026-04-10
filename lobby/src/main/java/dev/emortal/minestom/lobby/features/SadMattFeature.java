package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.entity.BetterLivingEntity;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

public final class SadMattFeature implements LobbyFeature {
    private static final Pos MATT_POS = new Pos(-33.65, 65, -28.65, 140f, 50f);

    private static final PlayerSkin MATT_SKIN = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY5Mzg1Mzg0NDcyOSwKICAicHJvZmlsZUlkIiA6ICJhY2ViMzI2ZmRhMTU0NWJjYmYyZjExOTQwYzIxNzgwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RtYXR0dyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kZDBiZDllMGIxNTRiMjkwOWIwNmJiMzIyMjVkOWMwYTczYTQ0NWRiMzYxOTU1OGNlZGQ4OTI0ZjljOTE1YjM3IgogICAgfSwKICAgICJDQVBFIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMzQwYzBlMDNkZDI0YTExYjE1YThiMzNjMmE3ZTllMzJhYmIyMDUxYjI0ODFkMGJhN2RlZmQ2MzVjYTdhOTMzIgogICAgfQogIH0KfQ==",
            "a466ZpFEf8CByL9prOX2ZaKz3qIGYHiLWlFN1KwDwo8DTd3zG5b3ew9mLwE3Mx7qx+R0IMJa2vuoPXTXI09ghIXMEDLwNNsf7DLWMfBzLqiMyuVgKjVOLeVZ4Di0woecklIsNPEUJPTqDawSveRCuQJJaJK+CqejLsIJAC4A3fTNPpG/9jVhg8kPH5jRFxpa2PMTUVzP7t5X1JHqFmkmxh49O4l2NwDD8T2j5UUWyeAjIBxv7Y5TAtViMMCN6CUJICFJC6KNV1R3PXdGyfpALsXGmZk2zp8tEt1+id/gMtTZd9eXksUTt6NZPqwjX2kjOIMo+/b4DTMw929P7e+5QEjmpwduTC4mugjde8tyA5et9dzX+zDsCNamd5i6DVfTXfV1FIkjXYKpb8PThmeYhjbRJh1sgWeZB8b5OFdRckoUYuHZPTHR7BCu3p3Qp7CQ3IEs8qZQxVBUCFwo7od5kEtRnkYis2DFuk3XlvkZ1BOQOD1lbYa6WHSAmfJztbkIaAWpgL7N+bKvmnnmrcAXdUE9zLenI0H312fJ5uoqWl1i2OQmFCUCxb7yyJj+J++ZwGe+ZrljZ6s2sCphQ3yJlbgBZHR5t6FrKanH8Bl+SQCDWMhckJwyxXefgy1IqP2AtPN5uvVCjYSySjWdUyXmYGrItvwq+do71Dl80OTWUs8="
    );

    @Override
    public void register(@NotNull Instance instance) {
        BetterLivingEntity bomNpc = new BetterLivingEntity(EntityType.MANNEQUIN);
        bomNpc.editEntityMeta(MannequinMeta.class, meta -> {
            meta.setProfile(new ResolvableProfile(MATT_SKIN));
            meta.setCustomNameVisible(true);
        });
        bomNpc.set(DataComponents.CUSTOM_NAME, Component.text("Sad Matt"));
        bomNpc.setInstance(instance, MATT_POS);
    }
}
