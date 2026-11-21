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
import net.minecraft.util.math.BlockPos;

public final class StalkerDirector {
    private StalkerDirector() {}
    public static void tick(ServerWorld world) {
        if(!RemnantConfig.enabled || world.getPlayers().isEmpty() || world.getTime()%2400L!=0) return;
        RemnantState state=StateAccess.get(world);
        if(state.stage()<2) return;
        if(world.getEntitiesByType(RemnantEntities.REMNANT, e->e.isAlive()).size()>0) return;
        int chance=state.stage()>=4?22:state.stage()==3?14:6;
                if(world.getRandom().nextInt(100)>=chance*RemnantConfig.stalkerFrequency/100) return;
        ServerPlayerEntity player=world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
        double angle=world.getRandom().nextDouble()*Math.PI*2;
        int radius=18+world.getRandom().nextInt(15);
        BlockPos pos=player.getBlockPos().add((int)(Math.cos(angle)*radius),0,(int)(Math.sin(angle)*radius));
        pos=world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,pos);
        if(!world.isChunkLoaded(pos))return;
        RemnantEntity entity=RemnantEntities.REMNANT.create(world);
        if(entity==null)return;
        entity.refreshPositionAndAngles(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,world.getRandom().nextFloat()*360,0);
        entity.setTarget(player);
        world.spawnEntity(entity);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.HOSTILE, 0.35f, 0.65f + world.getRandom().nextFloat()*0.2f);
        player.sendMessage(net.minecraft.text.Text.literal("§8You feel something watching from a distance."),false);
        state.awareness(3);
        state.fear(3);
    }
}
