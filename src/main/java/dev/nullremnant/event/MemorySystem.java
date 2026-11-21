package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
                    if (state.stage() >= 2 && state.doorRepeatCount() >= 4 && (state.doorRepeatCount() == 4 || state.doorRepeatCount() % 3 == 0)) {
                        int visits = state.doorRepeatCount();
                        String echo = visits >= 9 && state.stage() >= 4
                            ? "§4This door has become part of the pattern."
                            : visits >= 6 && state.stage() >= 3
                                ? "§8You remember opening this. It remembers you."
                                : "§8The sound of the door lingers longer than it should.";
                        serverPlayer.sendMessage(Text.literal(echo), false);
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
            if (blockEntity == null && !blockState.isAir() && blockState.getFluidState().isEmpty()
                    && blockState.isSolidBlock(world, pos)) {
                String blockId = Registries.BLOCK.getId(blockState.getBlock()).toString();
                if (isMimicableBlock(blockId)) memory.rememberBlock(blockId, pos.getX(), pos.getY(), pos.getZ());
            }
            int count = memory.intrusion();
            if (count > 0 && count % 128 == 0 && memory.stage() >= 2) {
                String[] lines = memory.stage() >= 4
                    ? new String[] {
                        "§4Every block removed makes the shape clearer.",
                        "§8It is learning what you leave behind.",
                        "§7Your hands are teaching it how to build."
                    }
                    : memory.stage() >= 3
                        ? new String[] {
                            "§8You hear the same sound twice.",
                            "§7Something is learning the rhythm of your hands.",
                            "§8The world remembers what you remove."
                        }
                        : new String[] {
                            "§7A familiar sound follows your next movement.",
                            "§8The world remembers what you remove.",
                            "§7Something is learning the rhythm of your hands."
                        };
                serverPlayer.sendMessage(Text.literal(lines[server.getRandom().nextInt(lines.length)]), false);
            }
            if (count > 0 && count % 256 == 0) memory.memoryAnchor(serverPlayer.getBlockX(), serverPlayer.getBlockY(), serverPlayer.getBlockZ());
        });
    }

    private static boolean isMimicableBlock(String id) {
        return id.startsWith("minecraft:stone") || id.startsWith("minecraft:deepslate")
            || id.startsWith("minecraft:dirt") || id.startsWith("minecraft:grass_block")
            || id.startsWith("minecraft:oak_") || id.startsWith("minecraft:spruce_")
            || id.startsWith("minecraft:birch_") || id.startsWith("minecraft:dark_oak_")
            || id.startsWith("minecraft:acacia_") || id.startsWith("minecraft:jungle_")
            || id.startsWith("minecraft:cherry_") || id.startsWith("minecraft:mangrove_")
            || id.endsWith("_planks") || id.endsWith("_log") || id.endsWith("_wood")
            || id.endsWith("_wool") || id.endsWith("_concrete") || id.endsWith("_terracotta")
            || id.endsWith("_bricks") || id.equals("minecraft:glass") || id.equals("minecraft:obsidian");
    }

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getTime() % 200L != 0 || world.getPlayers().isEmpty()) return;
        RemnantState state = StateAccess.get(world);

        // A distant, non-damaging presence: rare smoke and a cave sound behind a player.
        if (state.stage() >= 3 && (state.lastStalker() < 0 || world.getTime() - state.lastStalker() >= 6000L)
                && world.getTime() % 1200L == 0) {
            ServerPlayerEntity watched = world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
            if (world.getRandom().nextInt(4) == 0 && ((world.getTimeOfDay() % 24000L >= 13000L && world.getTimeOfDay() % 24000L <= 23000L) || world.getLightLevel(watched.getBlockPos()) <= 7)) {
                double angle = watched.getYaw() * Math.PI / 180.0;
                double behindX = watched.getX() + Math.sin(angle) * -9.0;
                double behindZ = watched.getZ() + Math.cos(angle) * -9.0;
                BlockPos surface = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(behindX, watched.getY(), behindZ));
                BlockPos presence = surface.up();
                if (world.isChunkLoaded(presence) && world.getBlockState(presence).isAir()
                        && world.getBlockState(presence.up()).isAir() && world.getBlockState(surface).isSolid()
                        && world.getLightLevel(presence) <= 7 && watched.getBlockPos().isWithinDistance(presence, 5.0) == false) {
                    // Keep the apparition purely atmospheric: no collision, damage, or block changes.
                    double px = presence.getX() + 0.5;
                    double py = presence.getY() + 0.15;
                    double pz = presence.getZ() + 0.5;
                    world.spawnParticles(ParticleTypes.SMOKE, px, py, pz, 12, 0.35, 0.5, 0.35, 0.015);
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE, px, py + 0.2, pz, 3, 0.2, 0.3, 0.2, 0.005);
                    world.playSound(null, presence, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.85f, 0.55f);
                    state.fear(1);
                    state.awareness(1);
                    watched.sendMessage(Text.literal(state.stage() >= 4
                        ? "§8Something was standing behind you."
                        : "§7You feel watched."), true);
                    state.lastStalker(world.getTime());
                }
            }
        }

        if (world.getTime() % 600L == 0) {
            ServerPlayerEntity sampler = world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
            state.sampleArea(sampler.getBlockX() >> 4, sampler.getBlockZ() >> 4);
            if (state.stage() >= 3 && state.areaRevisits() >= 3 && state.areaRevisits() % 3 == 0
                    && (state.lastAreaEcho() < 0 || world.getTime() - state.lastAreaEcho() >= 24000L)) {
                sampler.sendMessage(Text.literal(state.stage() >= 4
                    ? "§4You returned to the place it kept."
                    : "§8This place feels remembered."), false);
                state.fear(1);
                state.awareness(1);
                state.lastAreaEcho(world.getTime());
            }
            if (state.stage() >= 4 && state.areaRevisits() >= 3 && !state.areaEchoPlaced()) {
                BlockPos areaCenter = new BlockPos(state.rememberedAreaX() * 16 + 8, 0, state.rememberedAreaZ() * 16 + 8);
                BlockPos echoSign = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, areaCenter);
                if (world.isChunkLoaded(echoSign) && world.getBlockState(echoSign).isAir() && world.getBlockState(echoSign.down()).isSolid()) {
                    world.setBlockState(echoSign, Blocks.OAK_SIGN.getDefaultState());
                    if (world.getBlockEntity(echoSign) instanceof SignBlockEntity sign) {
                        sign.changeText(text -> text.withMessage(0, Text.literal("It kept this place.")).withMessage(1, Text.literal("You came back.")), true);
                        sign.markDirty();
                        world.updateListeners(echoSign, world.getBlockState(echoSign), world.getBlockState(echoSign), 3);
                        state.areaEchoPlaced(true);
                    }
                }
            }
        }

        if (state.stage() >= 3 && state.doorInteractions() >= 8 && !state.doorEchoPlaced()) {
            BlockPos rememberedDoor = new BlockPos(state.doorX(), state.doorY(), state.doorZ());
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.getBlockPos().isWithinDistance(rememberedDoor, 7.0)) continue;
                BlockPos echo = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, rememberedDoor.add(2, 0, 1));
                if (world.isChunkLoaded(echo) && world.getBlockState(echo).isAir() && world.getBlockState(echo.down()).isSolid()) {
                    world.setBlockState(echo, Blocks.OAK_SIGN.getDefaultState());
                    if (world.getBlockEntity(echo) instanceof SignBlockEntity sign) {
                        String firstLine = state.stage() >= 4 ? "It learned the sound." : "You opened this.";
                        String secondLine = state.stage() >= 4 ? "It knows the way." : "It was listening.";
                        sign.changeText(text -> text.withMessage(0, Text.literal(firstLine)).withMessage(1, Text.literal(secondLine)), true);
                        sign.markDirty();
                        world.updateListeners(echo, world.getBlockState(echo), world.getBlockState(echo), 3);
                        state.doorEchoPlaced(true);
                        player.sendMessage(Text.literal(state.stage() >= 4 ? "§4It remembers how you enter." : "§8A familiar sound returns."), false);
                    }
                }
                break;
            }
        }

        if (state.stage() >= 4 && !state.buildEchoPlaced() && !state.rememberedBlockId().isEmpty()) {
            BlockPos remembered = new BlockPos(state.rememberedBlockX(), state.rememberedBlockY(), state.rememberedBlockZ());
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (!player.getBlockPos().isWithinDistance(remembered, 10.0)) continue;
                Identifier id = Identifier.tryParse(state.rememberedBlockId());
                if (id == null) { state.buildEchoPlaced(true); break; }
                var block = Registries.BLOCK.get(id);
                var mimicState = block.getDefaultState();
                BlockPos target = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, remembered.add(1, 0, 1));
                if (world.isChunkLoaded(target) && world.getBlockState(target).isAir()
                        && world.getBlockState(target.down()).isSolid()
                        && mimicState.isSolidBlock(world, target) && mimicState.getFluidState().isEmpty()) {
                    if (world.setBlockState(target, mimicState)) {
                        state.buildEchoPlaced(true);
                        player.sendMessage(Text.literal("§4Something rebuilt a piece of what you removed."), false);
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
                    String firstLine = state.stage() >= 4 ? "It learned your hands." : "You have been here before.";
                    String secondLine = state.stage() >= 4 ? "It can build now." : "It remembers.";
                    sign.changeText(text -> text.withMessage(0, Text.literal(firstLine)).withMessage(1, Text.literal(secondLine)), true);
                    sign.markDirty();
                    world.updateListeners(place, world.getBlockState(place), world.getBlockState(place), 3);
                    state.memoryEchoPlaced(true);
                    player.sendMessage(Text.literal(state.stage() >= 4 ? "§4It has changed what it remembers." : "§8A memory has returned to this place."), false);
                }
            }
            return;
        }
    }
}
