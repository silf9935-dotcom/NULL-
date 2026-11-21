package dev.nullremnant.client;
import dev.nullremnant.entity.RemnantEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import dev.nullremnant.entity.RemnantEntity;
public final class RemnantClient implements ClientModInitializer {
 @SuppressWarnings({"unchecked","rawtypes"})
 @Override public void onInitializeClient(){EntityRendererRegistry.register(RemnantEntities.REMNANT,ctx->(EntityRenderer<RemnantEntity>)(EntityRenderer)new ZombieEntityRenderer(ctx));}
}