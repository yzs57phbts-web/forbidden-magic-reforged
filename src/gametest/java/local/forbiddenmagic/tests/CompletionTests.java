package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;
import thaumcraft.wand.*;
import thaumcraft.platform.Baubles;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class CompletionTests {
    static void enchant(ItemStack s,ServerLevel l,String id,int n){s.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id(id))),n);}
    @GameTest(batch="completion",template="test/platform",timeoutTicks=400)
    public static void fullContent(GameTestHelper h)throws Exception{
        var l=h.getLevel();var p=FakePlayerFactory.get(l,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"fm-completion"));l.addNewPlayer(p);p.setGameMode(GameType.SURVIVAL);var origin=h.absolutePos(new BlockPos(5,4,5));p.moveTo(origin.getX(),origin.getY(),origin.getZ(),0,0);
        var proof=new LinkedHashMap<String,Object>();
        for(var ore:List.of(Blocks.EMERALD_ORE,Blocks.DEEPSLATE_EMERALD_ORE))check(thaumcraft.device.SmeltingBonuses.get(new ItemStack(ore))==Content.get("emerald_nugget"),"Native furnace input bonus missing "+ore);proof.put("infernal_furnace_native_ore_bonus_lookup",true);
        for(var r:ResearchCategories.allResearch())Knowledge.get(p).addResearch(r.key());
        for(var s:List.of("morph_pickaxe","morph_axe","morph_shovel","morph_sword")){
            var tool=Content.stack(s);p.setItemInHand(InteractionHand.MAIN_HAND,tool);tool.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("first"));enchant(tool,l,"voidtouched",1);
            check(DarkTools.cycle(tool,p)&&DarkTools.phase(tool)==1&&tool.getEnchantments().isEmpty()&&!tool.has(DataComponents.CUSTOM_NAME),"First morph switch "+s);
            tool.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("second"));enchant(tool,l,"wrath",2);DarkTools.cycle(tool,p);check(tool.getEnchantments().isEmpty(),"Third phase not empty");DarkTools.cycle(tool,p);
            check(ToolEvents.enchant(p,tool,"voidtouched")==1&&tool.getHoverName().getString().equals("first")&&tool.getDamageValue()==15,"Loadout not restored "+s);
            var saved=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)tool.save(l.registryAccess()));DarkTools.cycle(saved,p);check(saved.getHoverName().getString().equals("second")&&ToolEvents.enchant(p,saved,"wrath")==2,"Morph save/load "+s);
        }proof.put("four_tools_three_independent_loadouts_save_reload",true);
        int enchanted=0;
        for(String name:List.of("wrath","cluster","greedy","consuming","educational","corrupting","voidtouched","impact")){
            var r=(InfusionEnchantmentRecipe)TcRecipes.get("FM:ENCH_"+name.toUpperCase());var target=Content.stack(name.equals("wrath")||name.equals("greedy")||name.equals("educational")?"morph_sword":"morph_pickaxe");
            var parts=r.components().stream().map(i->i.displayStacks().getFirst()).toList();var plan=InfusionCrafting.findEnchantment(l,parts,target,p);check(plan!=null&&plan.key().equals(r.key()),"Native infusion enchant match "+name);
            var out=InfusionCrafting.result(l,plan.type(),plan.key(),target);check(ToolEvents.enchant(p,out,name)==1,"Native enchant result "+name);enchanted++;
        }proof.put("native_infusion_enchantments",enchanted);
        var impact=Content.stack("morph_pickaxe");enchant(impact,l,"impact",1);p.setItemInHand(InteractionHand.MAIN_HAND,impact);p.getPersistentData().putInt("fm_break_face",Direction.UP.ordinal());
        var center=origin.offset(0,0,12);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)l.setBlockAndUpdate(center.offset(x,0,z),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(center.offset(1,0,1),Blocks.BEDROCK.defaultBlockState());
        check(p.gameMode.destroyBlock(center),"Primary harvest failed");int air=0;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(l.isEmptyBlock(center.offset(x,0,z)))air++;check(air==8&&l.getBlockState(center.offset(1,0,1)).is(Blocks.BEDROCK),"Impact area/bedrock "+air);proof.put("impact_native_harvest_8_stone_bedrock_preserved",true);
        var shovel=Content.stack("taint_shovel");p.setItemInHand(InteractionHand.MAIN_HAND,shovel);var flux=BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:flux_goo"));l.setBlockAndUpdate(origin.offset(2,0,0),flux.defaultBlockState());shovel.useOn(new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(origin.offset(2,0,0)),Direction.UP,origin.offset(2,0,0),false)));check(l.isEmptyBlock(origin.offset(2,0,0))&&shovel.getDamageValue()==1,"Shovel failed to purge flux");proof.put("purifier_removes_native_flux_and_charges_durability",true);
        p.getInventory().clearContent();p.getInventory().setItem(0,Content.stack("soul_crystal"));p.getInventory().setItem(1,Content.stack("soul_crystal"));
        check(WrathCage.capture(p,EntityType.PIG)&&WrathCage.soul(p.getInventory().getItem(0)).equals("minecraft:pig")&&WrathCage.soul(p.getInventory().getItem(1)).isEmpty(),"Soul capture first crystal");check(!WrathCage.capture(p,EntityType.WITHER),"Boss captured");
        var cagePos=origin.offset(14,0,0);l.setBlockAndUpdate(cagePos,Completion.block("wrath_cage").defaultBlockState());var cage=(WrathCage.Cage)l.getBlockEntity(cagePos);cage.mob="minecraft:pig";cage.mode=2;
        check(cage.addEssentia(Aspect.DESIDIA,100,Direction.UP)==64&&cage.addEssentia(Aspect.DESIDIA,1,Direction.UP)==0,"Cage buffer overflow");
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)l.setBlockAndUpdate(cagePos.offset(x,-1,z),Blocks.STONE.defaultBlockState());
        for(int i=0;i<100;i++){cage.delay=0;cage.tick();}
        var pigs=l.getEntitiesOfClass(net.minecraft.world.entity.animal.Pig.class,new AABB(cagePos).inflate(8));check(pigs.size()==6,"Cage nearby cap "+pigs.size());check(cage.slothful&&cage.delay>=400&&cage.delay<=499,"Sloth delay");
        var tag=cage.saveWithoutMetadata(l.registryAccess());var restored=new WrathCage.Cage(cagePos,cage.getBlockState());restored.loadWithComponents(tag,l.registryAccess());check(restored.mob.equals("minecraft:pig")&&restored.fuel==cage.fuel&&restored.getEssentiaAmount(null)==cage.getEssentiaAmount(null),"Cage NBT lost");
        pigs.forEach(Entity::discard);int fuel=cage.fuel;l.setBlockAndUpdate(cagePos.above(),Blocks.REDSTONE_BLOCK.defaultBlockState());cage.delay=0;cage.tick();check(cage.fuel==fuel&&l.getEntitiesOfClass(net.minecraft.world.entity.animal.Pig.class,new AABB(cagePos).inflate(8)).isEmpty(),"Cage ignores redstone");l.removeBlock(cagePos.above(),false);
        proof.put("cage_real_spawns_cap_sloth_buffers_redstone_nbt",true);
        l.destroyBlock(cagePos,true);var crystals=l.getEntitiesOfClass(ItemEntity.class,new AABB(cagePos).inflate(2),e->e.getItem().is(Content.get("soul_crystal")));check(crystals.size()==1&&WrathCage.soul(crystals.getFirst().getItem()).equals("minecraft:pig"),"Cage soul drop lost/duplicated");proof.put("soul_returned_once_on_break",true);
        var saplingPos=origin.offset(-10,0,0);for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){l.getChunkAt(saplingPos.offset(x,0,z));for(int y=0;y<=8;y++)l.setBlockAndUpdate(saplingPos.offset(x,y,z),Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(saplingPos.below(),Blocks.DIRT.defaultBlockState());l.setBlockAndUpdate(saplingPos,Completion.block("tainted_sapling").defaultBlockState());check(Ecology.grow(l,saplingPos,l.random),"Tainted tree failed");int logs=0;while(l.getBlockState(saplingPos.above(logs)).is(Completion.block("tainted_log")))logs++;check(logs>=4&&logs<=6,"Tainted tree height");proof.put("tainted_tree_actual_blocks_height",logs);
        var creative=Wands.create(WandRegistry.cap("fm_orichalcum"),WandRegistry.rod("fm_neutronium"),false);Wands.fill(creative);check(!((WandItem)creative.getItem()).consumeAllVis(creative,p,new AspectList().add(Aspect.AIR,100),true,true),"Creative wand used in Survival");p.setGameMode(GameType.CREATIVE);check(((WandItem)creative.getItem()).consumeAllVis(creative,p,new AspectList().add(Aspect.AIR,100),true,true),"Creative wand blocked in Creative");p.setGameMode(GameType.SURVIVAL);proof.put("creative_wand_native_consumption_guard",true);
        var contract=Wands.create(WandRegistry.CAP_IRON,WandRegistry.rod("fm_profane"),false);p.tickCount=20;Heretic.contract(contract,p);check(Wands.getAllVis(contract).visSize()==25000&&Wands.getRod(contract).tag().equals("fm_profaned"),"Profane contract total/expiry");proof.put("contract_exact_25000_centivis_then_inert",true);
        check(PoiTypes.forState(Completion.block("nether_star_block").defaultBlockState()).isPresent(),"Heretic POI not mapped");var villager=new Villager(EntityType.VILLAGER,l);villager.setVillagerData(villager.getVillagerData().setProfession(Heretic.JOB.get()).setLevel(3));var trades=villager.getOffers();check(trades.stream().anyMatch(o->Wands.isWand(o.getResult())&&Wands.getRod(o.getResult()).tag().equals("fm_profane")),"Heretic no contract trade");proof.put("heretic_registered_job_site_and_real_trades",true);
        // Every registered JSON recipe must resolve through RecipeManager, not merely parse as JSON.
        for(String id:List.of("planks","tainted_bricks","hexcoal","black_dye","emerald","stars"))check(l.getRecipeManager().byKey(ForbiddenMagic.id(id)).isPresent(),"Missing native recipe "+id);
        if(ForbiddenMagic.loaded("botania"))BotanyTests.verifyFloating(l,origin.offset(0,3,15));
        if(ForbiddenMagic.loaded("bloodmagic"))for(String id:List.of("taint","blood_seal"))check(l.getRecipeManager().byKey(ForbiddenMagic.id("flask/"+id)).isPresent(),"Missing Blood Magic flask recipe "+id);
        proof.put("native_crafting_smelting_optional_recipe_codecs",true);proof.put("status","PASS");p.discard();Files.writeString(Path.of("completion-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();
    }
}
