package dev.emortal.minestom.lazertag.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;


public class DisplayEntityUtil {

    public static List<Entity> drawLine(Instance instance, Point pos1, Point pos2, int color, int afterColor, double thickness) {
        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            var entity = new Entity(EntityType.TEXT_DISPLAY);
            entity.setNoGravity(true);
            int finalI = i;
            entity.editEntityMeta(TextDisplayMeta.class, meta -> {
                double distance = pos1.distance(pos2);

                meta.setText(Component.text(" "));
                meta.setBackgroundColor(color);
                meta.setScale(new Vec(distance / 0.125, thickness, 1));

                Quaternionf quat1 = new Quaternionf(new AxisAngle4f((float)Math.toRadians(90), 0f, 1f, 0f));
                Quaternionf quat2 = new Quaternionf(new AxisAngle4f((float)Math.toRadians(finalI * 90), 1f, 0f, 0f));
                Quaternionf quat = quat1.mul(quat2);
                meta.setLeftRotation(new float[] { quat.x, quat.y, quat.z, quat.w });
            });
            entities.add(entity);
            entity.setInstance(instance, lerpVec(pos1, pos2, 0.6).asPos().withLookAt(pos2)).thenRun(() -> {
                entity.scheduler().buildTask(() -> {
                    entity.editEntityMeta(TextDisplayMeta.class, meta -> {
                        meta.setTransformationInterpolationDuration(3);
                        meta.setTransformationInterpolationStartDelta(0);
                        meta.setBackgroundColor(afterColor);
                    });
                }).delay(TaskSchedule.tick(1)).schedule();
            });
        }
        return entities;
    }

    private static Vec lerpVec(Point a, Point b, double f) {
        return new Vec(
                lerp(a.x(), b.x(), f),
                lerp(a.y(), b.y(), f),
                lerp(a.z(), b.z(), f)
        );
    }

    private static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

}
