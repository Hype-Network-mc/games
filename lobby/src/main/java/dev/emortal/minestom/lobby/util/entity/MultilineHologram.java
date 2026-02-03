package dev.emortal.minestom.lobby.util.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MultilineHologram {

    private final List<Component> names;
    private final List<Entity> entities = new ArrayList<>();

    public MultilineHologram(@NotNull List<Component> names, @NotNull Instance instance, @NotNull Pos pos) {
        this.names = names;

        int i = 0;
        for (Component name : this.names) {
            BetterEntity entity = new BetterEntity(EntityType.TEXT_DISPLAY);
            entity.setTicking(false);

            TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
            meta.setNotifyAboutChanges(false);

            if (i == 0) {
                meta.setBackgroundColor(0);
                meta.setShadow(true);
                meta.setScale(new Vec(1.2, 1.2, 1.2));
            }
            if (i == 1) meta.setScale(new Vec(0.75, 0.75, 0.75));
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setText(name);
            meta.setNotifyAboutChanges(true);

            entity.setInstance(instance, pos.add(0.0, 0.7 + (0.23 * (names.size() - i)), 0.0));
            this.entities.add(entity);

            i++;
        }
    }

    public void remove() {
        for (Entity entity : this.entities) {
            entity.remove();
        }
    }

    public void setLine(int index, Component newName) {
        if (this.names.size() > index) this.names.set(index, newName);

        if (this.entities.size() > index) {
            Entity entity = this.entities.get(index);
            TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
            meta.setText(newName);
        }
    }

    public int size() {
        return this.names.size();
    }

    @Override
    public String toString() {
        return "MultilineHologram{" +
                "entities=" + this.entities +
                ", names=" + this.names +
                '}';
    }
}
