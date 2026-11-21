package dev.nullremnant.event;

import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class HorrorDirector {
    private HorrorDirector() {}
    public static void tick(ServerWorld world) {
        RemnantState state = StateAccess.get(world);
        long time = world.getTime();
        if (time % 1200 != 0) return;
        int day = (int)(time / 24000L);
        state.setDay(Math.max(state.getDay(), day));
        state.updateStage();
        if (world.getPlayers().isEmpty()) return;
        Random random = world.getRandom();
        int stage = state.getStage();
        int chance = switch(stage) { case 0 -> 0; case 1 -> 8; case 2 -> 18; case 3 -> 30; case 4 -> 42; default -> 55; };
        if (random.nextInt(100) >= chance) return;
        ServerPlayerEntity player = world.getPlayers().get(random.nextInt(world.getPlayers().size()));
        int event = random.nextInt(8);
        switch(event) {
            case 0 -> whisper(player, stage);
            case 1 -> falseMessage(player, stage);
            case 2 -> distantFootsteps(player, world);
            case 3 -> mark(world, player, state);
            case 4 -> shadowWarning(player);
            case 5 -> memoryMessage(player, state);
            case 6 -> emptySilence(player);
            default -> proximityHint(player, stage);
        }
        state.setLastEvent(time);
    }
    private static void whisper(ServerPlayerEntity p, int stage) {
        String[] lines = {"Did you hear that?","Don't turn around.","I remember this place.","You were not alone.","It is closer now."};
        p.sendMessage(Text.literal("§8[§kNULL§r§8] §7" + lines[Math.min(stage, lines.length-1)]), false);
    }
    private static void falseMessage(ServerPlayerEntity p, int stage) {
        String line = stage < 3 ? "§7Something moved beyond your sight." : "§cThe world is trying to remember you.";
        p.sendMessage(Text.literal(line), false);
    }
    private static void distantFootsteps(ServerPlayerEntity p, ServerWorld w) {
        p.sendMessage(Text.literal("§8You hear footsteps, but the ground is still."), false);
    }
    private static void mark(ServerWorld w, ServerPlayerEntity p, RemnantState state) {
        state.setAwareness(Math.min(100, state.getAwareness()+2));
        p.sendMessage(Text.literal("§7A cold feeling settles behind your neck."), false);
    }
    private static void shadowWarning(ServerPlayerEntity p) { p.sendMessage(Text.literal("§8Something briefly blocks the light."), false); }
    private static void memoryMessage(ServerPlayerEntity p, RemnantState state) {
        p.sendMessage(Text.literal("§7" + (state.getDay() > 0 ? "You have been here before." : "This world feels familiar.")), false);
    }
    private static void emptySilence(ServerPlayerEntity p) { p.sendMessage(Text.literal("§8The silence feels deliberate."), false); }
    private static void proximityHint(ServerPlayerEntity p, int stage) { p.sendMessage(Text.literal(stage >= 4 ? "§cIt is standing where you cannot see." : "§7You feel watched."), false); }
}
