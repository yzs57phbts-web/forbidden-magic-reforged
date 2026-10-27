package local.forbiddenmagic.tests;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class Rapier141Client {
    private static final List<String> PLAN=List.of("right","left","front","back","side","off-side","trident-side","icon","floating-fm","floating-native");
    private static boolean setup,done;private static volatile boolean ready,equipped;private static int stage,ticks;private static final Map<String,Object> proof=new LinkedHashMap<>(),hands=new LinkedHashMap<>();private static final List<String> captures=new ArrayList<>();
    private static String mainExpected,offExpected;
    private static Entity sideCamera;
    private static String id(ItemStack s){return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();}
    private static String cached(Minecraft mc,String field)throws Exception{var f=net.minecraft.client.renderer.ItemInHandRenderer.class.getDeclaredField(field);f.setAccessible(true);return id((ItemStack)f.get(mc.gameRenderer.itemInHandRenderer));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.rapier141Smoke"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;mc.options.pauseOnLostFocus=false;mc.player.getInventory().selected=0;
        if(!setup){setup=true;var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(.5,82,-2.5);p.serverLevel().setDayTime(6000);p.serverLevel().setWeatherParameters(6000,0,false,false);ready=true;});return;}if(!ready)return;
        ticks++;String name=PLAN.get(stage);
        if(ticks==1){
            mc.setScreen(null);mc.setCameraEntity(mc.player);mc.options.setCameraType(name.equals("front")?CameraType.THIRD_PERSON_FRONT:name.equals("back")?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);mc.player.setXRot(8);mc.player.setYRot(0);mc.player.setYHeadRot(0);
            if(name.endsWith("side")){if(sideCamera==null){sideCamera=EntityType.ARMOR_STAND.create(mc.level);sideCamera.setNoGravity(true);sideCamera.setInvisible(true);mc.level.addEntity(sideCamera);}sideCamera.moveTo(1.5,81.4,-2.5,90,12);sideCamera.setOldPosAndRot();}
            if(name.startsWith("floating-")){if(sideCamera==null){sideCamera=EntityType.ARMOR_STAND.create(mc.level);sideCamera.setNoGravity(true);sideCamera.setInvisible(true);mc.level.addEntity(sideCamera);}sideCamera.moveTo(-3,79.4,name.equals("floating-fm")?2.5:4.5,0,48);sideCamera.setOldPosAndRot();}
            boolean left=name.equals("left")||name.equals("off-side"),empty=name.equals("icon")||name.startsWith("floating-");var item=name.equals("trident-side")?Items.TRIDENT:Content.get("rapier_of_the_strix");mainExpected=left||empty?"minecraft:air":BuiltInRegistries.ITEM.getKey(item).toString();offExpected=left?BuiltInRegistries.ITEM.getKey(item).toString():"minecraft:air";equipped=false;var uuid=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);p.getInventory().selected=0;p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,left||empty?ItemStack.EMPTY:new ItemStack(item));p.setItemInHand(InteractionHand.OFF_HAND,left?new ItemStack(item):ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();p.setYRot(0);p.setYHeadRot(0);p.yBodyRot=0;p.setXRot(8);equipped=true;});
            if(name.equals("icon"))mc.setScreen(new Icon());
        }
        if(name.endsWith("side")||name.startsWith("floating-")){mc.setCameraEntity(sideCamera);mc.options.setCameraType(name.endsWith("side")?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);}
        if(ticks>1&&(!equipped||!id(mc.player.getMainHandItem()).equals(mainExpected)||!id(mc.player.getOffhandItem()).equals(offExpected))){ticks=2;return;}
        if(ticks>1&&(!cached(mc,"mainHandItem").equals(mainExpected)||!cached(mc,"offHandItem").equals(offExpected))){mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.MAIN_HAND);mc.gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.OFF_HAND);mc.gameRenderer.itemInHandRenderer.tick();ticks=2;return;}
        if(ticks==20&&stage==0){audit(mc);auditFlowers(mc);}
        if(ticks==35){hands.put(name,Map.of("main",cached(mc,"mainHandItem"),"off",cached(mc,"offHandItem")));Screenshot.grab(mc.gameDirectory,"fm141-"+name+".png",mc.getMainRenderTarget(),message->captures.add(name));}
        if(ticks>=45){ticks=0;stage++;if(stage==PLAN.size()){done=true;proof.put("captures",captures);proof.put("rendered_hands",hands);proof.put("status",captures.size()==PLAN.size()?"PASS":"FAIL");Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"rapier-141-client-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));mc.stop();}}
    }
    @SuppressWarnings("unchecked") private static List<ModelPart.Cube> cubes(ModelPart part)throws Exception{var f=ModelPart.class.getDeclaredField("cubes");f.setAccessible(true);return (List<ModelPart.Cube>)f.get(part);}
    private static void audit(Minecraft mc)throws Exception{
        var root=RapierRenderer.createGeometry();float max=-Float.MAX_VALUE;int cubes=0;for(var part:root.getAllParts().toList())for(var cube:cubes(part)){max=Math.max(max,part.y+cube.maxY*part.yScale);cubes++;}
        float rear=max-RapierRenderer.HAND_Y;var nativeRoot=TridentModel.createLayer().bakeRoot();float nativeMax=cubes(nativeRoot.getChild("pole")).getFirst().maxY;float nativeRear=nativeMax-RapierRenderer.HAND_Y;
        if(rear>3||rear<0||nativeRear<=rear||cubes!=5)throw new IllegalStateException("Rapier has an excessive rear shaft or unexpected prongs: "+rear);
        var blade=root.getChild("blade");var guard=root.getChild("guard");float bladeWidth=cubes(blade).getFirst().maxX-cubes(blade).getFirst().minX,guardWidth=(cubes(guard).getFirst().maxX-cubes(guard).getFirst().minX)*guard.xScale;if(bladeWidth!=1||guardWidth!=6)throw new IllegalStateException("Thin blade/crossguard geometry wrong");
        var stack=Content.stack("rapier_of_the_strix");var model=mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);for(var context:List.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,ItemDisplayContext.FIRST_PERSON_LEFT_HAND,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,ItemDisplayContext.THIRD_PERSON_LEFT_HAND))if(!model.applyTransform(context,new PoseStack(),context.name().endsWith("LEFT_HAND")).isCustomRenderer())throw new IllegalStateException("Rapier not3D in "+context);if(model.applyTransform(ItemDisplayContext.GUI,new PoseStack(),false).isCustomRenderer())throw new IllegalStateException("Inventory icon changed to a long model");
        var handPose=new PoseStack();model.applyTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,handPose,false);handPose.translate(-.5,-.5,-.5);handPose.scale(1,-1,-1);var hand=new org.joml.Matrix4f(handPose.last().pose()).invert().transformPosition(new org.joml.Vector3f()).mul(16);if(Math.abs(hand.y-RapierRenderer.HAND_Y)>.001||hand.y<7.5||hand.y>11)throw new IllegalStateException("Actual native hand transform grips outside the handle: "+hand);
        proof.put("hand_y_from_actual_native_transform",hand.y);proof.put("hand_inside_hilt",true);
        proof.put("rear_pixels",rear);proof.put("native_trident_rear_pixels",nativeRear);proof.put("blade_width_pixels",bladeWidth);proof.put("guard_width_pixels",guardWidth);proof.put("mesh_parts",cubes);proof.put("four_3d_hand_contexts",true);proof.put("original_inventory_icon",true);
    }
    private static int islands(Minecraft mc,net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.BlockPos pos,net.minecraft.client.renderer.RenderType... override){
        var model=mc.getBlockRenderer().getBlockModel(state);var data=model.getModelData(mc.level,pos,state,net.neoforged.neoforge.client.model.data.ModelData.EMPTY);Iterable<net.minecraft.client.renderer.RenderType> layers=override.length==0?model.getRenderTypes(state,net.minecraft.util.RandomSource.create(1),data):List.of(override);int count=0;for(var layer:layers)for(var side:Arrays.asList(null,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.DOWN,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST))for(var q:model.getQuads(state,side,net.minecraft.util.RandomSource.create(1),data,layer)){String sprite=q.getSprite().contents().name().toString();if(sprite.contains("island_")||sprite.equals("minecraft:block/dirt"))count++;}return count;
    }
    private static void auditFlowers(Minecraft mc){
        var pos=new net.minecraft.core.BlockPos(-3,80,4);var fm=BotanyContent.FLOATERS.get("euclidaisy").get();var state=fm.defaultBlockState();var nativeBlock=BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("botania:floating_endoflame"));int expected=islands(mc,nativeBlock.defaultBlockState(),pos.south(2));int actual=islands(mc,state,pos);if(expected==0||actual!=expected)throw new IllegalStateException("Floating island duplicate render passes: FM="+actual+" native="+expected);
        int old=islands(mc,state,pos,net.minecraft.client.renderer.RenderType.solid(),net.minecraft.client.renderer.RenderType.cutout());
        if(old<=actual)throw new IllegalStateException("Old solid+cutout regression control did not duplicate the island: "+old+" vs "+actual);
        if(!state.getOffset(mc.level,pos).equals(net.minecraft.world.phys.Vec3.ZERO))throw new IllegalStateException("Floating flower inherited poppy XZ offset");proof.put("floating_island_quads_native",expected);proof.put("floating_island_quads_fixed",actual);proof.put("floating_island_quads_old",old);proof.put("floating_no_poppy_offset",true);
    }
    private static final class Icon extends Screen{
        Icon(){super(Component.literal("Рапира — иконка в инвентаре сохранена"));}
        @Override public void render(GuiGraphics g,int x,int y,float partial){g.fill(0,0,width,height,0xFF313840);g.drawCenteredString(font,title,width/2,25,0xFFFFFF);g.pose().pushPose();g.pose().translate(width/2-40,height/2-40,0);g.pose().scale(5,5,5);g.renderItem(Content.stack("rapier_of_the_strix"),0,0);g.pose().popPose();}
    }
}
