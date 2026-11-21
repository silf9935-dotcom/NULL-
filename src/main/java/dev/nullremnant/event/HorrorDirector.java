package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;

public final class HorrorDirector {
    private static final String[] MESSAGE_KEYS = {
        "horror.null_remnant.silence",
        "horror.null_remnant.movement",
        "horror.null_remnant.return",
        "horror.null_remnant.turn",
        "horror.null_remnant.remembers"
    };

    private HorrorDirector() {
    }

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getPlayers().isEmpty() || world.getTime() % 1200L != 0L) return;

        RemnantState state = StateAccess.get(world);
        int stage = state.stage();
        long now = world.getTime();
        if (stage < 1 || now - state.lastHorror() < 1200L || state.lastEvent() == now) return;
        state.lastHorror(now);

        int baseChance = new int[] {0, 8, 18, 30, 42}[Math.min(stage, 4)];
        int pressure = Math.min(10, state.awareness() / 60 + state.fear() / 40);
        int scaledChance = Math.min(100,
            (baseChance + pressure) * RemnantConfig.eventFrequency / 100);
        if (world.getRandom().nextInt(100) >= scaledChance) return;

        List<ServerPlayerEntity> players = world.getPlayers();
        ServerPlayerEntity player = players.get(world.getRandom().nextInt(players.size()));
        String key = MESSAGE_KEYS[world.getRandom().nextInt(MESSAGE_KEYS.length)];
        player.sendMessage(Text.translatable(key), false);
        state.awareness(1);
        state.fear(1);
        state.lastEvent(now);
    }
}
