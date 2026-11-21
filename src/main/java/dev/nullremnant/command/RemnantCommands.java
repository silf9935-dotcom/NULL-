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
                    source.sendFeedback(() -> Text.translatable("command.null_remnant.help"), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("lore").executes(context -> {
                    int stage = StateAccess.get(context.getSource().getWorld()).stage();
                    String[] entries = {
                        "lore.null_remnant.stage0",
                        "lore.null_remnant.stage1",
                        "lore.null_remnant.stage2",
                        "lore.null_remnant.stage3",
                        "lore.null_remnant.stage4"
                    };
                    int index = Math.max(0, Math.min(stage, entries.length - 1));
                    context.getSource().sendFeedback(() -> Text.translatable(entries[index]), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    RemnantConfig.load();
                    context.getSource().sendFeedback(() -> Text.translatable(
                        "command.null_remnant.reloaded",
                        RemnantConfig.enabled,
                        RemnantConfig.eventFrequency,
                        RemnantConfig.stalkerFrequency
                    ), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("status").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    RemnantState state = StateAccess.get(world);
                    int entities = world.getEntitiesByType(RemnantEntities.REMNANT, RemnantEntity::isAlive).size();
                    context.getSource().sendFeedback(() -> Text.translatable(
                        "command.null_remnant.status",
                        state.day(),
                        state.stage() + 1,
                        state.fear(),
                        state.awareness(),
                        state.intrusion(),
                        entities,
                        RemnantConfig.enabled
                    ), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("spawn").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (!RemnantConfig.enabled) {
                        source.sendError(Text.translatable("command.null_remnant.disabled"));
                        return 0;
                    }
                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                        source.sendError(Text.translatable("command.null_remnant.player_only"));
                        return 0;
                    }
                    ServerWorld world = source.getWorld();
                    BlockPos spawn = findSafeSpawn(world, player);
                    if (spawn == null) {
                        source.sendError(Text.translatable("command.null_remnant.no_safe_spawn"));
                        return 0;
                    }
                    RemnantEntity entity = RemnantEntities.REMNANT.create(world);
                    if (entity == null) {
                        source.sendError(Text.translatable("command.null_remnant.create_failed"));
                        return 0;
                    }
                    entity.refreshPositionAndAngles(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                        world.getRandom().nextFloat() * 360.0f, 0.0f);
                    entity.setTarget(player);
                    if (!world.spawnEntity(entity)) {
                        source.sendError(Text.translatable("command.null_remnant.spawn_failed"));
                        return 0;
                    }
                    source.sendFeedback(() -> Text.translatable(
                        "command.null_remnant.spawned",
                        spawn.getX(), spawn.getY(), spawn.getZ()
                    ), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("despawn").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    List<? extends RemnantEntity> entities = world.getEntitiesByType(RemnantEntities.REMNANT, entity -> true);
                    entities.forEach(RemnantEntity::discard);
                    int removed = entities.size();
                    String resultKey = removed == 1
                        ? "command.null_remnant.despawned_one"
                        : "command.null_remnant.despawned_many";
                    context.getSource().sendFeedback(() -> Text.translatable(resultKey, removed), true);
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
