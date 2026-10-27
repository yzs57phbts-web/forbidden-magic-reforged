package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.research.*;
import thaumcraft.client.research.*;

/** Dedicated disposable integrated world, synchronised native knowledge at three progression stages. */
@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class ResearchWorldSmoke {
    private static int ticks,stage;private static boolean done,requested;private static volatile int synced=-1;
    private static final List<String> captures=new ArrayList<>();
    private static final List<String> names=List.of("fresh-common","fresh-nether","fresh-schools","mid-nether","mid-taint","complete-common","complete-nether","complete-enchants","complete-taint","complete-blood","complete-botania","enchantment-page");
    private static final int[][] positions={{-2,-7},{-9,-5},{-1,0},{-9,-5},{-9,2},{-2,-7},{-9,-5},{-6,-5},{-9,2},{-1,-2},{0,4},{0,0}};
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.researchSmoke"))return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null)return;
        if(!requested){requested=true;var id=mc.player.getUUID();int target=stage;mc.getSingleplayerServer().execute(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setNoGravity(true);var k=Knowledge.get(p);k.clearResearch();ResearchManager.grantAutoUnlock(p);
            if(target>=3&&target<5){for(String key:List.of("THAUMIUM","INFUSION","INFUSIONENCHANTMENT","ETHEREALBLOOM","FM_FORK","@FM_HELLFIRE"))k.addResearch(key);}
            if(target>=5)for(var r:ResearchCategories.allResearch())k.addResearch(r.key());
            Knowledge.sync(p);synced=target;
        });return;}
        if(synced!=stage)return;
        ticks++;
        if(ticks==30){
            var missing=new ArrayList<String>();for(var r:ResearchCategories.get("FORBIDDEN").research())if(r.iconTexture()!=null&&mc.getResourceManager().getResource(r.iconTexture()).isEmpty())missing.add(r.key());
            if(mc.getResourceManager().getResource(ResourceLocation.parse("forbiddenmagic:textures/misc/runecircle.png")).isEmpty())missing.add("background");if(!missing.isEmpty())throw new IllegalStateException("Missing artwork "+missing);
            if(stage==11)ResearchPageScreen.open(ResearchCategories.getResearch("FM_ENCH_WRATH"));
            else{var selected=ResearchBrowserScreen.class.getDeclaredField("selectedCategory");selected.setAccessible(true);selected.set(null,"FORBIDDEN");ResearchBrowserScreen.lastX=positions[stage][0];ResearchBrowserScreen.lastY=positions[stage][1];mc.setScreen(new ResearchBrowserScreen());}
        }
        if(ticks==55){String name=names.get(stage);Screenshot.grab(mc.gameDirectory,"fm110-"+name+".png",mc.getMainRenderTarget(),message->captures.add(name));}
        if(ticks>=75){ticks=0;stage++;requested=false;if(stage==names.size()){
            done=true;Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"research-world-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",captures.size()==12?"PASS":"FAIL","captures",captures,"native_synced_states",List.of("fresh","intermediate","completed"),"missing_research_artwork",List.of())));mc.stop();
        }}
    }
}
