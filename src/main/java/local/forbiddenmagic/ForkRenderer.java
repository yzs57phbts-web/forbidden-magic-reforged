package local.forbiddenmagic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.*;

/** Vanilla geometry and UVs; colour is applied at draw time, without editing the texture. */
public final class ForkRenderer extends BlockEntityWithoutLevelRenderer {
    public static final int COLOUR=0xFFB52B26;
    private TridentModel model;
    public ForkRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager resources){model=null;}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(model==null)model=new TridentModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.TRIDENT));
        pose.pushPose();pose.scale(1,-1,-1);
        var vertices=ItemRenderer.getFoilBufferDirect(buffers,model.renderType(TridentModel.TEXTURE),false,stack.hasFoil());
        model.renderToBuffer(pose,vertices,light,overlay,COLOUR);pose.popPose();
    }
}
