package dev.nullremnant.event;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
public final class HorrorDirector {
 private HorrorDirector(){}
 public static void tick(ServerWorld world){
  if(world.getTime()%1200L!=0||world.getPlayers().isEmpty())return;
  RemnantState s=StateAccess.get(world);int stage=s.stage();
  int chance=new int[]{0,8,18,30,42}[Math.min(stage,4)];
  if(world.getRandom().nextInt(100)>=chance*RemnantConfig.eventFrequency/100)return;
  ServerPlayerEntity p=world.getPlayers().get(world.getRandom().nextInt(world.getPlayers().size()));
  String[] lines={"The silence feels deliberate.","Something moved beyond your sight.","You have been here before.","Don't turn around.","It remembers you."};
  p.sendMessage(Text.literal("§8"+lines[world.getRandom().nextInt(lines.length)]),false);
  s.awareness(1);s.lastEvent(world.getTime());
 }
}