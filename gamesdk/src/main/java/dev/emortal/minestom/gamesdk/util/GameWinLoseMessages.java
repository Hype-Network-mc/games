package dev.emortal.minestom.gamesdk.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class GameWinLoseMessages {

    private static final List<String> VICTORY = List.of(
            "meow"
    );
    private static final List<String> DEFEAT = List.of(
            "meow"
    );

    public static @NotNull String randomVictory() {
        return random(VICTORY);
    }

    public static @NotNull String randomDefeat() {
        return random(DEFEAT);
    }

    private static @NotNull String random(@NotNull List<String> messages) {
        int length = messages.size();
        int randomIndex = ThreadLocalRandom.current().nextInt(length);
        return messages.get(randomIndex);
    }

    private GameWinLoseMessages() {
    }
}
