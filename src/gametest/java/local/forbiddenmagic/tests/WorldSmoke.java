package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import local.forbiddenmagic.*;
import thaumcraft.research.*;
import thaumcraft.wand.*;
import thaumcraft.client.research.*;

@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class WorldSmoke {
    private static int ticks,stage;private static boolean scheduled,done;private static volatile boolean granted;
    private static final List<String> captures=new ArrayList<>();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.worldSmoke"))return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;
        if(!scheduled){scheduled=true;var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.setNoGravity(true);
            player.teleportTo(player.getX(),100,player.getZ());
            var level=player.serverLevel();level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);var base=player.blockPosition().offset(0,-5,10);
            for(int x=-12;x<=12;x++)for(int z=-6;z<=12;z++)level.setBlockAndUpdate(base.offset(x,-1,z),net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());
            for(int x=-12;x<=12;x++)for(int y=0;y<9;y++)for(int z=-6;z<=12;z++)level.removeBlock(base.offset(x,y,z),false);
            var cagePos=base.offset(0,0,-2);level.setBlockAndUpdate(cagePos,Completion.block("wrath_cage").defaultBlockState());var cage=(WrathCage.Cage)level.getBlockEntity(cagePos);cage.mob="minecraft:pig";cage.sync();
            var treePos=base.offset(-6,0,4);level.setBlockAndUpdate(treePos.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());level.setBlockAndUpdate(treePos,Completion.block("tainted_sapling").defaultBlockState());Ecology.grow(level,treePos,level.random);
            String[] samples={"tainted_planks","tainted_stone","tainted_bricks","nether_star_block","arcane_cake","umbral_rose_bush"};
            for(int n=0;n<samples.length;n++){var at=base.offset(n*2-5,0,-4);var state=Completion.block(samples[n]).defaultBlockState();if(state.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock){level.setBlockAndUpdate(at.below(),net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());net.minecraft.world.level.block.DoublePlantBlock.placeAt(level,state,at,3);}else level.setBlockAndUpdate(at,state);}
            if(ForbiddenMagic.loaded("botania")){int n=0;for(var block:BotanyContent.FLOATERS.values())level.setBlockAndUpdate(base.offset(3+n++*2,1,1),block.get().defaultBlockState());}
            var villager=new net.minecraft.world.entity.npc.Villager(net.minecraft.world.entity.EntityType.VILLAGER,level);villager.setVillagerData(villager.getVillagerData().setProfession(Heretic.JOB.get()).setLevel(3));villager.setNoAi(true);villager.moveTo(base.getX()+3.5,base.getY(),base.getZ()-1.5,180,0);level.addFreshEntity(villager);
            player.setXRot(25);player.setYRot(0);player.setYHeadRot(0);
            for(var a:thaumcraft.aspect.Aspect.getEnabledAspects())Knowledge.get(player).discoverAspect(a);
            for(var research:ResearchCategories.allResearch())Knowledge.get(player).addResearch(research.key());
            Knowledge.sync(player);granted=true;
        });return;}
        if(!granted||!Knowledge.get(mc.player).isResearchComplete("FM_ELDRITCHORB"))return;
        ticks++;
        if(ticks==40){
            switch(stage){
                case 0->mc.setScreen(new Atlas());
                case 1->tree(mc,0,0);
                case 2->tree(mc,0,8);
                case 3->tree(mc,0,-7);
                case 4->ResearchPageScreen.open(ResearchCategories.getResearch("FM_EUCLIDAISY"));
                case 5->ResearchPageScreen.open(ResearchCategories.getResearch("FM_ELDRITCHORB"));
                case 6->ResearchPageScreen.open(ResearchCategories.getResearch("FM_SANITY"));
                case 7->{mc.setScreen(null);mc.player.setXRot(35);mc.player.setYRot(0);mc.player.setYHeadRot(0);}
                case 8->ResearchPageScreen.open(ResearchCategories.getResearch("FM_CAGE"));
                case 9->ResearchPageScreen.open(ResearchCategories.getResearch("FM_ENCH_IMPACT"));
                case 10->ResearchPageScreen.open(ResearchCategories.getResearch("FM_MORPH"));
                case 11->ResearchPageScreen.open(ResearchCategories.getResearch("FM_BLOODPOTIONS"));
            }
        }
        if(ticks==65){
            String name=List.of("atlas","apocrypha-core","botania-tree","blood-tree","euclidaisy-page","orb-page","ritual-page","completion-world","cage-page","impact-page","morph-page","flasks-page").get(stage);
            Screenshot.grab(mc.gameDirectory,"fm-"+name+".png",mc.getMainRenderTarget(),message->captures.add(name));
        }
        if(ticks>=90){
            ticks=0;stage++;
            if(stage==12){
                done=true;Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"port-world-client-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",captures.size()==12?"PASS":"FAIL","research_completed_synced",true,"captures",captures)));
                mc.stop();
            }
        }
    }
    private static void tree(Minecraft mc,int x,int y)throws Exception{
        var selected=ResearchBrowserScreen.class.getDeclaredField("selectedCategory");selected.setAccessible(true);selected.set(null,"FORBIDDEN");
        ResearchBrowserScreen.lastX=x;ResearchBrowserScreen.lastY=y;mc.setScreen(new ResearchBrowserScreen());
    }
    private static final class Atlas extends Screen{
        private final List<ItemStack> items=new ArrayList<>();
        Atlas(){super(Component.literal("Forbidden Magic 1.0.0"));Content.ALL.values().forEach(item->items.add(new ItemStack(item.get())));items.add(WrathCage.crystal("minecraft:pig"));
            for(var rod:WandRegistry.rods())if(rod.tag().startsWith("fm_"))items.add(Content.wandExample(rod));
        }
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float tick){
            graphics.fill(0,0,width,height,0xFF19241F);graphics.drawCenteredString(font,title,width/2,18,0xFFFFFF);
            int left=(width-12*40)/2;
            for(int n=0;n<items.size();n++){
                int x=left+n%12*40,y=50+n/12*40;graphics.fill(x-3,y-3,x+27,y+27,0xFF35473D);
                graphics.pose().pushPose();graphics.pose().translate(x,y,0);graphics.pose().scale(1.4F,1.4F,1);graphics.renderItem(items.get(n),0,0);graphics.pose().popPose();
            }
        }
    }
}
