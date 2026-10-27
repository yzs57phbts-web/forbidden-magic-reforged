package local.forbiddenmagic;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import velrondevs.botania.api.BotaniaClientCapabilities;
import velrondevs.botania.api.block_entity.*;
import velrondevs.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;

public final class BotanyClient {
    public static void init(IEventBus bus){bus.addListener(BotanyClient::renderers);bus.addListener(BotanyClient::capabilities);bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e)->e.enqueueWork(()->{
        BotanyCosmeticRenderer.register();
        BotanyContent.FLOWERS.values().forEach(block->net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(block.get(),net.minecraft.client.renderer.RenderType.cutout()));
        BotanyContent.FLOATERS.values().forEach(block->net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(block.get(),net.minecraft.client.renderer.RenderType.cutout()));
    }));}
    private static void renderers(EntityRenderersEvent.RegisterRenderers event){
        for(var name:BotanyContent.FLOWERS.keySet())event.registerBlockEntityRenderer(BotanyContent.<SpecialFlowerBlockEntity>type(name),SpecialFlowerBlockEntityRenderer::new);
    }
    private static void capabilities(RegisterCapabilitiesEvent event){
        for(var name:BotanyContent.FLOWERS.keySet())event.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD,BotanyContent.<BindableSpecialFlowerBlockEntity<?>>type(name),(flower,context)->new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(flower));
    }
}
