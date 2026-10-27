package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_configtests") @PrefixGameTestTemplate(false)
public final class Config130Tests {
    @GameTest(batch="config130",template="test/platform",timeoutTicks=300)
    public static void actualStartupSettings(GameTestHelper h)throws Exception{
        var l=h.getLevel();String scenario=System.getProperty("forbiddenmagic.configScenario","");var proof=new LinkedHashMap<String,Object>();
        check(Files.isRegularFile(Path.of("config",LegacyConfig.FILE)),"Configuration file not loaded in game directory");
        if(scenario.equals("disabled")){
            check(LegacyConfig.bool("noLust")&&!LegacyConfig.bool("wrathCage")&&!LegacyConfig.bool("crossMod")&&!LegacyConfig.bool("enchanting"),"Startup config ignored");
            for(String key:List.of("FM:CAKE","FM:RING","FM:CAGE","FM:COLLAR","FM:EMERALD","FM:ENCH_WRATH","FM:ENCH_GREEDY","FM:BLOODROD","FM:LIVINGWOOD"))check(TcRecipes.get(key)==null,"Disabled recipe registered "+key);
            for(String key:List.of("FM_ARCANECAKE","FM_RINGFOOD","FM_CAGE","FM_COLLAR","FM_EMERALD","FM_ENCH_WRATH","ROD_fm_blood","ROD_fm_livingwood"))check(ResearchCategories.getResearch(key)==null,"Disabled research registered "+key);
            for(String key:List.of("livingwood_activation","dreamwood_activation","blood_rod_activation","flask/taint","flask/blood_seal","floating_euclidaisy"))check(l.getRecipeManager().byKey(ForbiddenMagic.id(key)).isEmpty(),"Disabled native recipe loaded "+key);
            var shard=ResearchCategories.getResearch("FM_SHARDS");check(shard.pages().stream().anyMatch(page->page instanceof ResearchPage.Text t&&t.text().equals("forbidden.research_page.NETHERSHARDS.2b")),"NoLust book variant not restored");
            check(!CageRules.allowed(EntityType.PIG)&&!LegacyConfig.itemVisible("lust_shard"),"Disabled acquisition still active");proof.put("disabled_native_recipes_research_and_mechanics",true);
        }else if(scenario.equals("custom")){
            check(LegacyConfig.integer("wrathCost")==2&&LegacyConfig.integer("wrathEff")==7&&LegacyConfig.integer("bloodvis")==9&&LegacyConfig.integer("manavis")==13,"Numeric settings ignored");
            var cake=(InfusionRecipe)TcRecipes.get("FM:CAKE");var ingredients=cake.components().stream().map(i->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(i.displayStacks().getFirst().getItem()).toString()).toList();
            check(Collections.frequency(ingredients,"forbiddenmagic:nether_star_block")==2&&Collections.frequency(ingredients,"minecraft:honey_bottle")==1,"Hardcore cake failed or lost authorized honey");
            var ring=(ShapedArcaneRecipe)TcRecipes.get("FM:RING");check(ring.grid().stream().flatMap(i->i.displayStacks().stream()).anyMatch(s->s.is(Completion.block("nether_star_block").asItem())),"Hardcore ring not using star block");
            check(Content.stack("crystal_scribing_tools").getMaxDamage()==50,"Easy ink config not active at startup");var research=ResearchCategories.getResearch("FM_CRYSTALWELL");check(research.pages().stream().anyMatch(page->page instanceof ResearchPage.Text t&&t.text().equals("forbidden.research_page.CRYSTALWELL.1b")),"Easy ink original text not selected");
            check(Content.stack("diabolists_fork").getDescriptionId().equals("item.forbiddenmagic.spork"),"Spork switch not active");check(research.textKey().startsWith("fm.tag."),"Research tag switch not active");check(LegacyConfig.garbage(new ItemStack(Items.COBBLESTONE))&&!LegacyConfig.garbage(new ItemStack(Items.DIRT)),"Garbage override ignored");
            var p=HeadlessPlayer.create(l,"config130");l.addNewPlayer(p);for(var r:ResearchCategories.allResearch())Knowledge.get(p).addResearch(r.key());
            if(ForbiddenMagic.loaded("bloodmagic"))proof.put("lp_costs",LegacyBlood130Tests.configuredCosts(p));
            if(ForbiddenMagic.loaded("botania"))proof.put("mana_costs",LegacyBotany130Tests.configuredCosts(h,p));
            proof.put("cage_cost_and_efficiency",configuredCage(h));
            p.discard();proof.put("hardcore_easy_spork_tag_and_cost_settings",true);
        }else throw new IllegalStateException("Unknown config test scenario "+scenario);
        proof.put("scenario",scenario);proof.put("status","PASS");Files.writeString(Path.of("config-130-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();
    }
    private static Map<String,Object> configuredCage(GameTestHelper h)throws Exception{
        var l=h.getLevel();var pos=h.absolutePos(new net.minecraft.core.BlockPos(3,10,3));
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.getChunkAt(pos.offset(x,0,z));l.setBlockAndUpdate(pos.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(pos.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
        l.setBlockAndUpdate(pos,Completion.block("wrath_cage").defaultBlockState());var cage=(WrathCage.Cage)l.getBlockEntity(pos);cage.mob="minecraft:pig";cage.fuel=0;
        check(cage.addEssentia(cage.species(),2,null)==2,"Configured cage fuel rejected");int total=0;
        for(int n=0;n<100;n++){cage.delay=0;var emitted=DropCapture.mobs(l,pos,cage::tick);total+=emitted.size();emitted.forEach(net.minecraft.world.entity.Entity::discard);}
        check(total==7&&cage.fuel==0&&cage.amount(cage.species())==0,"Cage2 essentia/7 spawns ignored: "+total);
        int free=0;try(var config=new ConfigScope130(Map.of("wrathCost",0))){
            check(cage.getSuctionAmount(null)==0&&!cage.doesContainerAccept(cage.species()),"Free cage still consumes essentia");
            for(int n=0;n<30&&free<5;n++){cage.delay=0;var emitted=DropCapture.mobs(l,pos,cage::tick);free+=emitted.size();emitted.forEach(net.minecraft.world.entity.Entity::discard);}
            check(free>=5&&cage.fuel==0,"Free cage stopped without fuel");
        }l.removeBlock(pos,false);return Map.of("essentia",2,"spawns",total,"free_mode_spawns",free);
    }
}
