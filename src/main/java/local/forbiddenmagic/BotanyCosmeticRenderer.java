package local.forbiddenmagic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.*;
import velrondevs.botania.api.item.CosmeticAttachable;

/** Delegates the cosmetic stack to its own Botania renderer, retaining native Curios slots. */
public final class BotanyCosmeticRenderer implements ICurioRenderer {
    public static void register(){for(String id:java.util.List.of("submissive_collar","ring_of_nutrition"))CuriosRendererRegistry.register(Content.get(id),BotanyCosmeticRenderer::new);}
    @Override public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack stack,SlotContext slot,PoseStack pose,RenderLayerParent<T,M> parent,MultiBufferSource buffer,int light,float swing,float amount,float partial,float age,float yaw,float pitch){
        var cosmetic=((CosmeticAttachable)stack.getItem()).getCosmeticItem(stack);if(cosmetic.isEmpty())return;
        CuriosRendererRegistry.getRenderer(cosmetic.getItem()).ifPresent(r->r.render(cosmetic,slot,pose,parent,buffer,light,swing,amount,partial,age,yaw,pitch));
    }
}
