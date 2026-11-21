package dev.nullremnant.event;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.state.RemnantState;
import dev.nullremnant.state.StateAccess;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
public final class MemorySystem {
 private MemorySystem(){}
 public static void register(){
  PlayerBlockBreakEvents.AFTER.register((world,player,pos,state,blockEntity)->{
   if(!RemnantConfig.enabled||!(world instanceof ServerWorld server)||!(player instanceof ServerPlayerEntity p))return;
   RemnantState memory=StateAccess.get(server);memory.intrusion(1);memory.awareness(1);
   int count=memory.intrusion();
   if(count>0&&count%128==0&&memory.stage()>=2){
    String[] lines={"§8You hear the same sound twice.","§7Something is learning the rhythm of your hands.","§8The world remembers what you remove."};
    p.sendMessage(Text.literal(lines[server.getRandom().nextInt(lines.length)]),false);
   }
  });
 }
}