package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public final class MemorySystem {
    private MemorySystem() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (RemnantConfig.enabled && world instanceof ServerWorld server && player instanceof ServerPlayerEntity serverPlayer) {
                var block = server.getBlockState(hit.getBlockPos());
                if (block.isIn(BlockTags.DOORS) || block.isIn(BlockTags.TRAPDOORS) || block.isIn(BlockTags.FENCE_GATES)) {
                    RemnantState state = StateAccess.get(server);
                    state.recordDoor(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
                    if (state.stage() >= 2 && state.doorInteractions() % 6 == 0) {
                        serverPlayer.sendMessage(Text.literal("§8The sound of the door lingers longer than it should."), false);
                    }
                }
            }
            return ActionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, blockState, blockEntity) -> {
            if (!RemnantConfig.enabled || !(world instanceof ServerWorld server) || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            RemnantState memory = StateAccess.get(server);
            memory.intrusion(1);
            memory.awareness(1);
            int count = memory.intrusion();
            if (count > 0 && count % 128 == 0 && memory.stage() >= 2) {
                String[] lines = {
                    "§8You hear the same sound twice.",
                    "§7Something is learning the rhythm of your hands.",
                    "§8The world remembers what you remove."
                };
                serverPlayer.sendMessage(Text.literal(lines[server.getRandom().nextInt(lines.length)]), false);
            }
            if (count > 0 && count % 256 == 0) memory.memoryAnchor(serverPlayer.getBlockX(), serverPlayer.getBlockY(), serverPlayer.getBlockZ());
        });
    }

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getTime() % 200L != 0 || world.getPlayers().isEmpty()) return;
        RemnantState state = StateAccess.get(world);

        if (state.stage() >= 3 && state.doorInteractions() >= 8 && !state.doorEchoPlaced()) {
            BlockPos rememberedDoor = new BlockPos(state.doorX(), state.doorY(), state.doorZ());
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.getBlockPos().isWithinDistance(rememberedDoor, 7.0)) continue;
                BlockPos echo = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, rememberedDoor.add(2, 0, 1));
                if (world.isChunkLoaded(echo) && world.getBlockState(echo).isAir() && world.getBlockState(echo.down()).isSolid()) {
                    world.setBlockState(echo, Blocks.OAK_SIGN.getDefaultState());
                    if (world.getBlockEntity(echo) instanceof SignBlockEntity sign) {
                        sign.changeText(text -> text.withMessage(0, Text.literal("You opened this.")).withMessage(1, Text.literal("It was listening.")), true);
                        sign.markDirty();
                        world.updateListeners(echo, world.getBlockState(echo), world.getBlockState(echo), 3);
                        state.doorEchoPlaced(true);
                        player.sendMessage(Text.literal("§8A familiar sound returns."), false);
                    }
                }
                break;
            }
        }

        if (state.stage() < 3 || !state.memoryAnchorSet() || state.memoryEchoPlaced()) return;
        BlockPos anchor = new BlockPos(state.memoryX(), state.memoryY(), state.memoryZ());
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.getBlockPos().isWithinDistance(anchor, 6.0)) continue;
            BlockPos place = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, anchor);
            if (world.isChunkLoaded(place) && world.getBlockState(place).isAir() && world.getBlockState(place.down()).isSolid()) {
                world.setBlockState(place, Blocks.OAK_SIGN.getDefaultState());
                if (world.getBlockEntity(place) instanceof SignBlockEntity sign) {
                    sign.changeText(text -> text.withMessage(0, Text.literal("You have been here before.")).withMessage(1, Text.literal("It remembers.")), true);
                    sign.markDirty();
                    world.updateListeners(place, world.getBlockState(place), world.getBlockState(place), 3);
                    state.memoryEchoPlaced(true);
                    player.sendMessage(Text.literal("§8A memory has returned to this place."), false);
                }
            }
            return;
        }
    }
}
