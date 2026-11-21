package dev.nullremnant;

import dev.nullremnant.command.RemnantCommands;
import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.entity.RemnantEntities;
import dev.nullremnant.event.MemorySystem;
import dev.nullremnant.event.RemnantEvents;
import dev.nullremnant.item.RemnantItems;
import dev.nullremnant.RemnantSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemnantMod implements ModInitializer {
    public static final String ID = "null_remnant";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        RemnantConfig.load();
        RemnantSounds.register();
        RemnantEntities.register();
        RemnantItems.register();
        RemnantCommands.register();
        MemorySystem.register();
        ServerTickEvents.END_WORLD_TICK.register(RemnantEvents::tick);
        LOGGER.info("NULL: The Remnant is awake.");
    }

    public static void tell(ServerWorld world, String text) {
        world.getPlayers().forEach(player -> player.sendMessage(Text.literal(text), false));
    }

    public static void tellKey(ServerWorld world, String translationKey) {
        world.getPlayers().forEach(player -> player.sendMessage(Text.translatable(translationKey), false));
    }
}
