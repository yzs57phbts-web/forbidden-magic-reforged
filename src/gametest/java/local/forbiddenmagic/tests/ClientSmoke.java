package local.forbiddenmagic.tests;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.CuratedAspects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.aspect.Aspect;

@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class ClientSmoke {
    private static int ticks;
    private static boolean done;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws Exception {
        if(done||!Boolean.getBoolean("forbiddenmagic.clientSmoke")) return;
        var mc=Minecraft.getInstance();
        if(mc.screen instanceof AccessibilityOnboardingScreen && mc.getOverlay()==null) mc.setScreen(new TitleScreen());
        if(!(mc.screen instanceof TitleScreen)||mc.getOverlay()!=null||++ticks<40) return;
        done=true;
        var missing=new ArrayList<String>(); int items=0;
        for(var rule:CuratedAspects.rules()) {
            if(rule.entity()||!ModList.get().isLoaded(rule.mod()))continue;
            for(var id:rule.targets()) {
                var item=BuiltInRegistries.ITEM.getOptional(id).orElseThrow();
                var model=mc.getItemRenderer().getModel(new ItemStack(item),null,null,0);
                if(model==mc.getModelManager().getMissingModel())missing.add(id.toString());
                items++;
            }
        }
        for(var entry:local.forbiddenmagic.Content.ALL.entrySet()){
            var item=entry.getValue().get();
            var model=mc.getItemRenderer().getModel(new ItemStack(item),null,null,0);
            if(model==mc.getModelManager().getMissingModel())missing.add("forbiddenmagic:"+entry.getKey());
            for(var face:java.util.Arrays.asList(null,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.DOWN,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST))for(var quad:model.getQuads(null,face,net.minecraft.util.RandomSource.create(1)))
                if(quad.getSprite().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))missing.add("forbiddenmagic:"+entry.getKey()+"/texture");
            items++;
        }
        for(var rod:thaumcraft.wand.WandRegistry.rods())if(rod.tag().startsWith("fm_"))
            if(mc.getResourceManager().getResource(rod.texture()).isEmpty())missing.add(rod.texture().toString());
        for(var cap:thaumcraft.wand.WandRegistry.caps())if(cap.tag().startsWith("fm_"))
            if(mc.getResourceManager().getResource(cap.texture()).isEmpty())missing.add(cap.texture().toString());
        for(var aspect:Aspect.getEnabledAspects())
            if(mc.getResourceManager().getResource(aspect.getImage()).isEmpty())missing.add(aspect.getImage().toString());
        for(var r:thaumcraft.research.ResearchCategories.get("FORBIDDEN").research())if(r.iconTexture()!=null&&mc.getResourceManager().getResource(r.iconTexture()).isEmpty())missing.add(r.iconTexture().toString());
        if(mc.getResourceManager().getResource(net.minecraft.resources.ResourceLocation.parse("forbiddenmagic:textures/misc/runecircle.png")).isEmpty())missing.add("research_background");
        var proof=Map.of("status",missing.isEmpty()?"PASS":"FAIL","title_screen",true,"item_block_models",items,"aspect_icons",Aspect.getEnabledAspects().length,"missing",missing);
        Files.writeString(Path.of("aspect-client-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));
        mc.stop();
    }
}
