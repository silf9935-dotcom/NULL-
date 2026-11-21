package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class HorrorDirector {
    private HorrorDirector() {}

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getPlayers().isEmpty() || world.getTime() % 1200L != 0) return;
        RemnantState state = StateAccess.get(world);
        int stage = state.stage();
        long now = world.getTime();
        if (stage < 1 || now - state.lastHorror() < 1200L) return;
        state.lastHorror(now);
        if (state.lastEvent() == now) return;

        int chance = new int[] {0, 8, 18, 30, 42}[Math.min(stage, 4)];
        int scaledChance = chance * RemnantConfig.eventFrequency / 100;
        if (world.getRandom().nextInt(100) >= scaledChance) return;

        ServerPlayerEntity player = world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
        String[] lines = {"The silence feels deliberate.", "Something moved beyond your sight.",
            "You have been here before.", "Don't turn around.", "It remembers you."};
        player.sendMessage(Text.literal("§8" + lines[world.getRandom().nextInt(lines.length)]), false);
        state.awareness(1);
        state.fear(1);
        state.lastEvent(now);
    }
}
