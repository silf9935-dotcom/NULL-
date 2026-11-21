package dev.nullremnant.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class RemnantEntity extends HostileEntity {
    private int stareTicks;
    private int retreatTicks;
    public RemnantEntity(EntityType<? extends RemnantEntity> type, World world) { super(type, world); }
    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 36.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6);
    }
    @Override protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 28.0f));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.65));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }
    @Override public void tick() {
        super.tick();
        LivingEntity target=getTarget();
        if(target instanceof PlayerEntity player && player.isAlive()) {
            double d=squaredDistanceTo(player);
            if(d<16.0 && player.isLookingAt(this, 0.12)) stareTicks++; else stareTicks=Math.max(0,stareTicks-2);
            if(stareTicks>80) { retreatTicks=100; stareTicks=0; }
            if(retreatTicks>0) {
                retreatTicks--;
                setTarget(null);
                if(tickCount%20==0) {
                    double dx=getX()-player.getX(), dz=getZ()-player.getZ();
                    teleport(getX()+Math.signum(dx)*4, getY(), getZ()+Math.signum(dz)*4);
                }
            }
        }
        if(tickCount>1200 && getTarget()==null) discard();
    }
    @Override public boolean canSpawn(World world) { return super.canSpawn(world); }
    @Override protected boolean shouldDropLoot() { return false; }
    @Override public boolean isPersistent() { return true; }
}
