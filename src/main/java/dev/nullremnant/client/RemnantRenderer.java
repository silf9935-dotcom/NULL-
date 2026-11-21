package dev.nullremnant.client;
import dev.nullremnant.client.model.RemnantModel;
import dev.nullremnant.entity.RemnantEntity;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
public final class RemnantRenderer extends MobEntityRenderer<RemnantEntity,RemnantModel> {
 private static final Identifier TEXTURE=new Identifier("minecraft","textures/entity/skeleton/wither_skeleton.png");
 public RemnantRenderer(EntityRendererFactory.Context context){super(context,new RemnantModel(context.getPart(RemnantClient.MODEL_LAYER)),0.45f);}
 @Override public Identifier getTexture(RemnantEntity entity){return TEXTURE;}
}