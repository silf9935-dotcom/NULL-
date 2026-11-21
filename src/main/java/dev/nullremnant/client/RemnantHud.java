package dev.nullremnant.client;

import dev.nullremnant.config.RemnantConfig;
import dev.nullremnant.entity.RemnantEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;

public final class RemnantHud {
    private static long lastWhisper = -1L;
    private static int whisper = -1;
    private static final String[] LINES = {
        "You are not alone.",
        "It saw you.",
        "Do not look away.",
        "It remembers."
    };

    private RemnantHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((draw, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!RemnantConfig.enabled || client.player == null || client.world == null) return;

            boolean near = !client.world.getEntitiesByType(
                TypeFilter.instanceOf(RemnantEntity.class),
                client.player.getBoundingBox().expand(18.0),
                entity -> entity.isAlive() && entity.distanceTo(client.player) < 18.0f
            ).isEmpty();
            if (!near) return;

            long now = client.world.getTime();
            if (now - lastWhisper > 100L) {
                lastWhisper = now;
                whisper = (whisper + 1) % LINES.length;
            }

            int x = (draw.getScaledWindowWidth() - client.textRenderer.getWidth(LINES[whisper])) / 2;
            int y = draw.getScaledWindowHeight() - 48;
            draw.drawTextWithShadow(client.textRenderer, Text.literal(LINES[whisper]), x, y, 0x88777777);
        });
    }
}
