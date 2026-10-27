package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import java.io.*;
import com.google.gson.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import local.forbiddenmagic.*;
import thaumcraft.aspect.*;
import thaumcraft.research.*;
import thaumcraft.network.PlayerCompletePayload;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class ResearchTests {
    private static List<String> list(JsonObject r,String k){var out=new ArrayList<String>();r.getAsJsonArray(k).forEach(v->out.add(v.getAsString()));return out;}
    private static boolean enabled(JsonObject r){if(r.has("mods"))for(var v:r.getAsJsonArray("mods"))if(!ForbiddenMagic.loaded(v.getAsString()))return false;if(r.has("any_mods"))return r.getAsJsonArray("any_mods").asList().stream().anyMatch(v->ForbiddenMagic.loaded(v.getAsString()));return true;}
    @GameTest(batch="research",template="test/platform",timeoutTicks=400)
    public static void originalProgression(GameTestHelper h)throws Exception{
        try{var baseline=JsonParser.parseReader(new InputStreamReader(PortRecipes.class.getResourceAsStream("/forbiddenmagic/research-0575-baseline.json"))).getAsJsonObject();
        int count=0,visible=0,textures=0;var positions=new HashSet<String>();
        for(var value:baseline.getAsJsonArray("nodes")){
            var b=value.getAsJsonObject();String key=b.get("key").getAsString();var r=ResearchCategories.getResearch(key);
            if(!enabled(b)){check(r==null,"Disabled branch present "+key);continue;}
            count++;check(r!=null,"Missing original research "+key);
            check(r.column()==b.get("x").getAsInt()&&r.row()==b.get("y").getAsInt()&&r.complexity()==b.get("complexity").getAsInt(),"Layout/difficulty "+key);
            check(r.parents().equals(list(b,"parents"))&&r.parentsHidden().equals(list(b,"parents_hidden"))&&r.siblings().equals(list(b,"siblings")),"Original graph links "+key);
            for(String link:java.util.stream.Stream.of(r.parents(),r.parentsHidden(),r.siblings()).flatMap(Collection::stream).toList())check(ResearchCategories.getResearch(link)!=null,"Unresolved link "+key+" -> "+link);
            var flags=Map.of("auto",r.isAutoUnlock(),"hidden",r.isHidden(),"concealed",r.isConcealed(),"secondary",r.isSecondary(),"round",r.isRound(),"stub",r.isStub(),"special",r.isSpecial(),"virtual",r.isVirtual(),"lost",r.isLost());
            flags.forEach((k,v)->check(v==b.get(k).getAsBoolean(),"Flag "+key+" "+k));
            check(r.tags().equals(PortRecipes.aspects(b.getAsJsonObject("aspects"))),"Original research aspect cost "+key);
            check(r.warp()==b.get("warp").getAsInt(),"Original research warp "+key);
            check(r.aspectTriggers().stream().map(Aspect::getTag).toList().equals(list(b,"triggers")),"Discovery trigger "+key);
            if(!r.isVirtual()){visible++;check(positions.add(r.column()+","+r.row()),"Overlapping nodes "+key);check(r.iconItem()==null||!r.iconItem().create().is(Items.ENCHANTED_BOOK),"Generic enchanted book "+key);}
            if(b.has("icon_texture")){textures++;check(r.iconTexture().toString().equals(b.get("icon_texture").getAsString()),"Original texture "+key);}
        }
        check(PortRecipes.RESEARCH.stream().map(ResearchCategories::getResearch).filter(r->!r.isVirtual()).count()==visible,"Invented visible research nodes");
        var p=HeadlessPlayer.create(h.getLevel(),"research110");var k=Knowledge.get(p);k.clearResearch();ResearchManager.grantAutoUnlock(p);
        long auto=k.research().stream().filter(PortRecipes.RESEARCH::contains).count();check(auto==(ForbiddenMagic.loaded("botania")||ForbiddenMagic.loaded("bloodmagic")?3:2)+(ForbiddenMagic.loaded("botania")?1:0)+(ForbiddenMagic.loaded("bloodmagic")?1:0),"Fresh auto unlocks");
        check(!ResearchManager.hasRequisites(p,"FM_MORPH"),"Morph opens early");k.addResearch("FM_SHARDS");check(!ResearchManager.hasRequisites(p,"FM_MORPH"),"Hidden parents ignored");k.addResearch("THAUMIUM");k.addResearch("INFUSIONENCHANTMENT");check(ResearchManager.hasRequisites(p,"FM_MORPH"),"Morph stays locked");
        check(!ResearchManager.hasRequisites(p,"FM_CAGE"),"Cage before fork");var cage=ResearchCategories.getResearch("FM_CAGE");for(String s:cage.parents())k.addResearch(s);check(!ResearchManager.hasRequisites(p,"FM_CAGE"),"Cage hidden parents ignored");for(String s:cage.parentsHidden())k.addResearch(s);check(ResearchManager.hasRequisites(p,"FM_CAGE"),"Cage stays locked");
        check(ResearchManager.createClue(p,ItemStack.EMPTY,new AspectList().add(Aspect.getAspect("infernus"),1)),"No hellfire clue");check(k.isResearchComplete("@FM_HELLFIRE"),"Wrong hellfire clue");
        check(ResearchManager.createClue(p,ItemStack.EMPTY,new AspectList().add(Aspect.getAspect("luxuria"),1)),"No collar clue");check(k.isResearchComplete("@FM_COLLAR"),"Wrong collar clue");
        var emerald=ResearchCategories.getResearch("FM_EMERALD");for(String s:emerald.parents())k.addResearch(s);for(var a:emerald.tags().getAspects())k.setAspectPool(a,100);
        ResearchManager.handlePlayerComplete(new PlayerCompletePayload("FM_EMERALD",ResearchManager.ACTION_PURCHASE),p);check(k.isResearchComplete("FM_EMERALD"),"Native secondary purchase");
        p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(Items.PAPER,2));var ink=new ItemStack(thaumcraft.registry.ModItems.SCRIBING_TOOLS.get());p.getInventory().setItem(1,ink);
        ResearchManager.handlePlayerComplete(new PlayerCompletePayload("FM_MORPH",ResearchManager.ACTION_CREATE_NOTE),p);check(ResearchNotes.findNoteSlot(p,"FM_MORPH")>=0&&p.getInventory().getItem(0).getCount()==1&&ink.getDamageValue()==1,"Native note/ink/paper");
        if(ForbiddenMagic.loaded("botania")){ResearchManager.completeWithSiblings(p,"ROD_fm_dreamwood_staff");check(k.isResearchComplete("ROD_fm_dreamwood"),"Dreamwood virtual sibling");}
        k.clearResearch();k.addResearch("FM_RINGFOOD");check(!ResearchMigration.migrate(p)&&!k.isResearchComplete("FM_ARCANECAKE"),"New ring bypasses cake");k.addResearch("FM_GLUTTONY");k.addResearch("INFUSION");check(ResearchMigration.migrate(p)&&k.isResearchComplete("FM_ARCANECAKE")&&k.isResearchComplete("INFUSION"),"Old save migration");check(!ResearchMigration.migrate(p),"Migration not idempotent");
        var encoded=PlayerKnowledge.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,k).getOrThrow();check(PlayerKnowledge.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow().research().equals(k.research()),"Migrated save codec");
        Files.writeString(Path.of("research-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","original_nodes",count,"visible",visible,"texture_icons",textures,"fresh_auto",auto,"native_secondary_purchase",true,"native_note_consumption",true,"aspect_discovery_clues",true,"hidden_prerequisites",true,"save_migration",true)));
        h.succeed();}catch(Exception e){e.printStackTrace();throw e;}
    }
}
