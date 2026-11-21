package dev.nullremnant;
import dev.nullremnant.event.RemnantEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class RemnantMod implements ModInitializer {
 public static final String ID="null_remnant";
 public static final Logger LOGGER=LoggerFactory.getLogger(ID);
 public void onInitialize(){ LOGGER.info("NULL: The Remnant is awake."); ServerTickEvents.END_WORLD_TICK.register(RemnantEvents::tick); }
 public static void tell(ServerWorld world,String text){world.getPlayers().forEach(p->p.sendMessage(Text.literal(text),false));}
}