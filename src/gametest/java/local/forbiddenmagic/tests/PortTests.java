package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import com.google.gson.GsonBuilder;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import local.forbiddenmagic.*;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;
import thaumcraft.wand.*;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class PortTests {
    public static void check(boolean yes,String message){if(!yes)throw new IllegalStateException(message);}
    @GameTest(batch="port",template="test/platform",timeoutTicks=400)
    public static void playablePort(GameTestHelper h)throws Exception{
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"fm-verifier"));
        h.getLevel().addNewPlayer(player);player.setGameMode(GameType.SURVIVAL);
        var proof=new LinkedHashMap<String,Object>();
        // Flowers add 3 with Botania alone, 4 with both; the shared count is checked by actual registry content.
        check(Content.ALL.values().stream().noneMatch(i->i.get()==Items.AIR),"Unregistered content");
        proof.put("registered_items_blocks",Content.ALL.size());
        for(var aspect:Aspect.getEnabledAspects())Knowledge.get(player).discoverAspect(aspect);
        for(var research:ResearchCategories.allResearch())Knowledge.get(player).addResearch(research.key());
        int parents=0,arcane=0,infusion=0;
        for(String key:PortRecipes.RESEARCH){
            var research=ResearchCategories.getResearch(key);check(research!=null,"Missing research "+key);
            for(String parent:research.parents()){check(ResearchCategories.getResearch(parent)!=null,"Missing parent "+key+" -> "+parent);parents++;}
        }
        for(String key:PortRecipes.RECIPES){
            var recipe=TcRecipes.get(key);check(recipe!=null,"Missing recipe "+key);
            var result=switch(recipe){case InfusionRecipe r->r.result();case ArcaneRecipe r->r.result();case CrucibleRecipe r->r.result();default->TcStack.NONE;};
            check(recipe instanceof InfusionEnchantmentRecipe||!result.create().isEmpty(),"Empty recipe output "+key);
            if(recipe instanceof InfusionEnchantmentRecipe r)check(r.components().stream().allMatch(TcIngredient::exists),"No enchantment part "+key);
            if(recipe instanceof InfusionRecipe r){
                check(r.central().exists(),"No infusion central "+key);check(r.components().stream().allMatch(TcIngredient::exists),"No infusion part "+key);
                var inputs=r.components().stream().map(i->i.displayStacks().getFirst()).toList();
                var plan=InfusionCrafting.findRecipe(inputs,r.central().displayStacks().getFirst(),player);
                check(plan!=null&&plan.key().equals(key),"Native infusion match failed "+key);infusion++;
            }else if(recipe instanceof ArcaneRecipe r){
                var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));
                if(r instanceof ShapedArcaneRecipe s){for(int row=0;row<s.height();row++)for(int col=0;col<s.width();col++){
                    var i=s.grid().get(col+row*s.width());check(i.exists(),"No shaped ingredient "+key);if(!(i instanceof TcIngredient.Empty))grid.set(col+row*3,i.displayStacks().getFirst());
                }}else if(r instanceof ShapelessArcaneRecipe s){int slot=0;for(var i:s.ingredients()){check(i.exists(),"No shapeless ingredient "+key);grid.set(slot++,i.displayStacks().getFirst());}}
                var match=ArcaneCrafting.find(grid,player);check(match!=null&&ItemStack.isSameItemSameComponents(match.result(),r.result().create()),"Native arcane match failed "+key);arcane++;
            }
        }
        proof.put("research_entries",PortRecipes.RESEARCH.size());proof.put("research_parent_links",parents);proof.put("native_arcane_matches",arcane);proof.put("native_infusion_matches",infusion);
        var benchPos=h.absolutePos(new net.minecraft.core.BlockPos(8,5,8));h.getLevel().setBlockAndUpdate(benchPos,thaumcraft.registry.ModBlocks.ARCANE_WORKTABLE.get().defaultBlockState());
        var bench=(thaumcraft.arcane.ArcaneWorkbenchBlockEntity)h.getLevel().getBlockEntity(benchPos);bench.setItem(0,new ItemStack(Items.FEATHER));bench.setItem(1,new ItemStack(thaumcraft.registry.EldritchContent.PRIMORDIAL_PEARL.get()));bench.setItem(2,new ItemStack(Items.GLASS_BOTTLE));var charged=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_SILVERWOOD,false);Wands.fill(charged);bench.setItem(thaumcraft.arcane.ArcaneWorkbenchBlockEntity.WAND_SLOT,charged);
        var menu=new thaumcraft.menu.ArcaneWorkbenchMenu(0,player.getInventory(),benchPos);check(menu.getSlot(0).getItem().is(Content.get("primal_scribing_tools")),"Primal ink no actual workbench output");var crafted=menu.getSlot(0).remove(1);menu.getSlot(0).onTake(player,crafted);
        check(bench.getItem(1).is(thaumcraft.registry.EldritchContent.PRIMORDIAL_PEARL.get())&&bench.getItem(1).getCount()==1&&bench.getItem(0).isEmpty()&&bench.getItem(2).isEmpty(),"Primal ink consumed/duplicated pearl");proof.put("actual_workbench_primal_ink_preserves_one_pearl",true);menu.removed(player);h.getLevel().removeBlock(benchPos,false);
        for(var rod:WandRegistry.rods())if(rod.tag().startsWith("fm_")){
            if(rod.tag().startsWith("fm_neutronium")||rod.tag().equals("fm_profaned"))continue; // Creative/expired parts have no survival assembly research.
            var cap=rod.tag().contains("blood")?WandRegistry.findCap("fm_alchemical"):rod.tag().contains("wood")?WandRegistry.findCap("fm_elementium"):WandRegistry.CAP_THAUMIUM;
            var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));
            grid.set(2,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(cap.item())));grid.set(6,grid.get(2).copy());grid.set(4,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rod.item())));
            var match=ArcaneCrafting.find(grid,player);check(match!=null&&Wands.getRod(match.result()).tag().equals(rod.tag()),"Native wand assembly "+rod.tag());
        }
        var prime=Content.stack("primal_scribing_tools");prime.setDamageValue(100);check(prime.getDamageValue()==0,"Primal ink consumed");
        var crystal=Content.stack("crystal_scribing_tools");crystal.setDamageValue(100);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,crystal);
        int before=Knowledge.get(player).getAspectPool(Aspect.AIR);
        var used=((Scribes)crystal.getItem()).use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND).getObject();
        check(used.is(thaumcraft.registry.ModItems.SCRIBING_TOOLS.get())&&used.getDamageValue()==100,"Crystal ink did not become empty normal ink");
        check(Knowledge.get(player).getAspectPool(Aspect.AIR)>=before+4,"Crystal ink awarded no knowledge");
        proof.put("crystal_and_primal_ink",true);
        if(ForbiddenMagic.loaded("botania"))proof.put("botania",BotanyTests.verify(h,player));
        if(ForbiddenMagic.loaded("bloodmagic"))proof.put("blood_magic",BloodTests.verify(h,player));
        player.discard();proof.put("status","PASS");
        Files.writeString(Path.of("port-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();
    }
}
