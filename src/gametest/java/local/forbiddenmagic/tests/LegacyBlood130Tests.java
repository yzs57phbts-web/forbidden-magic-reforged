package local.forbiddenmagic.tests;

import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import static local.forbiddenmagic.tests.PortTests.check;

/** Optional implementation behind a common-only signature. */
public final class LegacyBlood130Tests {
    public static Map<String,Object> configuredCosts(ServerPlayer p){
        var network=NetworkHelper.getSoulNetwork(p);network.setOrbTier(6);var proof=new LinkedHashMap<String,Object>();
        for(String cap:List.of("iron","fm_alchemical")){
            var wand=thaumcraft.wand.Wands.create(thaumcraft.wand.WandRegistry.cap(cap),thaumcraft.wand.WandRegistry.rod("fm_blood"),false);thaumcraft.wand.Wands.fill(wand);int max=thaumcraft.wand.Wands.getMaxVis(wand);thaumcraft.wand.Wands.storeVis(wand,thaumcraft.aspect.Aspect.AIR,max-35);network.setCurrentEssence(10000);p.tickCount=100;BloodCompat.recharge(wand,p);
            int expected=35*Math.max(0,LegacyConfig.integer("bloodvis")-(cap.equals("fm_alchemical")?1:0));check(10000-network.getCurrentEssence()==expected&&thaumcraft.wand.Wands.getVis(wand,thaumcraft.aspect.Aspect.AIR)==max,"Configured LP price ignored "+cap);proof.put(cap,expected);
        }return proof;
    }
    public static Map<String,Object> verify(GameTestHelper h,ServerPlayer p)throws Exception{
        var l=h.getLevel();p.getInventory().clearContent();p.setHealth(20);var network=NetworkHelper.getSoulNetwork(p);network.setOrbTier(6);network.setCurrentEssence(10000);var field=SoulNetwork.class.getDeclaredField("cachedPlayer");field.setAccessible(true);field.set(network,p);var proof=new LinkedHashMap<String,Object>();
        var ink=Content.stack("bloody_scribing_tools");BloodCompat.bind(ink,p);ink.setDamageValue(1);check(ink.getDamageValue()==0&&network.getCurrentEssence()==9975,"Writing did not immediately pay25LP");ink.setDamageValue(2);check(ink.getDamageValue()==0&&network.getCurrentEssence()==9925,"Backlog ink payment wrong");
        network.setCurrentEssence(0);ink.setDamageValue(1);check(ink.getDamageValue()==1,"Unpaid ink did not retain damage");network.setCurrentEssence(1000);BloodCompat.refillInk(ink,p);check(ink.getDamageValue()==0&&network.getCurrentEssence()==975,"Inventory ink double-charged");proof.put("7_immediate_bound_writing_and_single_refill_payment",true);
        var rapier=Content.stack("rapier_of_the_strix");var sword=(SwordItem)rapier.getItem();var material=sword.getTier();var voidIngot=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:void_metal_ingot"));
        check(material.getUses()==700&&material.getSpeed()==10&&material.getAttackDamageBonus()==-3&&material.getEnchantmentValue()==18,"Rapier lost custom material");check(rapier.getItem() instanceof thaumcraft.item.gear.Repairable&&rapier.getItem().isValidRepairItem(rapier,new ItemStack(voidIngot))&&!rapier.getItem().isValidRepairItem(rapier,new ItemStack(Items.IRON_INGOT)),"Rapier repair material/marker wrong");proof.put("13_custom_material_void_repair",true);
        p.getInventory().clearContent();network.setCurrentEssence(3000);p.addEffect(new MobEffectInstance(Content.BLOOD_SEAL,1200));network.setCurrentEssence(4000);p.doTick();check(network.getCurrentEssence()==3000,"Direct LP gain bypassed seal");network.setCurrentEssence(1000);p.doTick();network.setCurrentEssence(1500);p.doTick();check(network.getCurrentEssence()==1000,"Seal did not lower ceiling after spending");
        p.removeEffect(Content.BLOOD_SEAL);p.doTick();network.setCurrentEssence(4000);p.doTick();check(network.getCurrentEssence()==4000,"Seal stuck after expiration");proof.put("14_direct_setter_tick_ceiling_and_release",true);
        var ward=SanityRitual.wardEffect();check(ward.getCures().isEmpty(),"Ritual ward curable");p.addEffect(ward);p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100));new ItemStack(Items.MILK_BUCKET).finishUsingItem(l,p);check(p.hasEffect(thaumcraft.registry.ModEffects.WARP_WARD)&&!p.hasEffect(MobEffects.WEAKNESS),"Milk removes ward or fixture milk failed");p.removeAllEffects();proof.put("15_native_milk_preserves_ritual_ward",true);return proof;
    }
}
