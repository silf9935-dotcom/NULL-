package dev.nullremnant.event;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
public final class MemorySystem {
 private MemorySystem(){}
 public static void register(){
  PlayerBlockBreakEvents.AFTER.register((world,player,pos,state,blockEntity)->{
   if(!RemnantConfig.enabled||!(world instanceof ServerWorld server)||!(player instanceof ServerPlayerEntity p))return;
   RemnantState memory=StateAccess.get(server);memory.intrusion(1);memory.awareness(1);
   int count=memory.intrusion();
   if(count>0&&count%128==0&&memory.stage()>=2){String[] lines={"§8You hear the same sound twice.","§7Something is learning the rhythm of your hands.","§8The world remembers what you remove."};p.sendMessage(Text.literal(lines[server.getRandom().nextInt(lines.length)]),false);}
   if(count>0&&count%256==0)memory.memoryAnchor(p.getBlockX(),p.getBlockY(),p.getBlockZ());
  });
 }
 public static void tick(ServerWorld world){
  if(!RemnantConfig.enabled||world.getTime()%200L!=0||world.getPlayers().isEmpty())return;
  RemnantState state=StateAccess.get(world);if(state.stage()<3||!state.memoryAnchorSet()||state.memoryEchoPlaced())return;
  for(ServerPlayerEntity player:world.getPlayers()){
   BlockPos anchor=new BlockPos(state.memoryX(),state.memoryY(),state.memoryZ());
   if(player.getBlockPos().isWithinDistance(anchor,6.0)){
    BlockPos place=world.getTopPosition(Heightmap.Type.MOTION_BLOCKING,anchor);
    if(world.getBlockState(place).isAir()&&world.getBlockState(place.down()).isSolid()&&world.isChunkLoaded(place)){
     world.setBlockState(place,Blocks.OAK_SIGN.getDefaultState());
     if(world.getBlockEntity(place) instanceof SignBlockEntity sign){sign.changeText(text->text.withMessage(0,Text.literal("You have been here before.")).withMessage(1,Text.literal("It remembers.")),true);sign.markDirty();world.updateListeners(place,world.getBlockState(place),world.getBlockState(place),3);state.memoryEchoPlaced(true);player.sendMessage(Text.literal("§8A memory has returned to this place."),false);}
    }
    return;
   }
  }
 }
}