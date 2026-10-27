package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import thaumcraft.research.*;
import thaumcraft.client.research.ResearchPageScreen;

@EventBusSubscriber(modid="forbiddenmagic_tests",value=Dist.CLIENT)
public final class LocalizationWorldSmoke {
    private record Capture(String name,String research,int page,String find){}
    private static final List<Capture> plan=Boolean.getBoolean("forbiddenmagic.modernSmoke")?List.of(
        new Capture("ru-modern-consuming","FM_ENCH_CONSUMING",0,"fm.modern.FM_ENCH_CONSUMING"),
        new Capture("ru-modern-skullaxe","FM_SKULLAXE",0,"fm.modern.FM_SKULLAXE"),
        new Capture("ru-modern-shards","FM_SHARDS",0,"fm.modern.FM_SHARDS"),
        new Capture("ru-modern-tree","FM_TAINTTREE",0,"fm.modern.FM_TAINTTREE"),
        new Capture("ru-modern-roses","FM_ROSES",0,"fm.modern.FM_ROSES"),
        new Capture("ru-modern-cage","FM_CAGE",0,"fm.modern.FM_CAGE"),
        new Capture("ru-morph-original","FM_MORPH",0,null),new Capture("ru-blink-original","FM_BLINK",0,null),
        new Capture("ru-cage-original","FM_CAGE",0,null),new Capture("ru-tree-original","FM_TAINTTREE",0,null),
        new Capture("en-modern-consuming","FM_ENCH_CONSUMING",0,"fm.modern.FM_ENCH_CONSUMING"),
        new Capture("en-modern-cage","FM_CAGE",0,"fm.modern.FM_CAGE")) : List.of(
        new Capture("ru-shards-illustration","FM_SHARDS",0,null),new Capture("ru-shards-details","FM_SHARDS",2,null),
        new Capture("ru-skullaxe","FM_SKULLAXE",0,null),new Capture("ru-ring","FM_RINGFOOD",0,null),
        new Capture("ru-primewell","FM_PRIMEWELL",0,null),new Capture("ru-blink","FM_BLINK",0,null),
        new Capture("ru-terrasteel","CAP_fm_terrasteel",0,null),new Capture("ru-orb-original","FM_ELDRITCHORB",0,null),
        new Capture("ru-orb-note","FM_ELDRITCHORB",0,"fm.note.FM_ELDRITCHORB"),new Capture("ru-fork-unlocked","FM_FORK",0,"forbidden.research_page.FORK.wc"),
        new Capture("en-primewell","FM_PRIMEWELL",0,null),new Capture("en-blink","FM_BLINK",0,null));
    private static int stage,ticks;private static boolean scheduled,done,loading;private static volatile boolean granted;
    private static final List<String> captures=new ArrayList<>();private static final Map<String,Object> layoutProof=new LinkedHashMap<>();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event)throws Exception{
        if(done||!Boolean.getBoolean("forbiddenmagic.localizationSmoke"))return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||loading)return;
        if(!scheduled){scheduled=true;var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.setNoGravity(true);for(var a:thaumcraft.aspect.Aspect.getEnabledAspects())Knowledge.get(p).discoverAspect(a);for(var r:ResearchCategories.allResearch())Knowledge.get(p).addResearch(r.key());Knowledge.sync(p);granted=true;});return;}
        if(!granted||!Knowledge.get(mc.player).isResearchComplete("FM_CAGE"))return;
        String language=stage<10?"ru_ru":"en_us";
        if(!mc.getLanguageManager().getSelected().equals(language)){loading=true;mc.getLanguageManager().setSelected(language);mc.options.languageCode=language;mc.reloadResourcePacks().thenRun(()->loading=false);return;}
        ticks++;
        if(ticks==30){
            if(stage==0||stage==10)layoutProof.put(language,auditAll(mc));
            var shot=plan.get(stage);var item=ResearchCategories.getResearch(shot.research());int target=shot.page();
            if(shot.find()!=null){var all=displayPages(new ResearchPageScreen(item,0));boolean found=false;for(int n=0;n<all.size();n++){var source=source(all.get(n));String key=source instanceof ResearchPage.Text t?t.text():source instanceof ResearchPage.ConcealedText c?c.text():"";if(key.equals(shot.find())){target=n-n%2;found=true;break;}}if(!found)throw new IllegalStateException("Missing requested text "+shot.find());}
            mc.setScreen(new ResearchPageScreen(item,target));
        }
        if(ticks==55){String name=plan.get(stage).name();Screenshot.grab(mc.gameDirectory,(Boolean.getBoolean("forbiddenmagic.modernSmoke")?"fm120-":"fm111-")+name+".png",mc.getMainRenderTarget(),message->captures.add(name));}
        if(ticks>=75){ticks=0;stage++;if(stage==plan.size()){done=true;Files.writeString(Path.of(mc.gameDirectory.getAbsolutePath(),"localization-world-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status",captures.size()==12?"PASS":"FAIL","captures",captures,"layout",layoutProof)));mc.stop();}}
    }
    private static List<?> displayPages(ResearchPageScreen screen)throws Exception{var field=ResearchPageScreen.class.getDeclaredField("pages");field.setAccessible(true);return (List<?>)field.get(screen);}
    private static ResearchPage source(Object display)throws Exception{var m=display.getClass().getDeclaredMethod("source");m.setAccessible(true);return (ResearchPage)m.invoke(display);}
    private static Map<String,Object> auditAll(Minecraft mc)throws Exception{
        int count=0,pages=0,textPages=0,pictures=0;var height=Class.forName("thaumcraft.client.research.BookText$Line").getDeclaredMethod("height");height.setAccessible(true);
        for(var r:ResearchCategories.get("FORBIDDEN").research()){
            if(r.isVirtual())continue;count++;if(!I18n.exists(r.nameKey())||!I18n.exists(r.textKey()))throw new IllegalStateException("Untranslated heading "+r.key());
            var display=displayPages(new ResearchPageScreen(r,0));pages+=display.size();
            for(int n=0;n<display.size();n++){
                var p=display.get(n);var source=source(p);String key=source instanceof ResearchPage.Text t?t.text():source instanceof ResearchPage.ConcealedText c?c.text():null;
                if(key==null)continue;textPages++;if(!I18n.exists(key))throw new IllegalStateException("Missing live translation "+key);
                var accessor=p.getClass().getDeclaredMethod("lines");accessor.setAccessible(true);var lines=(List<?>)accessor.invoke(p);int used=0;
                for(var line:lines){used+=(int)height.invoke(line);if(line.getClass().getSimpleName().equals("Picture")){pictures++;var texture=line.getClass().getDeclaredMethod("texture");texture.setAccessible(true);if(mc.getResourceManager().getResource((ResourceLocation)texture.invoke(line)).isEmpty())throw new IllegalStateException("Missing inline illustration "+key);}}
                if(used>(n==0?166:191))throw new IllegalStateException("Book text overflows "+r.key()+" page"+n+" height"+used);
            }
        }
        if(count!=47||pictures==0)throw new IllegalStateException("Incomplete native book audit");
        // Native buildPages must withhold the fork appendix until the dependent research is complete.
        var k=Knowledge.get(mc.player);k.removeResearch("FM_CAGE");var locked=displayPages(new ResearchPageScreen(ResearchCategories.getResearch("FM_FORK"),0));boolean leaked=false;
        for(var p:locked)if(source(p) instanceof ResearchPage.ConcealedText)leaked=true;k.addResearch("FM_CAGE");if(leaked)throw new IllegalStateException("Fork appendix shown too early");
        return Map.of("visible_research",count,"display_pages",pages,"text_display_pages",textPages,"inline_pictures",pictures,"untranslated_keys",0,"overflow_pages",0,"fork_appendix_hidden_until_cage",true);
    }
}
