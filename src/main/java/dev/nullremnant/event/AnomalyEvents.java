package dev.nullremnant.event;

import dev.nullremnant.state.StateAccess;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class AnomalyEvents {
    private AnomalyEvents() {}
    public static void tryTrigger(ServerWorld world, ServerPlayerEntity player) {
        int stage = StateAccess.get(world).getStage();
        if (stage < 2 || world.getRandom().nextInt(stage >= 4 ? 30 : 100) != 0) return;
        int choice = world.getRandom().nextInt(3);
        if (choice == 0) player.sendMessage(Text.literal("§8A shape disappears at the edge of your vision."), false);
        if (choice == 1) player.sendMessage(Text.literal("§7The cave sounds strangely distant."), false);
        if (choice == 2) {
            BlockPos pos = player.getBlockPos().add(0, -1, 0);
            if (world.getBlockState(pos).isOf(Blocks.STONE) && world.getRandom().nextInt(5)==0)
                world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
            player.sendMessage(Text.literal("§8You notice something that was not there."), false);
        }
    }
}
