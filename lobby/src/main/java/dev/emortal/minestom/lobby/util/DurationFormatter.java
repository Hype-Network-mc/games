package dev.emortal.minestom.lobby.util;

import java.time.Duration;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;

public final class DurationFormatter {

    private DurationFormatter() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static boolean canBeFormatted(@NotNull Duration duration) {
        return duration.minusSeconds(1).isPositive();
    }

    public static @NotNull String format(@NotNull Duration duration, int maxUnits) {
        StringJoiner joiner = new StringJoiner(", ");
        int unitCount = 0;

        int years = (int) duration.toDays() / 365;
        int months = (int) duration.toDays() / 30;
        int days = (int) duration.toDays() % 30;
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        if (years > 0) {
            joiner.add(years + " year" + (years == 1 ? "" : "s"));
            unitCount++;
        }

        if (months > 0) {
            joiner.add(months + " month" + (months == 1 ? "" : "s"));
            unitCount++;
        }

        if (days > 0) {
            joiner.add(days + " day" + (days == 1 ? "" : "s"));
            if (++unitCount == maxUnits) return joiner.toString();
        }

        if (hours > 0) {
            joiner.add(hours + " hour" + (hours == 1 ? "" : "s"));
            if (++unitCount == maxUnits) return joiner.toString();
        }

        if (minutes > 0) {
            joiner.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
            if (++unitCount == maxUnits) return joiner.toString();
        }

        if (seconds > 0) {
            joiner.add(seconds + " second" + (seconds == 1 ? "" : "s"));
        }

        return joiner.toString();
    }

}
