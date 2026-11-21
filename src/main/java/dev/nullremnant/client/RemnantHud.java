package dev.nullremnant.client;

import dev.nullremnant.entity.RemnantEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;

public final class RemnantHud {
    private static long lastWhisper = -1L;
    private static net.minecraft.client.world.ClientWorld lastWorld;
    private static int whisper = -1;
    private static final String[] LINES = {
        "hud.null_remnant.whisper1",
        "hud.null_remnant.whisper2",
        "hud.null_remnant.whisper3",
        "hud.null_remnant.whisper4"
    };

    private RemnantHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((draw, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            boolean near = !client.world.getEntitiesByType(
                TypeFilter.instanceOf(RemnantEntity.class),
                client.player.getBoundingBox().expand(18.0),
                entity -> entity.isAlive() && entity.distanceTo(client.player) < 18.0f
            ).isEmpty();
            if (!near) return;

            if (lastWorld != client.world) {
                lastWorld = client.world;
                lastWhisper = -1L;
                whisper = -1;
            }
            long now = client.world.getTime();
            if (whisper < 0 || now < lastWhisper || now - lastWhisper > 100L) {
                lastWhisper = now;
                whisper = (whisper + 1) % LINES.length;
            }

            Text line = Text.translatable(LINES[whisper]);
            int x = (draw.getScaledWindowWidth() - client.textRenderer.getWidth(line)) / 2;
            int y = draw.getScaledWindowHeight() - 48;
            draw.drawTextWithShadow(client.textRenderer, line, x, y, 0x88777777);
        });
    }
}
