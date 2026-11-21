package dev.nullremnant.event;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
public final class AnomalyEvents {
 private AnomalyEvents(){}
 public static void tryTrigger(ServerWorld world,ServerPlayerEntity player){
  if(!RemnantConfig.enabled||!RemnantConfig.subtleAnomalies||world.getTime()%1200L!=0)return;RemnantState state=StateAccess.get(world);int stage=state.stage();if(stage<2)return;
  int chance=stage>=4?12:stage==3?7:3;if(world.getRandom().nextInt(100)>=chance*RemnantConfig.eventFrequency/100)return;
  switch(world.getRandom().nextInt(4)){
   case 0->player.sendMessage(Text.literal("§8A shape disappears at the edge of your vision."),false);
   case 1->player.sendMessage(Text.literal("§7The cave sounds strangely distant."),false);
   case 2->player.sendMessage(Text.literal("§8You notice something that was not there."),false);
   default->{BlockPos pos=player.getBlockPos().add(world.getRandom().nextInt(5)-2,-1,world.getRandom().nextInt(5)-2);if(world.getBlockState(pos).isOf(Blocks.STONE)&&world.getRandom().nextInt(10)==0)world.setBlockState(pos,Blocks.COBBLESTONE.getDefaultState());player.sendMessage(Text.literal("§7The ground looks different for a moment."),false);}
  }
  state.awareness(1);
 }
}