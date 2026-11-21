package dev.nullremnant.entity;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.RemnantSounds;
import dev.nullremnant.state.StateAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.sound.SoundEvent;

import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.EnumSet;

public class RemnantEntity extends ZombieEntity {
    private int stareTicks;
    private int lifetimeTicks;
    private int stalkPathCooldown;
    private int retreatTicks;
    private double retreatX;
    private double retreatY;
    private double retreatZ;

    public RemnantEntity(EntityType<? extends RemnantEntity> type, World world) {
        super(type, world);
        setBaby(false);
        setCanPickUpLoot(false);
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
        goalSelector.add(1, new MeleeAttackGoal(this, 1.15, false) {
            @Override
            public boolean canStart() {
                return RemnantConfig.enabled && getWorld() instanceof ServerWorld server && StateAccess.get(server).stage() >= 4
                    && retreatTicks <= 0 && super.canStart();
            }

            @Override
            public boolean shouldContinue() {
                return getWorld() instanceof ServerWorld server && StateAccess.get(server).stage() >= 4
                    && retreatTicks <= 0 && super.shouldContinue();
            }
        });
        goalSelector.add(2, new Goal() {
            {
                setControls(EnumSet.of(Control.MOVE, Control.LOOK));
            }

            @Override
            public boolean canStart() {
                return retreatTicks <= 0 && getTarget() instanceof PlayerEntity
                    && getWorld() instanceof ServerWorld server && StateAccess.get(server).stage() < 4;
            }

            @Override
            public boolean shouldContinue() {
                return canStart();
            }

            @Override
            public void tick() {
                if (!(getTarget() instanceof PlayerEntity player)) return;
                getLookControl().lookAt(player, 25.0f, 25.0f);
                double distance = squaredDistanceTo(player);
                if (distance > 100.0) {
                    if (stalkPathCooldown > 0) stalkPathCooldown--;
                    if (stalkPathCooldown == 0) {
                        getNavigation().startMovingTo(player, 0.72);
                        stalkPathCooldown = 10;
                    }
                } else {
                    stalkPathCooldown = 0;
                    getNavigation().stop();
                }
            }

            @Override
            public void stop() {
                getNavigation().stop();
            }
        });
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 28.0f));
        goalSelector.add(4, new WanderAroundFarGoal(this, 0.55));
        goalSelector.add(5, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true) {
            @Override
            public boolean canStart() {
                return RemnantConfig.enabled && retreatTicks <= 0 && super.canStart();
            }
        });
    }

    @Override
    public void tick() {
        if (!getWorld().isClient && !RemnantConfig.enabled) {
            discard();
            return;
        }
        super.tick();
        if (getWorld().isClient) return;
        lifetimeTicks++;
        LivingEntity target = getTarget();
        if (target instanceof PlayerEntity player && player.isAlive()) {
            if (squaredDistanceTo(player) < 256.0 && looksAt(player, this)) {
                stareTicks++;
            } else {
                stareTicks = Math.max(0, stareTicks - 2);
            }
            if (stareTicks >= 80) {
                retreatTicks = 100;
                retreatX = player.getX();
                retreatY = player.getY() + player.getStandingEyeHeight();
                retreatZ = player.getZ();
                stareTicks = 0;
                setInvisible(true);
                setTarget(null);
                getNavigation().stop();
            }
        }

        if (retreatTicks > 0) {
            retreatTicks--;
            if (lifetimeTicks % 20 == 0) retreatToCover();
            if (retreatTicks == 0) setInvisible(false);
        }

        if (lifetimeTicks > 2400 && getTarget() == null && retreatTicks <= 0) discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("RemnantStareTicks", stareTicks);
        nbt.putInt("RemnantRetreatTicks", retreatTicks);
        nbt.putDouble("RemnantRetreatX", retreatX);
        nbt.putDouble("RemnantRetreatY", retreatY);
        nbt.putDouble("RemnantRetreatZ", retreatZ);
        nbt.putInt("RemnantLifetimeTicks", lifetimeTicks);
        nbt.putInt("RemnantStalkPathCooldown", stalkPathCooldown);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        stareTicks = Math.max(0, Math.min(80, nbt.getInt("RemnantStareTicks")));
        retreatTicks = Math.max(0, Math.min(100, nbt.getInt("RemnantRetreatTicks")));
        retreatX = nbt.getDouble("RemnantRetreatX");
        retreatY = nbt.getDouble("RemnantRetreatY");
        retreatZ = nbt.getDouble("RemnantRetreatZ");
        lifetimeTicks = Math.max(0, nbt.getInt("RemnantLifetimeTicks"));
        stalkPathCooldown = Math.max(0, Math.min(10, nbt.getInt("RemnantStalkPathCooldown")));
        setInvisible(retreatTicks > 0);
    }

    private void retreatToCover() {
        if (!(getWorld() instanceof ServerWorld world)) return;
        double awayX = getX() - retreatX;
        double awayZ = getZ() - retreatZ;
        double length = Math.sqrt(awayX * awayX + awayZ * awayZ);
        if (length < 0.01) {
            awayX = getRandom().nextDouble() - 0.5;
            awayZ = getRandom().nextDouble() - 0.5;
            length = Math.sqrt(awayX * awayX + awayZ * awayZ);
        }
        awayX /= length;
        awayZ /= length;

        for (int i = 0; i < 6; i++) {
            double angle = (i - 2.5) * 0.38;
            double dx = awayX * Math.cos(angle) - awayZ * Math.sin(angle);
            double dz = awayX * Math.sin(angle) + awayZ * Math.cos(angle);
            BlockPos candidate = BlockPos.ofFloored(getX() + dx * 6.0, getY(), getZ() + dz * 6.0);
            if (!world.isChunkLoaded(candidate)) continue;
            BlockPos feet = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, candidate);
            if (!world.isChunkLoaded(feet) || !world.getBlockState(feet).isAir()
                    || !world.getBlockState(feet.up()).isAir()
                    || !world.getBlockState(feet.down()).isSolid()
                    || !world.getBlockState(feet).getFluidState().isEmpty()
                    || !world.getBlockState(feet.up()).getFluidState().isEmpty()
                    || world.getLightLevel(feet) > 7) continue;
            Vec3d fromPlayer = new Vec3d(retreatX, retreatY, retreatZ);
            Vec3d toShadow = new Vec3d(feet.getX() + 0.5, feet.getY() + 1.4, feet.getZ() + 0.5);
            RaycastContext coverCheck = new RaycastContext(fromPlayer, toShadow, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, this);
            if (world.raycast(coverCheck).getType() != HitResult.Type.BLOCK) continue;
            refreshPositionAndAngles(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5,
                getYaw() + 110.0f, getPitch());
            return;
        }
    }

    private static boolean looksAt(PlayerEntity player, LivingEntity target) {
        Vec3d from = player.getCameraPosVec(1.0f);
        Vec3d to = target.getPos().add(0.0, target.getStandingEyeHeight() * 0.8, 0.0);
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d toward = to.subtract(from).normalize();
        if (look.dotProduct(toward) <= 0.94) return false;

        RaycastContext visibility = new RaycastContext(
            from, to, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, player
        );
        return player.getWorld().raycast(visibility).getType() == HitResult.Type.MISS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return RemnantSounds.AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return RemnantSounds.HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return RemnantSounds.DEATH;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    public int getXpToDrop() {
        return 0;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
