package local.forbiddenmagic.tests;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import local.forbiddenmagic.*;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.*;
import thaumcraft.research.Knowledge;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.core.data.*;
import wayoftime.bloodmagic.common.item.IBloodOrb;
import wayoftime.bloodmagic.common.tile.TileMasterRitualStone;
import wayoftime.bloodmagic.ritual.*;
import static local.forbiddenmagic.tests.PortTests.check;

public final class BloodTests {
    public static Map<String,Object> verify(GameTestHelper h,ServerPlayer player)throws Exception {
        var level=h.getLevel();var proof=new LinkedHashMap<String,Object>();
        player.getInventory().clearContent();player.setGameMode(GameType.SURVIVAL);player.setHealth(20);
        var network=NetworkHelper.getSoulNetwork(player);network.setOrbTier(6);network.setCurrentEssence(50000);
        // The GameTest mock is not in the server login UUID index; attach it to BM's ordinary cache.
        var cache=SoulNetwork.class.getDeclaredField("cachedPlayer");cache.setAccessible(true);cache.set(network,player);
        var wand=Wands.create(WandRegistry.cap("fm_alchemical"),WandRegistry.rod("fm_blood"),false);
        Wands.fill(wand);Wands.storeVis(wand,Aspect.AIR,0);player.getInventory().setItem(0,wand);player.tickCount=100;
        wand.getItem().inventoryTick(wand,level,player,0,true);
        check(network.getCurrentEssence()==49600&&Wands.getVis(wand,Aspect.AIR)==100,"Real soul network vis debit");
        check(Binding.fromStack(wand).getOwnerId().equals(player.getUUID()),"Wand not bound by native BM binding");
        Wands.storeVis(wand,Aspect.AIR,Wands.getMaxVis(wand)-35);int lp=network.getCurrentEssence();BloodCompat.recharge(wand,player);
        check(lp-network.getCurrentEssence()==140,"Partial LP overcharge");
        network.setCurrentEssence(0);Wands.storeVis(wand,Aspect.AIR,0);BloodCompat.recharge(wand,player);
        check(player.getHealth()==17&&Wands.getVis(wand,Aspect.AIR)==100,"Blood wand health fallback");
        player.setHealth(6);Wands.storeVis(wand,Aspect.AIR,0);BloodCompat.recharge(wand,player);check(player.getHealth()==6&&Wands.getVis(wand,Aspect.AIR)==0,"Wand low-health guard");
        proof.put("native_network_cost",400);proof.put("partial_35_centivis_cost",140);proof.put("health_fallback_and_safe_wand_threshold",true);
        var staff=Wands.create(WandRegistry.cap("fm_alchemical"),WandRegistry.rod("fm_blood_staff"),false);
        check(Wands.getMaxVis(staff)==5000,"Blood Staff capacity must be original 50 vis");
        player.getInventory().setItem(0,staff);Wands.fill(staff);Wands.storeVis(staff,Aspect.AIR,0);player.tickCount=25;network.setCurrentEssence(10000);
        BloodCompat.recharge(staff,player);check(network.getCurrentEssence()==9600&&Wands.getVis(staff,Aspect.AIR)==100,"Staff cadence25");
        player.getInventory().setItem(0,ItemStack.EMPTY);player.getInventory().setItem(9,staff);Wands.storeVis(staff,Aspect.AIR,0);BloodCompat.recharge(staff,player);check(network.getCurrentEssence()==9600,"Staff charged outside hotbar");
        proof.put("blood_staff_50vis_25ticks_hotbar",true);

        var ink=Content.stack("bloody_scribing_tools");BloodCompat.bind(ink,player);((Scribes)ink.getItem()).setStoredDamage(ink,5);network.setCurrentEssence(1000);BloodCompat.refillInk(ink,player);
        check(ink.getDamageValue()==4&&network.getCurrentEssence()==975,"Bloody ink25LP");
        var bound=Content.stack("bound_scribing_tools");bound.setDamageValue(100);player.setItemInHand(InteractionHand.MAIN_HAND,bound);network.setCurrentEssence(20000);
        int before=Knowledge.get(player).getAspectPool(Aspect.AIR);bound.getItem().use(level,player,InteractionHand.MAIN_HAND);
        check(bound.getDamageValue()==0&&network.getCurrentEssence()==10000&&Knowledge.get(player).getAspectPool(Aspect.AIR)>=before+4,"Bound ink native LP/research");proof.put("bloody_and_bound_ink_real_network",true);

        var orb=Content.stack("eldritch_blood_orb");var orbInfo=((IBloodOrb)orb.getItem()).getOrb(orb);
        check(orbInfo.getTier()==6&&orbInfo.getCapacity()==80_000_000&&NetworkHelper.getMaximumForTier(6)==80_000_000,"Orb is not recognized by native tier system");
        proof.put("native_orb_tier6_capacity",NetworkHelper.getMaximumForTier(6));
        network.setCurrentEssence(20000);BloodCompat.strike(player);
        check(network.getCurrentEssence()==15000&&player.hasEffect(Content.BLOOD_SEAL),"Rapier LP loss/seal");
        check(network.add(new SoulTicket(200),80_000_000)==0&&network.getCurrentEssence()==15000,"Seal did not cancel actual native fill event");
        network.syphon(new SoulTicket(100));check(network.getCurrentEssence()==14900,"Seal blocked spending instead of only refilling");
        player.removeEffect(Content.BLOOD_SEAL);check(network.add(new SoulTicket(200),80_000_000)==200,"Seal did not release native network after removal");proof.put("rapier_and_native_fill_event_seal",true);

        var ritual=wayoftime.bloodmagic.BloodMagic.RITUAL_MANAGER.getRitual("forbiddenmagic_delayed_insanity");
        check(ritual instanceof SanityRitual,"Ritual not discovered by Blood Magic");var components=new ArrayList<RitualComponent>();ritual.gatherComponents(components::add);
        check(components.size()==28&&components.stream().map(RitualComponent::getOffset).distinct().count()==28,"Ritual structure");
        var pos=h.absolutePos(new BlockPos(5,3,5));level.setBlock(pos,BuiltInRegistries.BLOCK.get(ResourceLocation.parse("bloodmagic:masterritualstone")).defaultBlockState(),3);
        var stone=(TileMasterRitualStone)level.getBlockEntity(pos);stone.setOwner(player.getUUID());stone.setCachedNetwork(network);stone.setDirection(Direction.NORTH);
        for(var component:components){
            String block=switch(component.getRuneType()){case AIR->"airritualstone";case EARTH->"earthritualstone";case FIRE->"fireritualstone";case DUSK->"duskritualstone";default->"ritualstone";};
            level.setBlock(pos.offset(component.getOffset(Direction.NORTH)),BuiltInRegistries.BLOCK.get(ResourceLocation.parse("bloodmagic:"+block)).defaultBlockState(),3);
        }
        network.setCurrentEssence(100000);player.setHealth(20);player.moveTo(pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,0,0);
        var crystal=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("bloodmagic:activationcrystalawakened")));
        BloodCompat.bind(crystal,player);
        // BM explicitly rejects FakePlayer activation. A normal ServerPlayer actor uses the mock's
        // no-op transport, without a synthetic Forge login/Modonomicon handshake.
        var actor=new ServerPlayer(level.getServer(),level,player.getGameProfile(),net.minecraft.server.level.ClientInformation.createDefault());
        actor.connection=player.connection;actor.setGameMode(GameType.SURVIVAL);
        check(stone.activateRitual(crystal,actor,ritual),"Native master stone rejected28-rune ritual");
        proof.put("ritual_activation_debit",100000-network.getCurrentEssence());
        var data=level.getServer().getWorldData().overworldData();long previousTime=data.getGameTime();
        try{
            data.setGameTime(600);player.removeEffect(thaumcraft.registry.ModEffects.WARP_WARD);int prior=network.getCurrentEssence();ritual.performRitual(stone);
            check(player.hasEffect(thaumcraft.registry.ModEffects.WARP_WARD)&&prior-network.getCurrentEssence()==15000,"Real ritual protection/LP charge");
            prior=network.getCurrentEssence();ritual.performRitual(stone);check(prior==network.getCurrentEssence(),"Ritual double-charged already long ward");
            network.setCurrentEssence(0);player.removeEffect(thaumcraft.registry.ModEffects.WARP_WARD);ritual.performRitual(stone);check(!player.hasEffect(thaumcraft.registry.ModEffects.WARP_WARD),"Free ward without LP");
        }finally{data.setGameTime(previousTime);}
        proof.put("ritual28_native_master_stone_and_network",true);
        for(var component:components)level.setBlock(pos.offset(component.getOffset(Direction.NORTH)),Blocks.AIR.defaultBlockState(),3);level.removeBlock(pos,false);
        int externalRecipes=0;for(String id:List.of("blood_rod_activation","bound_scribes")){check(level.getRecipeManager().byKey(ForbiddenMagic.id(id)).isPresent(),"Missing native BM recipe "+id);externalRecipes++;}
        if(ForbiddenMagic.loaded("botania")){check(level.getRecipeManager().byKey(ForbiddenMagic.id("bloodthorn")).isPresent(),"Missing bloodthorn altar recipe");externalRecipes++;}
        proof.put("native_bm_recipes",externalRecipes);
        for(String id:List.of("taint","blood_seal")){
            var r=(wayoftime.bloodmagic.recipe.flask.RecipePotionEffect)level.getRecipeManager().byKey(ForbiddenMagic.id("flask/"+id)).orElseThrow().value();
            var flask=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("bloodmagic:alchemy_flask")));var flaskItem=(wayoftime.bloodmagic.common.item.potion.ItemAlchemyFlask)flask.getItem();
            var output=r.getOutput(flask,new ArrayList<>());flaskItem.resyncEffectInstances(output);var effects=flaskItem.getEffectHoldersOfFlask(output);
            check(effects.size()==1&&effects.getFirst().getBaseDuration()==(id.equals("taint")?450:1200),"Flask effect/duration "+id);
            player.removeEffect(Content.BLOOD_SEAL);player.removeEffect(thaumcraft.registry.ModEffects.TAINT_POISON);flaskItem.finishUsingItem(output,level,player);
            check(player.hasEffect(id.equals("taint")?thaumcraft.registry.ModEffects.TAINT_POISON:Content.BLOOD_SEAL),"Native flask drinking did not apply "+id);
        }
        proof.put("native_flask_recipe_output_and_drinking_both_effects",true);player.removeEffect(Content.BLOOD_SEAL);player.removeEffect(thaumcraft.registry.ModEffects.TAINT_POISON);return proof;
    }
}
