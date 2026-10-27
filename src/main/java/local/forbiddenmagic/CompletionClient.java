package local.forbiddenmagic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;

public final class CompletionClient {
    public static void init(IEventBus bus){
        if(ForbiddenMagic.loaded("bloodmagic"))bus.addListener((net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event)->event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){
            private RapierRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new RapierRenderer();return renderer;}
        },Content.get("rapier_of_the_strix")));
        bus.addListener((net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event)->event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){
            private ForkRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ForkRenderer();return renderer;}
        },Content.get("diabolists_fork")));
        bus.addListener((RegisterColorHandlersEvent.Item event)->event.register((stack,tint)->LegacyConfig.bool("spork")?-1:ForkRenderer.COLOUR,Content.get("diabolists_fork")));
        bus.addListener((RegisterColorHandlersEvent.Item e)->{for(String id:java.util.List.of("morph_pickaxe","morph_axe","morph_sword","morph_shovel"))e.register((s,tint)->tint==1?0xFF000000|new int[]{9961472,4300,15063552}[Math.floorMod(DarkTools.phase(s),3)]:-1,Content.get(id));});
        bus.addListener((EntityRenderersEvent.RegisterRenderers e)->e.registerBlockEntityRenderer(Completion.CAGE_ENTITY.get(),CageRenderer::new));
        bus.addListener((FMLClientSetupEvent e)->e.enqueueWork(()->ItemProperties.register(Content.get("soul_crystal"),ForbiddenMagic.id("filled"),(s,l,entity,seed)->WrathCage.soul(s).isEmpty()?0:1)));
        bus.addListener((FMLClientSetupEvent e)->e.enqueueWork(()->ItemProperties.register(Content.get("diabolists_fork"),ForbiddenMagic.id("spork"),(s,l,entity,seed)->LegacyConfig.bool("spork")?1:0)));
    }
    public static final class CageRenderer implements BlockEntityRenderer<WrathCage.Cage> {
        private Entity entity;private String soul="";
        public CageRenderer(BlockEntityRendererProvider.Context c){}
        @Override public void render(WrathCage.Cage cage,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
            if(cage.getLevel()==null||cage.mob.isEmpty())return;
            if(entity==null||!soul.equals(cage.mob)||entity.level()!=cage.getLevel()){soul=cage.mob;var id=ResourceLocation.tryParse(soul);entity=id==null?null:BuiltInRegistries.ENTITY_TYPE.get(id).create(cage.getLevel());}
            if(entity==null)return;float scale=0.48F/Math.max(1,Math.max(entity.getBbWidth(),entity.getBbHeight()));pose.pushPose();pose.translate(0.5,0.12,0.5);pose.mulPose(Axis.YP.rotationDegrees((cage.getLevel().getGameTime()+partial)*3));pose.scale(scale,scale,scale);Minecraft.getInstance().getEntityRenderDispatcher().render(entity,0,0,0,0,partial,pose,buffers,light);pose.popPose();
        }
    }
}
