package dev.nullremnant.state;

import net.minecraft.server.world.ServerWorld;

public final class StateAccess {
    private StateAccess() {
    }

    public static RemnantState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
            RemnantState::load,
            RemnantState::new,
            "null_remnant"
        );
    }
}
