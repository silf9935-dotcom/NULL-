package dev.nullremnant.state;

import net.minecraft.server.world.ServerWorld;

public final class StateAccess {
    private StateAccess() {
    }

    public static RemnantState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
            RemnantState::new,
            RemnantState::load,
            "null_remnant"
        );
    }
}
