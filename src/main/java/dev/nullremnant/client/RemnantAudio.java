package dev.nullremnant.client;

import dev.nullremnant.entity.RemnantEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;

public final class RemnantAudio {
    private static long lastCue = -1L;

    private RemnantAudio() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            long time = client.world.getTime();
            if (time - lastCue < 160L) return;

            boolean distantPresence = !client.world.getEntitiesByType(
                TypeFilter.instanceOf(RemnantEntity.class),
                client.player.getBoundingBox().expand(26.0),
                entity -> entity.isAlive()
                    && entity.distanceTo(client.player) >= 7.0f
                    && entity.distanceTo(client.player) <= 26.0f
            ).isEmpty();
            if (!distantPresence) return;

            lastCue = time;
            client.player.playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 0.12f, 0.68f);
        });
    }
}
