package local.forbiddenmagic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.*;

/** Thin trident-style blade; the grip ends just behind the hand instead of forming a long shaft. */
public final class RapierRenderer extends BlockEntityWithoutLevelRenderer {
    // ItemRenderer centres the item cube by8px after the native17px hand translation.
    public static final float HAND_Y=9;
    private ModelPart model;
    public RapierRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    public static ModelPart createGeometry(){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("blade",CubeListBuilder.create().texOffs(0,6).addBox(-.5F,-4,-.5F,1,18.5F,1),PartPose.ZERO);
        root.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(0,0).addBox(-.5F,-2,-.5F,1,4,1),PartPose.offset(0,-5,0));
        root.addOrReplaceChild("guard",CubeListBuilder.create().texOffs(4,0).addBox(-1.5F,-1,-.5F,3,2,1),PartPose.offset(0,15,0));
        root.addOrReplaceChild("grip",CubeListBuilder.create().texOffs(0,6).addBox(-.5F,15.5F,-.5F,1,3.5F,1),PartPose.ZERO);
        root.addOrReplaceChild("pommel",CubeListBuilder.create().texOffs(0,6).addBox(-.5F,-.5F,-.5F,1,1,1),PartPose.offset(0,19,0));
        var baked=LayerDefinition.create(mesh,32,32).bakeRoot();
        var tip=baked.getChild("tip");tip.xScale=tip.yScale=tip.zScale=.5F;
        var guard=baked.getChild("guard");guard.xScale=2;guard.yScale=.5F;
        var pommel=baked.getChild("pommel");pommel.xScale=pommel.zScale=1.5F;pommel.yScale=.7F;
        for(var part:baked.getAllParts().toList())if(part!=baked)part.y-=8;
        return baked;
    }
    @Override public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resources){model=null;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(model==null)model=createGeometry();pose.pushPose();pose.scale(1,-1,-1);
        var vertices=ItemRenderer.getFoilBufferDirect(buffers,RenderType.entitySolid(net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/misc/white.png")),false,stack.hasFoil());
        for(String part:new String[]{"blade","tip","guard","grip","pommel"})model.getChild(part).render(pose,vertices,light,overlay,switch(part){case "blade","tip"->0xFFC42832;case "grip"->0xFF70452E;default->0xFFE2BA58;});
        pose.popPose();
    }
}
