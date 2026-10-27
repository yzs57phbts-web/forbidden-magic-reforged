package local.forbiddenmagic.tests;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class Visual140Client {
    private static final List<String> PLANTS=List.of("tainted_sapling","umbral_rose","umbral_rose_bush","euclidaisy","whisperweed","tainthistle","bloodthorn");
    private static final List<String> FLOATERS=List.of("euclidaisy","whisperweed","tainthistle","bloodthorn");
    private static final List<String> PARTS=List.of("profaned_rod","neutronium_rod","neutronium_staff_core","orichalcum_cap");
    private static final List<String> PLAN=new ArrayList<>();
    static{PLAN.add("icons");PLAN.add("components");PLAN.addAll(PLANTS);PLAN.addAll(List.of("floating_euclidaisy","native-floating","rapier-right","rapier-left","rapier-third","fork-right","fork-left","fork-third","native-trident","floating-world"));}
    private static boolean setup,done,loading;private static volatile boolean ready;private static int stage,ticks;private static final Map<String,Object> proof=new LinkedHashMap<>();private static final List<String> captures=new ArrayList<>();
    private static String expectedMain="minecraft:air",expectedOff="minecraft:air";private static volatile boolean equipmentReady;
    private static final Map<String,Object> equipment=new LinkedHashMap<>();
    private static String rendered(Minecraft mc,String field)throws Exception{var f=net.minecraft.client.renderer.ItemInHandRenderer.class.getDeclaredField(field);f.setAccessible(true);return BuiltInRegistries.ITEM.getKey(((ItemStack)f.get(mc.gameRenderer.itemInHandRenderer)).getItem()).toString();}
    private static ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.contains(":")?id:"forbiddenmagic:"+id)));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.visual140Smoke"))return;var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||loading)return;
        mc.options.pauseOnLostFocus=false;
        mc.player.getInventory().selected=0;
        if(!mc.getLanguageManager().getSelected().equals("ru_ru")){loading=true;mc.getLanguageManager().setSelected("ru_ru");mc.options.languageCode="ru_ru";mc.reloadResourcePacks().thenRun(()->loading=false);return;}
        if(!setup){setup=true;var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(.5,82,-2.5);var l=p.serverLevel();l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
            for(int x=-7;x<=7;x++)for(int z=-5;z<=9;z++){var pos=new BlockPos(x,79,z);l.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());for(int y=80;y<=84;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
            for(int n=0;n<4;n++){l.setBlockAndUpdate(new BlockPos(-3+2*n,80,4),BotanyContent.FLOATERS.get(FLOATERS.get(n)).get().defaultBlockState());var id=ResourceLocation.parse("botania:floating_"+List.of("endoflame","rosa_arcana","hydroangeas","thermalily").get(n));l.setBlockAndUpdate(new BlockPos(-3+2*n,80,6),BuiltInRegistries.BLOCK.get(id).defaultBlockState());}ready=true;
        });return;}if(!ready)return;
        ticks++;String name=PLAN.get(stage);
        if(ticks==1){
            if(stage==0)audit(mc);
            mc.setScreen(null);mc.options.setCameraType(name.endsWith("third")?CameraType.THIRD_PERSON_FRONT:CameraType.FIRST_PERSON);mc.player.setXRot(name.equals("floating-world")?25:8);mc.player.setYRot(0);mc.player.setYHeadRot(0);
            String item=PLANTS.contains(name)||name.startsWith("floating_")?name:name.startsWith("rapier")?"rapier_of_the_strix":name.startsWith("fork")?"diabolists_fork":name.equals("native-floating")?"botania:floating_endoflame":name.equals("native-trident")?"minecraft:trident":"minecraft:air";boolean left=name.endsWith("left");String full=item.contains(":")?item:"forbiddenmagic:"+item;expectedMain=left?"minecraft:air":full;expectedOff=left?full:"minecraft:air";equipmentReady=false;var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);p.getInventory().selected=0;p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,left?ItemStack.EMPTY:stack(item));p.setItemInHand(InteractionHand.OFF_HAND,left?stack(item):ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();p.setXRot(name.equals("floating-world")?25:8);p.setYRot(0);p.setYHeadRot(0);equipmentReady=true;});
            if(name.equals("icons")||name.equals("components"))mc.setScreen(new Sheet(name.equals("components")));
        }
        String main=BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString(),off=BuiltInRegistries.ITEM.getKey(mc.player.getOffhandItem().getItem()).toString();
        if(ticks>1&&(!equipmentReady||!main.equals(expectedMain)||!off.equals(expectedOff))){ticks=2;return;}
        if(ticks>1&&(!rendered(mc,"mainHandItem").equals(expectedMain)||!rendered(mc,"offHandItem").equals(expectedOff))){mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.MAIN_HAND);mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.OFF_HAND);mc.gameRenderer.itemInHandRenderer.tick();ticks=2;return;}
        if(ticks==40){equipment.put(name,Map.of("main",main,"off",off,"rendered_main",rendered(mc,"mainHandItem"),"rendered_off",rendered(mc,"offHandItem")));Screenshot.grab(mc.gameDirectory,"fm140-"+name+".png",mc.getMainRenderTarget(),message->captures.add(name));}
        if(ticks>=55){ticks=0;stage++;if(stage==PLAN.size()){done=true;proof.put("captures",captures);proof.put("equipment_by_capture",equipment);proof.put("status",captures.size()==PLAN.size()?"PASS":"FAIL");Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"visual-140-client-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));mc.stop();}}
    }
    private static void audit(Minecraft mc)throws Exception{
        for(String id:PLANTS){var model=mc.getItemRenderer().getModel(stack(id),mc.level,mc.player,0);if(model==mc.getModelManager().getMissingModel()||model.isGui3d())throw new IllegalStateException("Plant still uses block item model: "+id);double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(var side:Arrays.asList(null,Direction.UP,Direction.DOWN,Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST))for(var q:model.getQuads(null,side,net.minecraft.util.RandomSource.create(1))){var v=q.getVertices();for(int n=2;n<v.length;n+=8){double z=Float.intBitsToFloat(v[n]);min=Math.min(min,z);max=Math.max(max,z);}}if(max-min>.07)throw new IllegalStateException("Plant is crossed block geometry: "+id);}
        var nativeModel=mc.getItemRenderer().getModel(stack("botania:floating_endoflame"),mc.level,mc.player,0);for(String id:FLOATERS){var model=mc.getItemRenderer().getModel(stack("floating_"+id),mc.level,mc.player,0);if(model.getClass()!=nativeModel.getClass())throw new IllegalStateException("Floating flower differs from Botania loader: "+id);}
        var fork=stack("diabolists_fork");var model=mc.getItemRenderer().getModel(fork,mc.level,mc.player,0);for(var context:List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,ItemDisplayContext.FIRST_PERSON_LEFT_HAND,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,ItemDisplayContext.THIRD_PERSON_LEFT_HAND))if(!model.applyTransform(context,new PoseStack(),context.name().endsWith("LEFT_HAND")).isCustomRenderer())throw new IllegalStateException("Fork does not use native trident geometry in "+context);
        if(model.applyTransform(ItemDisplayContext.GUI,new PoseStack(),false).isCustomRenderer()||mc.getItemColors().getColor(fork,0)!=ForkRenderer.COLOUR)throw new IllegalStateException("Fork GUI/tint broken");
        try(var config=new ConfigScope130(Map.of("spork",true))){var spork=mc.getItemRenderer().getModel(fork,mc.level,mc.player,0);if(!spork.getParticleIcon().contents().name().equals(ForbiddenMagic.id("item/spork")))throw new IllegalStateException("Spork override lost");}
        proof.put("plant_sprites_flat",PLANTS);proof.put("floating_loader_matches_botania",FLOATERS);proof.put("four_hand_contexts_native_trident",true);proof.put("fork_colour",Integer.toHexString(ForkRenderer.COLOUR));proof.put("spork_preserved",true);
    }
    private static final class Sheet extends Screen{
        final boolean parts;Sheet(boolean parts){super(Component.literal(parts?"Сохранённые служебные детали":"Иконки растений, парящих цветов и оружия"));this.parts=parts;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xFF313840);g.drawCenteredString(font,title,width/2,8,0xFFFFFF);var ids=new ArrayList<String>();if(parts)ids.addAll(PARTS);else{ids.addAll(PLANTS);for(String n:FLOATERS)ids.add("floating_"+n);for(String n:List.of("endoflame","rosa_arcana","hydroangeas","thermalily"))ids.add("botania:floating_"+n);ids.addAll(List.of("rapier_of_the_strix","diabolists_fork"));}int cols=parts?4:5,step=width/cols;for(int n=0;n<ids.size();n++){var s=stack(ids.get(n));int x=(n%cols)*step+step/2,y=32+(n/cols)*85;g.pose().pushPose();g.pose().translate(x-16,y,0);g.pose().scale(2,2,2);g.renderItem(s,0,0);g.pose().popPose();var lines=font.split(s.getHoverName(),step-8);for(int k=0;k<Math.min(3,lines.size());k++)g.drawCenteredString(font,lines.get(k),x,y+37+k*10,0xFFFFFF);}}
    }
}
