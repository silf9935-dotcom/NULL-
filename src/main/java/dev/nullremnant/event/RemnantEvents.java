package dev.nullremnant.event;

import dev.nullremnant.RemnantMod;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.List;
import java.util.Random;

public final class RemnantEvents {
    private static final Random RNG = new Random();
    private static final long EVENT_INTERVAL = 12_000L;

    private RemnantEvents() {
    }

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getPlayers().isEmpty()) return;

        RemnantState state = StateAccess.get(world);
        long now = world.getTime();
        int day = (int) (now / 24_000L);
        if (day > state.day()) state.day(day);

        int currentStage = stageForDay(day);
        if (currentStage != state.stage()) {
            state.stage(currentStage);
            announceStage(world, currentStage);
        }

        checkSleepInterruption(world, state, now);

        if (now % EVENT_INTERVAL == 0L && state.lastEvent() != now) {
            state.lastEvent(now);
            double pressure = Math.min(0.18,
                state.awareness() / 2500.0
                    + state.fear() / 2000.0
                    + state.intrusion() / 10_000.0);
            double chance = Math.min(1.0,
                (stageEventChance(currentStage) + pressure)
                    * RemnantConfig.eventFrequency / 100.0);
            if (RNG.nextDouble() < chance) {
                dispatchStageEvent(world, currentStage, RNG.nextInt(100));
            }
        }

        HorrorDirector.tick(world);
        StalkerDirector.tick(world);
        MemorySystem.tick(world);
        if (!world.getPlayers().isEmpty()) {
            AnomalyEvents.tryTrigger(world, world.getPlayers().get(0));
        }
    }

    private static int stageForDay(int day) {
        if (day < 3) return 0;
        if (day < 10) return 1;
        if (day < 15) return 2;
        if (day < 21) return 3;
        return 4;
    }

    private static double stageEventChance(int stage) {
        return new double[] {0.10, 0.24, 0.40, 0.52, 0.64}[stage];
    }

    private static void announceStage(ServerWorld world, int stage) {
        if (stage <= 0) return;
        RemnantMod.tellKey(world, "message.null_remnant.stage" + stage);

        if (stage == 4) {
            RemnantState state = StateAccess.get(world);
            if (!state.finaleStarted()) {
                state.finaleStarted(true);
                state.intrusion(10);
                RemnantMod.tellKey(world, "message.null_remnant.finale1");
                RemnantMod.tellKey(world, "message.null_remnant.finale2");
            }
        }
    }

    private static void checkSleepInterruption(ServerWorld world, RemnantState state, long now) {
        if (now % 100L != 0L || state.lastSleep() == now) return;

        List<ServerPlayerEntity> sleepers = world.getPlayers().stream()
            .filter(ServerPlayerEntity::isSleeping)
            .toList();
        if (sleepers.isEmpty()) return;

        state.lastSleep(now);
        if (!RemnantConfig.nightmares || state.day() < 3 || RNG.nextDouble() >= 0.30) return;

        ServerPlayerEntity player = sleepers.get(RNG.nextInt(sleepers.size()));
        state.fear(2);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_STARE,
            SoundCategory.AMBIENT, 0.18f, 0.55f);
        player.wakeUp(false, true);
        player.sendMessage(Text.translatable("message.null_remnant.nightmare_wake"), false);

        if (!state.hasSeenNightmare()) {
            state.nightmareSeen();
            player.sendMessage(Text.translatable("message.null_remnant.nightmare_first1"), false);
            player.sendMessage(Text.translatable("message.null_remnant.nightmare_first2"), false);
        } else if (state.stage() >= 3) {
            player.sendMessage(Text.translatable("message.null_remnant.nightmare_late1"), false);
            player.sendMessage(Text.translatable("message.null_remnant.nightmare_late2"), false);
        } else {
            player.sendMessage(Text.translatable("message.null_remnant.nightmare_repeat"), false);
        }
    }

    private static void dispatchStageEvent(ServerWorld world, int stage, int roll) {
        switch (stage) {
            case 0 -> early(world, roll);
            case 1 -> awakening(world, roll);
            case 2 -> aggression(world, roll);
            case 3 -> fracture(world, roll);
            default -> finale(world, roll);
        }
    }

    private static void early(ServerWorld world, int roll) {
        if (roll < 35) RemnantMod.tellKey(world, "event.null_remnant.wind_footsteps");
        else if (roll < 70) RemnantMod.tellKey(world, "event.null_remnant.chunk_pause");
        else RemnantMod.tellKey(world, "event.null_remnant.distant_door");
    }

    private static void awakening(ServerWorld world, int roll) {
        if (roll < 25) RemnantMod.tellKey(world, "event.null_remnant.forest_steps");
        else if (roll < 50) smokeCross(world);
        else if (roll < 75) sign(world, "sign.null_remnant.remember_place");
        else RemnantMod.tellKey(world, "event.null_remnant.unexplained_change");
    }

    private static void aggression(ServerWorld world, int roll) {
        if (roll < 20) sign(world, "sign.null_remnant.always_close_door");
        else if (roll < 40) RemnantMod.tellKey(world, "event.null_remnant.fog_figure");
        else if (roll < 60) RemnantMod.tellKey(world, "event.null_remnant.door_sound");
        else if (roll < 80) {
            ServerPlayerEntity player = randomPlayer(world);
            player.sendMessage(Text.translatable("event.null_remnant.player_return", player.getName()), false);
        } else RemnantMod.tellKey(world, "event.null_remnant.save_pause");
    }

    private static void fracture(ServerWorld world, int roll) {
        if (roll < 20) sign(world, "sign.null_remnant.built_before");
        else if (roll < 40) RemnantMod.tellKey(world, "event.null_remnant.familiar_landscape");
        else if (roll < 60) smokeCross(world);
        else if (roll < 80) RemnantMod.tellKey(world, "event.null_remnant.delayed_steps");
        else RemnantMod.tellKey(world, "event.null_remnant.repeated_sound");
    }

    private static void finale(ServerWorld world, int roll) {
        if (roll < 25) sign(world, "sign.null_remnant.home_learned");
        else if (roll < 50) RemnantMod.tellKey(world, "event.null_remnant.interior_door");
        else if (roll < 75) RemnantMod.tellKey(world, "event.null_remnant.first_arrival");
        else RemnantMod.tellKey(world, "event.null_remnant.single_player");
    }

    private static ServerPlayerEntity randomPlayer(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        return players.get(world.getRandom().nextInt(players.size()));
    }

    private static void smokeCross(ServerWorld world) {
        ServerPlayerEntity player = randomPlayer(world);
        BlockPos sample = player.getBlockPos().add(
            RNG.nextInt(17) - 8, 0, RNG.nextInt(17) - 8
        );
        if (!world.isChunkLoaded(sample)) return;

        BlockPos center = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, sample);
        if (!world.isChunkLoaded(center)) return;

        int emitted = 0;
        for (int offset = -2; offset <= 2; offset++) {
            BlockPos eastWest = center.add(offset, 0, 0);
            if (isSafeSmokePosition(world, eastWest)) {
                spawnSmoke(world, eastWest);
                emitted++;
            }

            BlockPos northSouth = center.add(0, 0, offset);
            if (offset != 0 && isSafeSmokePosition(world, northSouth)) {
                spawnSmoke(world, northSouth);
                emitted++;
            }
        }

        if (emitted == 0) return;
        world.playSound(null, center, SoundEvents.AMBIENT_CAVE.value(),
            SoundCategory.AMBIENT, 0.65f, 0.65f);
        player.sendMessage(Text.translatable("event.null_remnant.smoke_cross"), false);
    }

    private static boolean isSafeSmokePosition(ServerWorld world, BlockPos pos) {
        return world.isChunkLoaded(pos)
            && world.getBlockState(pos).isAir()
            && world.getBlockState(pos.up()).isAir()
            && world.getBlockState(pos.down()).isSolid();
    }

    private static void spawnSmoke(ServerWorld world, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.15;
        double z = pos.getZ() + 0.5;
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y, z,
            5, 0.18, 0.12, 0.18, 0.005);
        world.spawnParticles(ParticleTypes.SMOKE, x, y, z,
            8, 0.22, 0.18, 0.22, 0.008);
    }

    private static void sign(ServerWorld world, String translationKey) {
        ServerPlayerEntity player = randomPlayer(world);
        BlockPos sample = player.getBlockPos().add(
            RNG.nextInt(9) - 4, 0, RNG.nextInt(9) - 4
        );
        if (!world.isChunkLoaded(sample)) return;

        BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, sample);
        if (!world.isChunkLoaded(pos)
                || !world.getBlockState(pos).isAir()
                || !world.getBlockState(pos.down()).isSolid()) return;
        if (!world.setBlockState(pos, Blocks.OAK_SIGN.getDefaultState())) return;

        if (world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            sign.changeText(text -> text
                .withMessage(0, Text.translatable(translationKey))
                .withMessage(1, Text.empty()), true);
            sign.markDirty();
            world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
}
