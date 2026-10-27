package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.gametest.*;
import static local.forbiddenmagic.tests.PortTests.check;

/** Observations of current behaviour, deliberately not assertions that every legacy detail matches. */
@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class ParityProbe121Tests {
    @GameTest(batch="parity_probes",template="test/platform",timeoutTicks=300)
    public static void currentBehaviour(GameTestHelper h)throws Exception{
        if(!Boolean.getBoolean("forbiddenmagic.auditProbe")){h.succeed();return;}
        var l=h.getLevel();var p=HeadlessPlayer.create(l,"parity121");l.addNewPlayer(p);p.setNoGravity(true);var origin=h.absolutePos(new BlockPos(7,7,7));p.moveTo(origin.getX()+.5,origin.getY(),origin.getZ()+.5,0,0);
        var proof=new LinkedHashMap<String,Object>();
        var fruit=Content.stack("tainted_fruit");p.getFoodData().setFoodLevel(0);p.setItemInHand(InteractionHand.MAIN_HAND,fruit);p.startUsingItem(InteractionHand.MAIN_HAND);for(int n=0;n<40;n++)p.doTick();
        check(p.getMainHandItem().isEmpty(),"Native fruit consumption did not complete");proof.put("fruit_vanilla_poison",p.hasEffect(MobEffects.POISON));proof.put("fruit_hunger",p.hasEffect(MobEffects.HUNGER));proof.put("fruit_taint_poison",p.hasEffect(thaumcraft.registry.ModEffects.TAINT_POISON));p.removeAllEffects();
        var focus=(BlinkFocus)Content.get("blink_focus");proof.put("blink_cooldown_ticks",focus.getActivationCooldown(Content.stack("blink_focus")));proof.put("blink_initial_damage_is_fire",l.damageSources().indirectMagic(p,p).is(DamageTypeTags.IS_FIRE));
        var blaze=EntityType.BLAZE.create(l);blaze.setNoAi(true);blaze.moveTo(origin.getX()+3,origin.getY(),origin.getZ(),0,0);l.addFreshEntity(blaze);float before=blaze.getHealth();blaze.hurt(l.damageSources().indirectMagic(p,p),3);proof.put("blaze_initial_magic_hp_loss",before-blaze.getHealth());blaze.discard();
        var flux=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:flux_goo"));BlockPos clicked=origin.offset(0,0,12),near=origin.east(2),far=clicked.east(2);l.getChunkAt(far);l.setBlockAndUpdate(near,flux.defaultBlockState());l.setBlockAndUpdate(far,flux.defaultBlockState());
        var shovel=Content.stack("taint_shovel");p.setItemInHand(InteractionHand.MAIN_HAND,shovel);shovel.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(clicked),Direction.UP,clicked,false)));
        proof.put("purifier_removed_near_player",l.isEmptyBlock(near));proof.put("purifier_removed_near_clicked_block",l.isEmptyBlock(far));l.removeBlock(far,false);l.removeBlock(near,false);
        proof.put("thaumic_slime_cage_aspect",WrathCage.SOULS.get(ResourceLocation.parse("thaumcraft:thaumic_slime")).getTag());
        proof.put("crystal_ink_max_damage",Content.stack("crystal_scribing_tools").getMaxDamage());
        var ward=new MobEffectInstance(thaumcraft.registry.ModEffects.WARP_WARD,602);proof.put("ritual_ward_default_has_cures",!ward.getCures().isEmpty());
        if(ForbiddenMagic.loaded("bloodmagic")){
            var rapier=Content.stack("rapier_of_the_strix");var voidIngot=BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:void_metal_ingot"));check(voidIngot!=Items.AIR,"Wrong void ingot lookup");
            proof.put("rapier_repairs_with_iron",rapier.getItem().isValidRepairItem(rapier,new ItemStack(Items.IRON_INGOT)));proof.put("rapier_repairs_with_void",rapier.getItem().isValidRepairItem(rapier,new ItemStack(voidIngot)));
        }
        proof.put("status","PASS");proof.put("method","Current native 1.21.1 observations; legacy counterparts established separately by source/bytecode. These are not old-runtime trials.");
        Files.writeString(Path.of("parity-121-probe-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));p.discard();h.succeed();
    }
}
