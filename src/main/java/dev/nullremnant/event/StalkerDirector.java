package dev.nullremnant.event;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.entity.RemnantEntities;
import dev.nullremnant.entity.RemnantEntity;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class StalkerDirector {
    private StalkerDirector() {}

    public static void tick(ServerWorld world) {
        if (!RemnantConfig.enabled || world.getPlayers().isEmpty() || world.getTime() % 2400L != 0) return;
        RemnantState state = StateAccess.get(world);
        long now = world.getTime();
        if (state.stage() < 2 || (state.lastEntityStalker() >= 0 && now - state.lastEntityStalker() < 2400L)) return;
        if (!world.getEntitiesByType(RemnantEntities.REMNANT, entity -> entity.isAlive()).isEmpty()) return;

        int chance = state.stage() >= 4 ? 22 : state.stage() == 3 ? 14 : 6;
        if (world.getRandom().nextInt(100) >= chance * RemnantConfig.stalkerFrequency / 100) return;

        ServerPlayerEntity player = world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
        BlockPos spawnPos = findHiddenSpawn(world, player);
        if (spawnPos == null) return;

        RemnantEntity entity = RemnantEntities.REMNANT.create(world);
        if (entity == null) return;
        entity.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
            world.getRandom().nextFloat() * 360.0f, 0.0f);
        entity.setTarget(player);
        if (!world.spawnEntity(entity)) return;

        state.lastEntityStalker(now);
        state.awareness(3);
        state.fear(3);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.HOSTILE,
            0.28f, 0.62f + world.getRandom().nextFloat() * 0.18f);
        player.sendMessage(Text.literal("§8You feel something watching from behind cover."), false);
    }

    private static BlockPos findHiddenSpawn(ServerWorld world, ServerPlayerEntity player) {
        double yaw = player.getYaw() * Math.PI / 180.0;
        double behindAngle = yaw + Math.PI;
        for (int attempt = 0; attempt < 18; attempt++) {
            double angle = behindAngle + (world.getRandom().nextDouble() - 0.5) * 1.5;
            int radius = 13 + world.getRandom().nextInt(14);
            int dx = (int) Math.round(-Math.sin(angle) * radius);
            int dz = (int) Math.round(Math.cos(angle) * radius);
            for (int verticalOffset = -2; verticalOffset <= 2; verticalOffset++) {
                BlockPos feet = player.getBlockPos().add(dx, verticalOffset, dz);
                if (!world.isChunkLoaded(feet) || !world.getBlockState(feet).isAir()
                        || !world.getBlockState(feet.up()).isAir()
                        || !world.getBlockState(feet.down()).isSolid()
                        || world.getLightLevel(feet) > 7
                        || player.getBlockPos().isWithinDistance(feet, 12.0)) continue;

                Vec3d start = player.getCameraPosVec(1.0f);
                Vec3d end = new Vec3d(feet.getX() + 0.5, feet.getY() + 1.45, feet.getZ() + 0.5);
                RaycastContext sightCheck = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, player);
                if (world.raycast(sightCheck).getType() == HitResult.Type.BLOCK) return feet;
            }
        }
        return null;
    }
}
