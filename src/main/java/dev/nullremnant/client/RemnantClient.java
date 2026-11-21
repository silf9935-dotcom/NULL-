package dev.nullremnant.client;
import dev.nullremnant.client.model.RemnantModel;
import dev.nullremnant.entity.RemnantEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
public final class RemnantClient implements ClientModInitializer {
 public static final EntityModelLayer MODEL_LAYER=new EntityModelLayer(new Identifier("null_remnant","remnant"),"main");
 @Override public void onInitializeClient(){
  net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(MODEL_LAYER,RemnantModel::getTexturedModelData);
  EntityRendererRegistry.register(RemnantEntities.REMNANT,RemnantRenderer::new);
 }
}