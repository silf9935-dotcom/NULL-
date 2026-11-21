package dev.nullremnant.client;
import dev.nullremnant.entity.RemnantEntities;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
public final class RemnantHud {
 private static long lastWhisper=-1;
 private static int whisper=-1;
 private static final String[] LINES={"You are not alone.","It saw you.","Do not look away.","It remembers."};
 private RemnantHud(){}
 public static void register(){
  HudRenderCallback.EVENT.register((draw,tickDelta)->{
   MinecraftClient client=MinecraftClient.getInstance();
   if(client.player==null||client.world==null)return;
   boolean near=!client.world.getEntitiesByType(RemnantEntities.REMNANT,e->e.isAlive()&&e.distanceTo(client.player)<18).isEmpty();
   if(!near)return;
   long now=client.world.getTime();
   if(now-lastWhisper>100){lastWhisper=now;whisper=(whisper+1)%LINES.length;}
   int x=(draw.getScaledWindowWidth()-client.textRenderer.getWidth(LINES[whisper]))/2;
   int y=draw.getScaledWindowHeight()-48;
   draw.drawTextWithShadow(client.textRenderer,Text.literal(LINES[whisper]),x,y,0x88777777);
  });
 }
}