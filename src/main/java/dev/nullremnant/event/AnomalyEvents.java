package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class AnomalyEvents {
    private AnomalyEvents() {}

    public static void tryTrigger(ServerWorld world, ServerPlayerEntity ignored) {
        if (!RemnantConfig.enabled || !RemnantConfig.subtleAnomalies || world.getTime() % 1200L != 0) return;
        RemnantState state = StateAccess.get(world);
        int stage = state.stage();
        long now = world.getTime();
        if (stage < 2 || now - state.lastAnomaly() < 1200L || state.lastEvent() == now) return;
        state.lastAnomaly(now);

        int chance = stage >= 4 ? 12 : stage == 3 ? 7 : 3;
        int scaledChance = chance * RemnantConfig.eventFrequency / 100;
        if (world.getRandom().nextInt(100) >= scaledChance) return;

        ServerPlayerEntity recipient = world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
        String[] messages = {
            "anomaly.null_remnant.shape",
            "anomaly.null_remnant.cave",
            "anomaly.null_remnant.appeared",
            "anomaly.null_remnant.ground"
        };
        recipient.sendMessage(
            Text.translatable(messages[world.getRandom().nextInt(messages.length)]),
            false
        );
        state.awareness(1);
        state.fear(1);
        state.lastEvent(now);
    }
}
