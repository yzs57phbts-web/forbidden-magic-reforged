package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class ModernizationTests {
    static void ench(ItemStack stack,ServerLevel level,ResourceKey<Enchantment> key,int n){stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key),n);}
    static void fmEnch(ItemStack stack,ServerLevel level,String key,int n){ench(stack,level,ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id(key)),n);}
    static ItemStack grind(ServerPlayer p,BlockPos pos,ItemStack first,ItemStack second){
        p.serverLevel().setBlockAndUpdate(pos,Blocks.GRINDSTONE.defaultBlockState());var menu=new GrindstoneMenu(0,p.getInventory(),ContainerLevelAccess.create(p.level(),pos));
        menu.getSlot(0).set(first.copy());menu.getSlot(1).set(second.copy());var result=menu.getSlot(2).remove(1);check(!result.isEmpty(),"Native grindstone has no result");menu.getSlot(2).onTake(p,result);check(menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty(),"Grindstone did not consume inputs");return result;
    }
    @GameTest(batch="modernization",template="test/platform",timeoutTicks=2400)
    public static void modernMechanics(GameTestHelper h)throws Exception{
        var l=h.getLevel();var origin=h.absolutePos(new BlockPos(500,7,0));var p=HeadlessPlayer.create(l,"modern120");l.addNewPlayer(p);p.moveTo(origin.getX(),origin.getY()+5,origin.getZ(),0,0);
        var proof=new LinkedHashMap<String,Object>();
        // The real grindstone result slot and take path, all three stored profiles, curses and reload.
        var grindProof=new ArrayList<Map<String,Object>>();
        for(String id:List.of("morph_pickaxe","morph_axe","morph_shovel","morph_sword")){
            var tool=Content.stack(id);p.setItemInHand(InteractionHand.MAIN_HAND,tool);tool.setDamageValue(100);ench(tool,l,Enchantments.UNBREAKING,1);ench(tool,l,Enchantments.VANISHING_CURSE,1);tool.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("phase zero"));
            DarkTools.cycle(tool,p);ench(tool,l,Enchantments.UNBREAKING,2);DarkTools.cycle(tool,p);ench(tool,l,Enchantments.MENDING,1);DarkTools.cycle(tool,p);
            var result=grind(p,origin,tool,ItemStack.EMPTY);check(result.getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING))==0,"Active non-curse survived grinding");check(result.getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE))==1,"Curse removed");check(result.getHoverName().getString().equals("phase zero"),"Name lost");
            p.setItemInHand(InteractionHand.MAIN_HAND,result);DarkTools.cycle(result,p);check(result.getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING))==2,"Inactive profile lost");DarkTools.cycle(result,p);check(result.getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING))==1,"Third profile lost");DarkTools.cycle(result,p);check(result.getEnchantments().size()==1,"Removed enchantment resurrected");
            ench(result,l,Enchantments.UNBREAKING,3);DarkTools.cycle(result,p);DarkTools.cycle(result,p);DarkTools.cycle(result,p);var restored=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)result.save(l.registryAccess()));check(restored.getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING))==3,"Re-enchanted profile lost on reload");
            var second=Content.stack(id);fmEnch(second,l,"wrath",1);DarkTools.cycle(second,p);fmEnch(second,l,"educational",1);second.setDamageValue(200);var combined=grind(p,origin,restored,second);DarkTools.cycle(combined,p);
            check(ToolEvents.enchant(p,combined,"educational")==0,"Unexpected merged hidden profiles");grindProof.add(Map.of("tool",id,"active_profile_cleared",true,"inactive_profiles_preserved",true,"curse_preserved",true,"reenchant_reload",true,"two_item_repair_keeps_first_item_hidden_profiles",true));
        }proof.put("native_grindstone",grindProof);
        var recipe=(CrucibleRecipe)TcRecipes.get("FM:TAINTTREE");
        int saplings=0;for(var item:List.of(Items.OAK_SAPLING,Items.SPRUCE_SAPLING,Items.BIRCH_SAPLING,Items.JUNGLE_SAPLING,Items.ACACIA_SAPLING,Items.DARK_OAK_SAPLING,Items.CHERRY_SAPLING,Items.MANGROVE_PROPAGULE)){check(recipe.catalyst().test(new ItemStack(item)),"Rejected vanilla sapling "+item);saplings++;}
        for(var item:List.of(Items.CRIMSON_FUNGUS,Items.WARPED_FUNGUS,Completion.block("tainted_sapling").asItem(),Items.STICK))check(!recipe.catalyst().test(new ItemStack(item)),"Invalid taint catalyst "+item);proof.put("saplings_accepted",saplings);
        var craft=l.getRecipeManager().byKey(ForbiddenMagic.id("black_dye")).orElseThrow().value();var grid=CraftingInput.of(1,1,List.of(Content.stack("umbral_rose")));var dye=((CraftingRecipe)craft).assemble(grid,l.registryAccess());check(dye.is(Items.BLACK_DYE)&&dye.getCount()==2,"Rose does not directly yield vanilla dye");proof.put("rose_two_vanilla_dyes",true);
        check(TcRecipes.get("forbiddenmagic:black_dye") instanceof CraftingRecipeRef reference&&reference.result().create().is(Items.BLACK_DYE)&&reference.result().create().getCount()==2,"Missing updated native book crafting reference");
        var trash=TagKey.create(Registries.ITEM,ForbiddenMagic.id("consuming_trash"));
        var consumed=List.of(Items.COBBLED_DEEPSLATE,Items.DEEPSLATE,Items.GRANITE,Items.DIORITE,Items.ANDESITE,Items.TUFF,Items.CALCITE,Items.BASALT,Items.SMOOTH_BASALT,Items.BLACKSTONE);
        for(var item:consumed)check(new ItemStack(item).is(trash),"Modern spoil missing "+item);
        for(var item:List.of(Items.RAW_IRON,Items.DIAMOND,Items.COBBLED_DEEPSLATE_STAIRS,Items.POLISHED_BASALT,Items.SUSPICIOUS_GRAVEL))check(!new ItemStack(item).is(trash),"Valuable/processed material consumed "+item);
        var consuming=Content.stack("morph_pickaxe");fmEnch(consuming,l,"consuming",1);p.setItemInHand(InteractionHand.MAIN_HAND,consuming);var mine=origin.offset(10,0,0);l.getChunkAt(mine);
        for(var block:List.of(Blocks.DEEPSLATE,Blocks.TUFF,Blocks.BASALT,Blocks.BLACKSTONE)){
            l.getEntitiesOfClass(ItemEntity.class,new AABB(mine).inflate(2)).forEach(Entity::discard);l.setBlockAndUpdate(mine,block.defaultBlockState());check(p.gameMode.destroyBlock(mine),"Native modern mining failed");check(l.getEntitiesOfClass(ItemEntity.class,new AABB(mine).inflate(2)).isEmpty(),"Consumed spoil still dropped "+block);}
        proof.put("modern_spoil_native_harvest",4);
        var axe=Content.stack("skull_axe");check(axe.getItem() instanceof AxeItem&&axe.is(ItemTags.AXES)&&!axe.is(ItemTags.SWORDS),"Skull axe not a modern axe");check(axe.canDisableShield(new ItemStack(Items.SHIELD),p,p),"Axe cannot disable shield");check(axe.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState())==1&&!axe.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SWORD_SWEEP),"Axe acquired lumberjack/sword role");
        for(var key:List.of(Enchantments.LOOTING,Enchantments.FIRE_ASPECT,Enchantments.KNOCKBACK,ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id("greedy")),ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id("educational"))))check(axe.supportsEnchantment(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key)),"Axe lost a pre-existing weapon enchantment "+key);
        check(!axe.supportsEnchantment(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SWEEPING_EDGE)),"Battle axe accepts useless sweeping enchantment");
        var anvil=new AnvilMenu(0,p.getInventory());anvil.getSlot(0).set(axe.copy());var book=new ItemStack(Items.ENCHANTED_BOOK);ench(book,l,Enchantments.LOOTING,3);p.experienceLevel=60;anvil.getSlot(1).set(book);check(!anvil.getSlot(2).getItem().isEmpty()&&anvil.getSlot(2).getItem().getEnchantmentLevel(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING))==3,"Native anvil cannot apply Looting to Skull Axe");proof.put("skull_axe_retains_native_anvil_looting",true);
        for(String key:List.of("greedy","educational")){
            var r=(InfusionEnchantmentRecipe)TcRecipes.get("FM:ENCH_"+key.toUpperCase());thaumcraft.research.Knowledge.get(p).addResearch(r.research());
            var plan=InfusionCrafting.findEnchantment(l,r.components().stream().map(i->i.displayStacks().getFirst()).toList(),axe.copy(),p);check(plan!=null&&plan.key().equals(r.key()),"Skull Axe lost native TC infusion "+key);
            check(ToolEvents.enchant(p,InfusionCrafting.result(l,plan.type(),plan.key(),axe.copy()),key)==1,"Skull Axe native infusion output failed "+key);
        }proof.put("skull_axe_retains_two_native_infusion_enchantments",true);
        var defender=HeadlessPlayer.create(l,"shield120");l.addNewPlayer(defender);p.setItemInHand(InteractionHand.MAIN_HAND,axe);p.moveTo(origin.getX()+1,origin.getY()+5,origin.getZ()+3,180,0);defender.moveTo(origin.getX()+1,origin.getY()+5,origin.getZ()+1,0,0);defender.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SHIELD));defender.setNoGravity(true);var spawnProtection=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");spawnProtection.setAccessible(true);spawnProtection.setInt(defender,0);defender.startUsingItem(InteractionHand.MAIN_HAND);for(int i=0;i<8;i++)defender.doTick();defender.setYHeadRot(0);check(defender.isBlocking(),"Fixture shield not raised");check(defender.isDamageSourceBlocked(l.damageSources().playerAttack(p)),"Fixture attack not in front of shield");boolean pvp=l.getServer().isPvpAllowed();l.getServer().setPvpAllowed(true);defender.hurt(l.damageSources().playerAttack(p),10);l.getServer().setPvpAllowed(pvp);check(defender.getCooldowns().isOnCooldown(Items.SHIELD),"Real shield was not disabled");defender.discard();proof.put("actual_shield_disabled",true);
        // Nether greed: actual block harvesting, single check per block and no Silk Touch route.
        var nether=l.getServer().getLevel(Level.NETHER);check(nether!=null,"No Nether test level");var np=HeadlessPlayer.create(nether,"greed120");nether.addNewPlayer(np);var at=new BlockPos(1000,90,1000);nether.getChunkAt(at);np.moveTo(1000,93,1000,0,0);var fortune=new ItemStack(Items.NETHERITE_PICKAXE);ench(fortune,nether,Enchantments.FORTUNE,3);np.setItemInHand(InteractionHand.MAIN_HAND,fortune);var greed=new LinkedHashMap<String,Integer>();
        for(var block:List.of(Blocks.NETHER_QUARTZ_ORE,Blocks.NETHER_GOLD_ORE,Blocks.GILDED_BLACKSTONE)){
            int shards=0;for(int i=0;i<200;i++){nether.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(2)).forEach(Entity::discard);nether.setBlockAndUpdate(at,block.defaultBlockState());var drops=DropCapture.take(nether,at,()->check(np.gameMode.destroyBlock(at),"Nether harvest failed"));int count=drops.stream().filter(s0->s0.is(Content.get("greed_shard"))).mapToInt(ItemStack::getCount).sum();check(count<=1,"More than one greed roll output");shards+=count;}check(shards>0,"No greed from "+block);greed.put(BuiltInRegistries.BLOCK.getKey(block).toString(),shards);}
        var silk=new ItemStack(Items.NETHERITE_PICKAXE);ench(silk,nether,Enchantments.SILK_TOUCH,1);ench(silk,nether,Enchantments.FORTUNE,3);np.setItemInHand(InteractionHand.MAIN_HAND,silk);
        for(int i=0;i<100;i++){nether.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(2)).forEach(Entity::discard);nether.setBlockAndUpdate(at,Blocks.NETHER_GOLD_ORE.defaultBlockState());var drops=DropCapture.take(nether,at,()->np.gameMode.destroyBlock(at));check(drops.stream().noneMatch(s0->s0.is(Content.get("greed_shard"))),"Silk greed exploit");}np.discard();proof.put("nether_greed_200_native_breaks_each",greed);proof.put("silk_greed_rejected_100",true);
        // Separate supported land / full-water arenas. Keep away from other GameTest structures.
        var ground=origin.offset(40,0,0);var water=origin.offset(75,0,0);
        for(var center:List.of(ground,water))for(int x=(center.getX()-10)>>4;x<=(center.getX()+10)>>4;x++)for(int z=(center.getZ()-10)>>4;z<=(center.getZ()+10)>>4;z++)l.setChunkForced(x,z,true);
        for(var center:List.of(ground,water))for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){
            l.getChunkAt(center.offset(x,0,z));for(int y=-2;y<=7;y++)l.setBlockAndUpdate(center.offset(x,y,z),y==-2?Blocks.STONE.defaultBlockState():center.equals(water)&&y<=4?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());}
        for(var center:List.of(ground,water))l.setBlockAndUpdate(center,Completion.block("wrath_cage").defaultBlockState());
        var souls=new ArrayList<>(WrathCage.SOULS.keySet());var actual=new ArrayList<String>();var previousSpawns=new ArrayList<Mob>();var counter=new int[]{0};
        h.onEachTick(()->{
            if(counter[0]<souls.size()){
                var id=souls.get(counter[0]++);var type=BuiltInRegistries.ENTITY_TYPE.get(id);check(CageRules.allowed(type),"Registered forbidden soul "+id);var habitat=CageRules.habitat(type);var center=habitat==CageRules.Habitat.WATER?water:ground;
                previousSpawns.forEach(Entity::discard);previousSpawns.clear();for(var arena:List.of(ground,water))l.getEntitiesOfClass(Mob.class,new AABB(arena).inflate(15)).forEach(Entity::discard);
                check(type.create(l) instanceof Mob,"Non-mob soul "+id);var sample=(Mob)type.create(l);sample.moveTo(center.getX()+3.5,center.getY()-1,center.getZ()+3.5,0,0);check(CageRules.position(l,sample),"Rejected valid physical position "+id+" "+CageRules.explain(l,sample));
                if(habitat==CageRules.Habitat.WATER){sample.moveTo(ground.getX()+3.5,ground.getY()-1,ground.getZ()+3.5,0,0);check(!CageRules.position(l,sample),"Aquatic soul accepted dry ground "+id);}
                if(habitat==CageRules.Habitat.LAND){sample.moveTo(water.getX()+3.5,water.getY()-1,water.getZ()+3.5,0,0);check(!CageRules.position(l,sample),"Land soul accepted underwater "+id);sample.moveTo(ground.getX()+3.5,ground.getY()+4,ground.getZ()+3.5,0,0);check(!CageRules.position(l,sample),"Land soul accepted unsupported air "+id);}
                var cage=(WrathCage.Cage)l.getBlockEntity(center);cage.mob=id.toString();cage.fuel=4;
                var mobs=new ArrayList<Mob>();for(int attempt=0;attempt<100&&mobs.stream().noneMatch(e->e.getType()==type);attempt++){cage.delay=0;mobs.addAll(DropCapture.mobs(l,center,cage::tick));}
                check(cage.fuel<4&&mobs.stream().anyMatch(e->e.getType()==type),"No actual cage spawn "+id+" fuel="+cage.fuel);previousSpawns.addAll(mobs);for(var mob:mobs){if(mob.getType()==type)check(CageRules.position(l,mob),"Invalid finalized position "+id);mob.setNoAi(true);}actual.add(id.toString());
            }else{
                p.getInventory().clearContent();p.getInventory().setItem(0,Content.stack("soul_crystal"));check(WrathCage.capture(p,EntityType.PIG),"Positive capture control failed");
                for(String id:List.of("minecraft:wither","minecraft:ender_dragon","minecraft:warden","minecraft:elder_guardian","twilightforest:naga","twilightforest:lich","twilightforest:loyal_zombie","twilightforest:lich_minion","twilightforest:rising_zombie","twilightforest:quest_ram")){
                    var key=ResourceLocation.parse(id);if(BuiltInRegistries.ENTITY_TYPE.containsKey(key)){p.getInventory().setItem(0,Content.stack("soul_crystal"));check(!CageRules.allowed(BuiltInRegistries.ENTITY_TYPE.get(key))&&!WrathCage.capture(p,BuiltInRegistries.ENTITY_TYPE.get(key)),"Excluded creature captured "+id);}}
                proof.put("actual_cage_spawn_species",actual);proof.put("twilight_loaded",ForbiddenMagic.loaded("twilightforest"));proof.put("bosses_and_green_zombies_rejected",true);proof.put("status","PASS");p.discard();
                try{Files.writeString(Path.of("modernization-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();}catch(Exception e){throw new RuntimeException(e);}
            }
        });
    }
    @GameTest(batch="modern_heads",template="test/platform",timeoutTicks=600)
    public static void modernHeads(GameTestHelper h){
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(500,10,100));l.getChunkAt(pos);var p=HeadlessPlayer.create(l,"heads120");l.addNewPlayer(p);p.moveTo(pos.getX(),pos.getY()+3,pos.getZ(),0,0);p.setItemInHand(InteractionHand.MAIN_HAND,Content.stack("skull_axe"));
        var types=List.of(EntityType.SKELETON,EntityType.WITHER_SKELETON,EntityType.STRAY,EntityType.BOGGED,EntityType.ZOMBIE,EntityType.HUSK,EntityType.DROWNED,EntityType.PIGLIN,EntityType.CREEPER);var proof=new LinkedHashMap<String,Integer>();var index=new int[]{0};
        h.onEachTick(()->{
            if(index[0]<types.size()){
                var type=types.get(index[0]++);int heads=0;
                for(int n=0;n<128;n++){
                    l.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3)).forEach(Entity::discard);var mob=(Mob)type.create(l);mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);mob.setNoAi(true);l.addFreshEntity(mob);var drops=DropCapture.take(l,pos,()->check(mob.hurt(l.damageSources().playerAttack(p),1000),"Native lethal hit rejected"));
                    heads+=drops.stream().filter(s0->s0.is(Items.SKELETON_SKULL)||s0.is(Items.WITHER_SKELETON_SKULL)||s0.is(Items.ZOMBIE_HEAD)||s0.is(Items.PIGLIN_HEAD)||s0.is(Items.CREEPER_HEAD)).mapToInt(ItemStack::getCount).sum();mob.discard();
                }check(heads>0,"No actual head drops "+type);proof.put(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(),heads);
            }else try{Files.writeString(Path.of("modern-heads-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","actual_deaths_per_species",128,"heads",proof)));p.discard();h.succeed();}catch(Exception e){throw new RuntimeException(e);}
        });
    }
}
