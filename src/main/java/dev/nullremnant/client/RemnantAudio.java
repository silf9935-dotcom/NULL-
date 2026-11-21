package dev.nullremnant.client;

import dev.nullremnant.RemnantSounds;
import dev.nullremnant.entity.RemnantEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.TypeFilter;

public final class RemnantAudio {
    private static long lastCue = -1L;
    private static net.minecraft.client.world.ClientWorld lastWorld;
    private RemnantAudio() {}
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            if (lastWorld != client.world) { lastWorld = client.world; lastCue = -1L; }
            long time = client.world.getTime();
            if (lastCue >= 0L && time >= lastCue && time - lastCue < 180L) return;
            boolean presence = !client.world.getEntitiesByType(TypeFilter.instanceOf(RemnantEntity.class),
                client.player.getBoundingBox().expand(26.0), entity -> entity.isAlive()
                    && entity.distanceTo(client.player) >= 7.0f && entity.distanceTo(client.player) <= 26.0f).isEmpty();
            if (!presence) return;
            lastCue = time;
            client.player.playSound(RemnantSounds.PRESENCE, 0.22f, 0.92f + client.world.random.nextFloat() * 0.12f);
        });
    }
}
