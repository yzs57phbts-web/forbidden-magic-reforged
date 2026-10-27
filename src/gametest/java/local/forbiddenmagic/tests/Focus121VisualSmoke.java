package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.wand.*;
import thaumcraft.research.*;
import thaumcraft.client.research.ResearchPageScreen;

/** Test-only real mounted wand renderer, not merely an inventory PNG existence check. */
@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class Focus121VisualSmoke {
    private static final List<String> NAMES=List.of("blink-main","blink-offhand","blink-staff-main","blink-staff-offhand","blink-gui","plain-wand-main","vanilla-frost-main","blink-research","recipe-infernal","recipe-taintpick","recipe-cage","recipe-cake");
    private static boolean scheduled,done,loading;private static volatile boolean granted;
    private static int stage,ticks;private static final List<String> captures=new ArrayList<>();private static final Map<String,Object> proof=new LinkedHashMap<>();
    private static ItemStack wand(boolean staff,boolean focus,boolean frost){
        var rod=staff?WandRegistry.rod("silverwood_staff"):WandRegistry.ROD_SILVERWOOD;
        if(rod==null)throw new IllegalStateException("Missing native silverwood staff");
        var s=Wands.create(WandRegistry.CAP_GOLD,rod,false);Wands.fill(s);if(Wands.isStaff(s)!=staff)throw new IllegalStateException("Wrong native staff/wand kind");
        if(focus)Wands.setFocus(s,frost?new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:focus_frost"))):Content.stack("blink_focus"));return s;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.audit121Smoke"))return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||loading)return;
        if(!mc.getLanguageManager().getSelected().equals("ru_ru")){loading=true;mc.getLanguageManager().setSelected("ru_ru");mc.options.languageCode="ru_ru";mc.reloadResourcePacks().thenRun(()->loading=false);return;}
        if(!scheduled){scheduled=true;var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(0.5,100,0.5);p.setYRot(0);p.setYHeadRot(0);p.setXRot(8);
            p.serverLevel().setDayTime(6000);p.serverLevel().setWeatherParameters(6000,0,false,false);
            for(var a:thaumcraft.aspect.Aspect.getEnabledAspects())Knowledge.get(p).discoverAspect(a);for(var r:ResearchCategories.allResearch())Knowledge.get(p).addResearch(r.key());Knowledge.sync(p);granted=true;
        });return;}
        if(!granted||!Knowledge.get(mc.player).isResearchComplete("FM_CAGE"))return;
        ticks++;
        if(ticks==1){
            if(stage==0)audit(mc);
            mc.setScreen(null);mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setXRot(8);mc.player.setYRot(0);mc.player.setYHeadRot(0);
            if(stage<7){
                var id=mc.player.getUUID();int selected=stage;mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var item=wand(selected==2||selected==3,selected!=5,selected==6);boolean off=selected==1||selected==3;p.setItemInHand(InteractionHand.MAIN_HAND,off?ItemStack.EMPTY:item);p.setItemInHand(InteractionHand.OFF_HAND,off?item:ItemStack.EMPTY);});
                if(stage==4)mc.setScreen(new Preview());
            }else{
                String key=switch(stage){case 7->"FM_BLINK";case 8->"ROD_fm_infernal";case 9->"FM_TAINTPICK";case 10->"FM_CAGE";default->"FM_ARCANECAKE";};
                mc.setScreen(new ResearchPageScreen(ResearchCategories.getResearch(key),0));
            }
        }
        if(ticks==50){Screenshot.grab(mc.gameDirectory,"fm121-"+NAMES.get(stage)+".png",mc.getMainRenderTarget(),m->captures.add(NAMES.get(stage)));}
        if(ticks>=70){ticks=0;stage++;if(stage==NAMES.size()){done=true;proof.put("status",captures.size()==12?"PASS":"FAIL");proof.put("captures",captures);Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"focus-121-client-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));mc.stop();}}
    }
    private static void audit(Minecraft mc)throws Exception{
        var focus=Content.stack("blink_focus");var item=(BlinkFocus)focus.getItem();var atlas=mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);var ids=new ArrayList<net.minecraft.resources.ResourceLocation>();ids.add(item.getFocusTexture(focus));ids.add(item.getOrnamentTexture(focus));if(item.getDepthLayerTexture(focus)!=null)ids.add(item.getDepthLayerTexture(focus));
        for(var id:ids)if(atlas.apply(id).contents().name().equals(MissingTextureAtlasSprite.getLocation()))throw new IllegalStateException("Missing mounted focus atlas sprite "+id);
        proof.put("mounted_sprite_ids",ids.stream().map(Object::toString).toList());proof.put("colour",item.getFocusColor(focus));
        proof.put("old_png_path_is_missing_sprite",atlas.apply(ForbiddenMagic.id("textures/foci/blink_ornament.png")).contents().name().equals(MissingTextureAtlasSprite.getLocation()));
        if(!proof.get("old_png_path_is_missing_sprite").equals(true))throw new IllegalStateException("Regression control did not reproduce old bad sprite lookup");
        var missing=new ArrayList<String>();int count=0;for(var entry:Content.ALL.entrySet()){var model=mc.getItemRenderer().getModel(new ItemStack(entry.getValue().get()),mc.level,mc.player,0);if(model==mc.getModelManager().getMissingModel())missing.add(entry.getKey());count++;}
        if(!missing.isEmpty())throw new IllegalStateException("Missing FM models "+missing);proof.put("fm_models",count);proof.put("missing",missing);
        if(Boolean.getBoolean("forbiddenmagic.legacy130Smoke")){
            var fork=Content.stack("diabolists_fork");var normal=mc.getItemRenderer().getModel(fork,mc.level,mc.player,0);
            try(var config=new ConfigScope130(Map.of("spork",true))){
                var spork=mc.getItemRenderer().getModel(fork,mc.level,mc.player,0);if(spork==normal||!spork.getParticleIcon().contents().name().equals(ForbiddenMagic.id("item/spork")))throw new IllegalStateException("Spork model override does not render original texture");
            }proof.put("native_spork_model_override",true);
        }
    }
    private static final class Preview extends Screen {
        Preview(){super(Component.literal("Скачок — предмет, жезл и посох"));}
        @Override public void render(GuiGraphics g,int x,int y,float partial){
            g.fill(0,0,width,height,0xFF283142);g.drawCenteredString(font,title,width/2,20,0xFFFFFF);var items=List.of(Content.stack("blink_focus"),wand(false,true,false),wand(true,true,false));
            for(int n=0;n<items.size();n++){g.pose().pushPose();g.pose().translate(width/2-140+n*115,70,0);g.pose().scale(5,5,5);g.renderItem(items.get(n),0,0);g.pose().popPose();}
        }
    }
}
