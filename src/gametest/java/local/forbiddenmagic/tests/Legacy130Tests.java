package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.*;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.aspect.*;
import thaumcraft.research.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_130tests") @PrefixGameTestTemplate(false)
public final class Legacy130Tests {
    @GameTest(batch="legacy130",template="test/platform",timeoutTicks=600)
    public static void restoredMechanics(GameTestHelper h)throws Exception{
        var l=h.getLevel();var p=HeadlessPlayer.create(l,"legacy130");l.addNewPlayer(p);p.setNoGravity(true);var origin=h.absolutePos(new BlockPos(300,10,0));l.getChunkAt(origin);p.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);
        var proof=new LinkedHashMap<String,Object>();
        var sword=new ItemStack(Items.IRON_SWORD);int weapon=LegacyMechanics.wrathWeaponBonus(sword);p.setItemInHand(InteractionHand.MAIN_HAND,sword);p.doTick();double before=p.getAttributeValue(Attributes.ATTACK_DAMAGE);p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,4));p.doTick();
        check(p.getAttributeValue(Attributes.ATTACK_DAMAGE)>before&&LegacyMechanics.wrathWeaponBonus(sword)==weapon&&weapon==6,"Wrath includes player strength or wrong old sword damage");check(LegacyMechanics.wrathWeaponBonus(new ItemStack(Items.IRON_PICKAXE))==0,"Original ItemTool zero contribution changed");p.removeAllEffects();proof.put("1_weapon_only_wrath",weapon);
        var tool=Content.stack("morph_sword");ModernizationTests.fmEnch(tool,l,"educational",2);p.setItemInHand(InteractionHand.MAIN_HAND,tool);var zombie=EntityType.ZOMBIE.create(l);int base=((local.forbiddenmagic.mixin.MobExperienceAccessor)zombie).fmBaseExperience();check(base==5,"Unexpected base XP fixture");
        for(int dropped:new int[]{0,5,11,20}){var event=new LivingExperienceDropEvent(zombie,p,dropped);NeoForge.EVENT_BUS.post(event);check(event.getDroppedExperience()==dropped+30,"Educational multiplies ready XP "+dropped+" -> "+event.getDroppedExperience());}proof.put("2_base_xp_addition",30);
        BlockPos clicked=origin.south(4),near=origin.north(2),far=origin.south(8);l.getChunkAt(far);l.setBlockAndUpdate(clicked,Blocks.STONE.defaultBlockState());var flux=thaumcraft.registry.ModBlocks.FLUX_GOO.get();l.setBlockAndUpdate(near,flux.defaultBlockState());l.setBlockAndUpdate(far,flux.defaultBlockState());
        var shovel=Content.stack("taint_shovel");p.setItemInHand(InteractionHand.MAIN_HAND,shovel);shovel.getItem().use(l,p,InteractionHand.MAIN_HAND);check(!l.isEmptyBlock(near)&&!l.isEmptyBlock(far),"Air use purges flux");shovel.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(clicked),Direction.UP,clicked,false)));
        check(!l.isEmptyBlock(near)&&l.isEmptyBlock(far)&&shovel.getDamageValue()==1,"Purifier centre/cost incorrect");l.removeBlock(near,false);proof.put("3_clicked_centre_only",true);
        var box=LegacyMechanics.cageLimitBox(BlockPos.ZERO);check(box.getXsize()==17&&box.getYsize()==9&&box.getZsize()==17,"Legacy cage counter dimensions");check(LegacyMechanics.sameCageFamily(EntityType.SKELETON,EntityType.WITHER_SKELETON.create(l),l),"Skeleton variants split cap");check(LegacyMechanics.sameCageFamily(EntityType.HORSE,EntityType.DONKEY.create(l),l),"Horse variants split cap");check(LegacyMechanics.sameCageFamily(EntityType.CAT,EntityType.OCELOT.create(l),l),"Cat variants split cap");check(!LegacyMechanics.sameCageFamily(EntityType.COW,EntityType.PIG.create(l),l),"Unrelated species merged");proof.put("4_legacy_cap_groups_and_box",true);
        check(WrathCage.SOULS.get(ResourceLocation.parse("thaumcraft:thaumic_slime"))==Aspect.TAINT,"Wrong slime fuel");proof.put("5_slime_vitium",true);
        try(var config=new ConfigScope130(Map.of("researchDifficulty",-1))){
            var ink=Content.stack("crystal_scribing_tools");check(ink.getMaxDamage()==50,"Easy ink not50");p.getInventory().setItem(9,ink);int points=pool(p);p.tickCount=200;ink.getItem().inventoryTick(ink,l,p,9,false);int gain=pool(p)-points;check(gain>=1&&gain<=4&&ink.getDamageValue()==1,"Easy passive reward/cost");
            ((Scribes)ink.getItem()).setStoredDamage(ink,49);p.tickCount=400;ink.getItem().inventoryTick(ink,l,p,9,false);check(ink.isEmpty(),"Easy ink not consumed after50");proof.put("6_easy_ink_50_passive_200ticks",gain);
        }p.getInventory().clearContent();
        p.getFoodData().setFoodLevel(0);p.setItemInHand(InteractionHand.MAIN_HAND,Content.stack("tainted_fruit"));p.startUsingItem(InteractionHand.MAIN_HAND);for(int i=0;i<40;i++)p.doTick();
        check(p.getMainHandItem().isEmpty()&&p.hasEffect(MobEffects.HUNGER)&&!p.hasEffect(MobEffects.POISON)&&p.hasEffect(thaumcraft.registry.ModEffects.TAINT_POISON),"Wrong native fruit effects");p.removeAllEffects();proof.put("8_native_hunger_not_poison",true);
        var tree=origin.west(25);for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){l.getChunkAt(tree.offset(x,0,z));for(int y=0;y<9;y++)l.setBlockAndUpdate(tree.offset(x,y,z),Blocks.AIR.defaultBlockState());}l.setBlockAndUpdate(tree.below(),Blocks.DIRT.defaultBlockState());l.setBlockAndUpdate(tree,Completion.block("tainted_sapling").defaultBlockState());Ecology.Sapling.advance(l.getBlockState(tree),l,tree,l.random);check(l.getBlockState(tree).is(Completion.block("tainted_sapling"))&&l.getBlockState(tree).getValue(Ecology.Sapling.STAGE)==1,"First growth skipped stage");Ecology.Sapling.advance(l.getBlockState(tree),l,tree,l.random);check(l.getBlockState(tree).is(Completion.block("tainted_log")),"Second growth failed");proof.put("9_two_growth_steps",true);
        verifyRose(h,origin.east(35));proof.put("10_rose_old_walk_100_seeds",true);
        var focus=(BlinkFocus)Content.get("blink_focus");check(focus.getActivationCooldown(Content.stack("blink_focus"))==0,"Blink extra cooldown remains");proof.put("11_focus_cooldown_zero",true);
        var damage=LegacyMechanics.hellfire(p);check(damage.is(DamageTypeTags.IS_FIRE),"Hellfire not fire");var blaze=EntityType.BLAZE.create(l);blaze.moveTo(origin.getX()+2,origin.getY(),origin.getZ(),0,0);float hp=blaze.getHealth();check(!blaze.hurt(damage,3)&&blaze.getHealth()==hp,"Fire-immune blaze damaged");var target=EntityType.ZOMBIE.create(l);target.moveTo(origin.getX()+2,origin.getY(),origin.getZ()+2,0,0);target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100));hp=target.getHealth();target.hurt(damage,3);check(target.getHealth()==hp,"Fire Resistance ignored");proof.put("12_fire_type_and_immunity",true);
        try(var config=new ConfigScope130(Map.of("enchanting",false,"silverfishEmeralds",false,"noLust",true,"garbageBlocks",List.of("minecraft:cobblestone")))){
            check(ToolEvents.enchant(p,tool,"educational")==0,"Disabled enchantment still active");var enchantments=l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().map(e->(net.minecraft.core.Holder<Enchantment>)e);var choices=EnchantmentHelper.getAvailableEnchantmentResults(50,new ItemStack(Items.BOOK),enchantments);check(choices.stream().noneMatch(e->e.enchantment.unwrapKey().orElseThrow().location().getNamespace().equals("forbiddenmagic")),"Disabled FM enchantment still rolls");
            check(LegacyConfig.garbage(new ItemStack(Items.COBBLESTONE))&&!LegacyConfig.garbage(new ItemStack(Items.DIRT)),"Garbage override ineffective");check(!LegacyConfig.research("FM_COLLAR"),"NoLust recipe gate ineffective");
        }proof.put("16_live_effect_and_random_enchant_controls",true);
        if(ForbiddenMagic.loaded("bloodmagic"))proof.put("blood_points_7_13_14_15",LegacyBlood130Tests.verify(h,p));
        proof.put("status","PASS");p.discard();Files.writeString(Path.of("legacy-130-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();
    }
    private static int pool(net.minecraft.server.level.ServerPlayer p){int sum=0;for(var a:Aspect.getPrimalAspects())sum+=Knowledge.get(p).getAspectPool(a);return sum;}
    @GameTest(batch="legacy130_cage",template="test/platform",timeoutTicks=300)
    public static void actualLegacyCageLimit(GameTestHelper h){
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(3,10,3));
        for(int x=(pos.getX()-9)>>4;x<=(pos.getX()+9)>>4;x++)for(int z=(pos.getZ()-9)>>4;z<=(pos.getZ()+9)>>4;z++){l.getChunk(x,z);l.setChunkForced(x,z,true);}
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.setBlockAndUpdate(pos.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<=9;y++)l.setBlockAndUpdate(pos.offset(x,y,z),Blocks.AIR.defaultBlockState());}
        l.setBlockAndUpdate(pos,Completion.block("wrath_cage").defaultBlockState());var cage=(WrathCage.Cage)l.getBlockEntity(pos);cage.mob="minecraft:skeleton";cage.fuel=4;cage.delay=1000;var actors=new ArrayList<Mob>();
        for(int n=0;n<6;n++){var m=n%2==0?EntityType.SKELETON.create(l):EntityType.WITHER_SKELETON.create(l);m.setNoAi(true);m.setNoGravity(true);m.setPersistenceRequired();m.moveTo(pos.getX()+2+n%3,pos.getY(),pos.getZ()+2+n/3,0,0);l.addFreshEntity(m);actors.add(m);}
        h.runAfterDelay(3,()->{
            int count=l.getEntitiesOfClass(Mob.class,LegacyMechanics.cageLimitBox(pos),e->LegacyMechanics.sameCageFamily(EntityType.SKELETON,e,l)).size();check(count==6,"Mixed skeleton fixtures count="+count+" removed="+actors.stream().map(Entity::isRemoved).toList()+" difficulty="+l.getDifficulty());cage.delay=0;var blocked=DropCapture.mobs(l,pos,cage::tick);check(blocked.isEmpty()&&cage.fuel==4,"Mixed skeleton cap failed");actors.forEach(Entity::discard);actors.clear();cage.delay=1000;
            for(int n=0;n<6;n++){var m=EntityType.SKELETON.create(l);m.setNoAi(true);m.setNoGravity(true);m.setPersistenceRequired();m.moveTo(pos.getX()+2+n%3,pos.getY()+6,pos.getZ()+2+n/3,0,0);l.addFreshEntity(m);actors.add(m);}
            h.runAfterDelay(3,()->{var spawned=DropCapture.mobs(l,pos,()->{for(int n=0;n<30&&cage.fuel==4;n++){cage.delay=0;cage.tick();}});check(!spawned.isEmpty()&&cage.fuel<4,"Mobs outside old vertical range block cage");actors.forEach(Entity::discard);spawned.forEach(Entity::discard);l.removeBlock(pos,false);try{Files.writeString(Path.of("legacy-130-cage-proof.json"),"{\"status\":\"PASS\",\"mixed_skeleton_cap\":true,\"y6_does_not_block\":true}\n");h.succeed();}catch(Exception e){throw new RuntimeException(e);}});
        });
    }
    private static BlockPos oldRoseCandidate(net.minecraft.server.level.ServerLevel l,BlockPos origin,long seed){
        var r=new Random(seed);BlockPos anchor=origin,candidate=anchor.offset(r.nextInt(3)-1,r.nextInt(2)-r.nextInt(2),r.nextInt(3)-1);var flower=Completion.block("umbral_rose").defaultBlockState();
        for(int i=0;i<8;i++){if(l.isEmptyBlock(candidate)&&flower.canSurvive(l,candidate))anchor=candidate;candidate=anchor.offset(r.nextInt(3)-1,r.nextInt(2)-r.nextInt(2),r.nextInt(3)-1);}
        return l.isEmptyBlock(candidate)&&flower.canSurvive(l,candidate)?candidate:null;
    }
    private static void verifyRose(GameTestHelper h,BlockPos center){
        var l=h.getLevel();for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++){l.getChunkAt(center.offset(x,0,z));l.setBlockAndUpdate(center.offset(x,-1,z),Blocks.DIRT.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(center.offset(x,y,z),Blocks.AIR.defaultBlockState());}
        DoublePlantBlock.placeAt(l,Completion.block("umbral_rose_bush").defaultBlockState(),center,3);var bush=(Ecology.RoseBush)Completion.block("umbral_rose_bush");
        for(long seed=1;seed<=100;seed++){
            BlockPos expected=oldRoseCandidate(l,center,seed);bush.performBonemeal(l,net.minecraft.util.RandomSource.create(seed),center,l.getBlockState(center));var actual=new ArrayList<BlockPos>();
            for(var at:BlockPos.betweenClosed(center.offset(-10,0,-10),center.offset(10,3,10)))if(l.getBlockState(at).is(Completion.block("umbral_rose")))actual.add(at.immutable());
            check(expected==null?actual.isEmpty():actual.equals(List.of(expected)),"Rose differs from independent old walk seed="+seed+" expected="+expected+" actual="+actual);for(var at:actual)l.removeBlock(at,false);
        }
    }
}
