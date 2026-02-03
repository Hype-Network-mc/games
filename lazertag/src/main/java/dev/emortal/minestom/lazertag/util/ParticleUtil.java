package dev.emortal.minestom.lazertag.util;

import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public final class ParticleUtil {

    public static void renderParticleLine(@NotNull PacketGroupingAudience audience, @NotNull Point start, @NotNull Point end, double step) {
        Point current = start;
        double distRemaining = start.distanceSquared(end);

        Vec dir = end.sub(start).asVec().normalize().mul(step);
        double dirLength = dir.lengthSquared();


        while (distRemaining > 0) {
            ParticlePacket packet = new ParticlePacket(Particle.DUST.withProperties(new Color(255, 255, 0), 0.7f), false, true,
                    current.x(), current.y(), current.z(),
                    0f, 0f, 0f,
                    0f, 1);
            audience.sendGroupedPacket(packet);

            distRemaining -= dirLength;
            current = current.add(dir);
        }
    }

    private ParticleUtil() {
    }
}
