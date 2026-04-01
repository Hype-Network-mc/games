package dev.emortal.minestom.lobby.features;

import dev.emortal.minestom.lobby.util.entity.BetterEntity;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public final class EmortalRoomFeature implements LobbyFeature {
    private static final Pos EMORTAL_POS = new Pos(-15.5, 70.3, 1.5, 0, 0);

    private static final PlayerSkin EMORTAL_SKIN = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTY5NDcxODU0NjE0MywKICAicHJvZmlsZUlkIiA6ICI3YmQ1YjQ1OTFlNmI0NzUzODI3NDFmYmQyZmU5YTRkNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJlbW9ydGFsZGV2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q1YmQ1MDViMTBkM2I2YWZjOGY3NTI1OGIwMWE3YzQwMjFjNjFkODFkMjA1M2I4MDg4ZWUyYjhjMTA0NDE4OTMiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzNDBjMGUwM2RkMjRhMTFiMTVhOGIzM2MyYTdlOWUzMmFiYjIwNTFiMjQ4MWQwYmE3ZGVmZDYzNWNhN2E5MzMiCiAgICB9CiAgfQp9",
            "ghORZRfpTCHolZ2RNjjeSM/i5zVqoD7cGOoTONE+f/d376DylqLM3U57/0ZbpvK4qpUy6sk/hRBtqG777BKcEQrILQNSo7EWMrqjbhhJcbI4MudlT/2Jh8/jmjV8qnCD1yXfjn+lK4vueu7mQx0E4zjvRo9oQ55nfBUhUh4XCQFIjoDwjrH+Ndk9dinX1sTVvHv2hP8gwkvpL0U3VaQypfjeWzjwwAJUAGAYO+iJNhcKk3rV00gQ+Dyda5v64sadsL50R5fXfFr89UeoRt7XprGngws27+F30AG6Bj+9/pEyZAhV2SaXeHDm50UUPByQ0bhoLcuN6FT6n+ev7HGn/NB3Q4b+Zi95meM+ePiOwwooO1DtfODd4I8Gkkn6obsiZnY/iLdqPS50KtCmLmyNq0ZdOKXAQ+UmhsRbDDJYz+D1n4syXEZl33h1HqBUm7CuRPj9ow6DvSkdA6s5ZCgsopP6xfRjXDMvBGf7cNriqaxq1swOa+jAirUF9Tq6zGo7+yacAwfeETnmmyfvp7CwED3GkHmPcn0hofw7ycOotd5jvTr1NJD3fB1y5cTR0S/1sf1gusdBXquXGkOX3aN4YiLoqPESuQ1FPqNs7NBLMIxTEFuM3meAMRKX5PzrTBJ24USgiEClTJiPJFDTWr1s1sID3+SEWKFv3dkt1KMYJ4k="
    );

    @Override
    public void register(@NotNull Instance instance) {
        BetterEntity emortalNpc = new BetterEntity(EntityType.CAT);
        emortalNpc.set(DataComponents.CAT_VARIANT, CatVariant.RED);
        emortalNpc.setTicking(false);
//        emortalNpc.editEntityMeta(MannequinMeta.class, meta -> {
//            meta.setProfile(new ResolvableProfile(EMORTAL_SKIN));
//        });
        emortalNpc.setInstance(instance, EMORTAL_POS);

        BetterEntity seatEntity = new BetterEntity(EntityType.TEXT_DISPLAY);
        seatEntity.setTicking(false);
        seatEntity.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setScale(Vec.ZERO);
            meta.setBackgroundColor(0);
        });
        seatEntity.setInstance(instance, EMORTAL_POS);

        seatEntity.addPassenger(emortalNpc);
    }
}
