package dev.nullremnant.client.model;
import dev.nullremnant.entity.RemnantEntity;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;
public final class RemnantModel extends EntityModel<RemnantEntity> {
 private final ModelPart head,body,leftArm,rightArm,leftLeg,rightLeg;
 public RemnantModel(ModelPart root){super(root);head=root.getChild("head");body=root.getChild("body");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");}
 public static TexturedModelData getTexturedModelData(){
  ModelData data=new ModelData();ModelPartData root=data.getRoot();
  root.addChild("head",ModelPartBuilder.create().uv(0,0).cuboid(-4,-8,-4,8,8,8).uv(32,0).cuboid(-3,-7,-4.1f,6,6,1),ModelTransform.pivot(0,0,0));
  root.addChild("body",ModelPartBuilder.create().uv(0,16).cuboid(-4,-2,-2,8,16,4).uv(24,16).cuboid(-3,-1,-2.5f,6,14,1),ModelTransform.pivot(0,0,0));
  root.addChild("left_arm",ModelPartBuilder.create().uv(40,16).cuboid(-1,-2,-2,4,14,4),ModelTransform.pivot(5,0,0));
  root.addChild("right_arm",ModelPartBuilder.create().uv(40,16).cuboid(-3,-2,-2,4,14,4),ModelTransform.pivot(-5,0,0));
  root.addChild("left_leg",ModelPartBuilder.create().uv(0,32).cuboid(-2,0,-2,4,12,4),ModelTransform.pivot(2,12,0));
  root.addChild("right_leg",ModelPartBuilder.create().uv(0,32).cuboid(-2,0,-2,4,12,4),ModelTransform.pivot(-2,12,0));
  return TexturedModelData.of(data,64,64);
 }
 @Override public void setAngles(RemnantEntity e,float limbAngle,float limbDistance,float animationProgress,float headYaw,float headPitch){
  head.yaw=headYaw*(Math.PI/180f);head.pitch=headPitch*(Math.PI/180f);
  float swing=MathHelper.cos(limbAngle*0.6662f)*1.4f*limbDistance;
  leftLeg.pitch=swing;rightLeg.pitch=MathHelper.cos(limbAngle*0.6662f+(float)Math.PI)*1.4f*limbDistance;
  leftArm.pitch=MathHelper.cos(limbAngle*0.6662f+(float)Math.PI)*1.2f*limbDistance;rightArm.pitch=MathHelper.cos(limbAngle*0.6662f)*1.2f*limbDistance;
  body.pitch=0.08f+MathHelper.sin(animationProgress*0.04f)*0.025f;
 }
}