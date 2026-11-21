package dev.nullremnant.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;\nimport net.minecraft.server.world.ServerWorld;\nimport dev.nullremnant.state.StateAccess;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RemnantEntity extends ZombieEntity {
    private int stareTicks;
    private int retreatTicks;

    public RemnantEntity(EntityType<? extends RemnantEntity> type, World world) {
        super(type, world);
        setBaby(false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 36.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new MeleeAttackGoal(this, 1.15, false) {\n            @Override\n            public boolean canStart() {\n                return getWorld() instanceof ServerWorld server && StateAccess.get(server).stage() >= 4 && super.canStart();\n            }\n            @Override\n            public boolean shouldContinue() {\n                return getWorld() instanceof ServerWorld server && StateAccess.get(server).stage() >= 4 && super.shouldContinue();\n            }\n        });
        goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 28.0f));
        goalSelector.add(3, new WanderAroundFarGoal(this, 0.65));
        goalSelector.add(4, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true) {
            @Override
            public boolean canStart() {
                return retreatTicks <= 0 && super.canStart();
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = getTarget();
        if (target instanceof PlayerEntity player && player.isAlive()) {
            if (squaredDistanceTo(player) < 16 && looksAt(player, this)) {
                stareTicks++;
            } else {
                stareTicks = Math.max(0, stareTicks - 2);
            }
            if (stareTicks > 80) {
                retreatTicks = 100;
                stareTicks = 0;
            }
        }

        if (retreatTicks > 0) {
            retreatTicks--;
            setTarget(null);
            if (target instanceof PlayerEntity player && tickCount % 20 == 0) {
                double dx = getX() - player.getX();
                double dz = getZ() - player.getZ();
                teleport(getX() + Math.signum(dx) * 4.0, getY(), getZ() + Math.signum(dz) * 4.0);
            }
        }

        if (tickCount > 1200 && getTarget() == null) discard();
    }

    private static boolean looksAt(PlayerEntity player, LivingEntity target) {
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d toward = target.getPos().subtract(player.getPos()).normalize();
        return look.dotProduct(toward) > 0.94;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
