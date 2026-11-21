package dev.nullremnant.command;

import com.mojang.brigadier.Command;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.entity.RemnantEntities;
import dev.nullremnant.entity.RemnantEntity;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class RemnantCommands {
    private RemnantCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("remnant")
                .then(CommandManager.literal("help").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    source.sendFeedback(() -> Text.literal(
                        "NULL: The Remnant commands\n" +
                        "/remnant lore - read the current world chapter\n" +
                        "/remnant status - show world state (operator)\n" +
                        "/remnant spawn - summon the Remnant safely (operator)\n" +
                        "/remnant despawn - remove Remnant entities in this dimension (operator)\n" +
                        "/remnant reload - reload config/null_remnant.json (operator)"
                    ), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("lore").executes(context -> {
                    int stage = StateAccess.get(context.getSource().getWorld()).stage();
                    String[] entries = {
                        "§8The first world was quiet. Too quiet to be empty.",
                        "§7A shape was seen beyond the trees, then never again.",
                        "§8The signs began appearing near places you had visited.",
                        "§cIt copies the things you build. It cannot understand why.",
                        "§4It believes this world belongs to the one who returns."
                    };
                    int index = Math.max(0, Math.min(stage, entries.length - 1));
                    context.getSource().sendFeedback(() -> Text.literal(entries[index]), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    RemnantConfig.load();
                    context.getSource().sendFeedback(() -> Text.literal(
                        "NULL Remnant config reloaded. enabled=" + RemnantConfig.enabled
                            + ", eventFrequency=" + RemnantConfig.eventFrequency
                            + ", stalkerFrequency=" + RemnantConfig.stalkerFrequency
                    ), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("status").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    RemnantState state = StateAccess.get(world);
                    int entities = world.getEntitiesByType(RemnantEntities.REMNANT, RemnantEntity::isAlive).size();
                    String status = "NULL Remnant | day " + state.day()
                        + " | stage " + state.stage()
                        + " | fear " + state.fear()
                        + " | awareness " + state.awareness()
                        + " | intrusion " + state.intrusion()
                        + " | active entities " + entities
                        + " | enabled " + RemnantConfig.enabled;
                    context.getSource().sendFeedback(() -> Text.literal(status), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("spawn").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayerOrThrow();
                    ServerWorld world = source.getWorld();
                    BlockPos spawn = findSafeSpawn(world, player);
                    if (spawn == null) {
                        source.sendError(Text.literal("Could not find a safe, loaded spawn position near you."));
                        return 0;
                    }
                    RemnantEntity entity = RemnantEntities.REMNANT.create(world);
                    if (entity == null) {
                        source.sendError(Text.literal("Could not create the Remnant entity."));
                        return 0;
                    }
                    entity.refreshPositionAndAngles(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                        world.getRandom().nextFloat() * 360.0f, 0.0f);
                    entity.setTarget(player);
                    if (!world.spawnEntity(entity)) {
                        source.sendError(Text.literal("The Remnant could not be added to this world."));
                        return 0;
                    }
                    source.sendFeedback(() -> Text.literal("Spawned the Remnant at " +
                        spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ() + "."), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("despawn").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    List<RemnantEntity> entities = world.getEntitiesByType(RemnantEntities.REMNANT, entity -> true);
                    entities.forEach(RemnantEntity::discard);
                    int removed = entities.size();
                    context.getSource().sendFeedback(() -> Text.literal(
                        "Removed " + removed + " Remnant entit" + (removed == 1 ? "y" : "ies") + " from this dimension."
                    ), true);
                    return Command.SINGLE_SUCCESS;
                }))
            )
        );
    }

    private static BlockPos findSafeSpawn(ServerWorld world, ServerPlayerEntity player) {
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = world.getRandom().nextDouble() * Math.PI * 2.0;
            int radius = 4 + world.getRandom().nextInt(5);
            int dx = (int) Math.round(Math.cos(angle) * radius);
            int dz = (int) Math.round(Math.sin(angle) * radius);
            int dy = world.getRandom().nextInt(3) - 1;
            BlockPos feet = player.getBlockPos().add(dx, dy, dz);

            if (!world.isChunkLoaded(feet)) continue;
            if (!world.getBlockState(feet).isAir() || !world.getBlockState(feet.up()).isAir()) continue;
            if (!world.getBlockState(feet.down()).isSolidBlock(world, feet.down())) continue;
            if (!world.getBlockState(feet).getFluidState().isEmpty()
                    || !world.getBlockState(feet.up()).getFluidState().isEmpty()) continue;
            if (player.getBlockPos().isWithinDistance(feet, 3.0)) continue;
            return feet;
        }
        return null;
    }
}
