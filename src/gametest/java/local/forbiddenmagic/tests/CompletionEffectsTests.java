package local.forbiddenmagic.tests;

import java.util.*;
import java.nio.file.*;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;
import thaumcraft.platform.Baubles;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class CompletionEffectsTests {
    @GameTest(batch="completionEffects",template="test/platform",timeoutTicks=400)
    public static void trinketsAndBlink(GameTestHelper h)throws Exception{
        var level=h.getLevel();var p=HeadlessPlayer.create(level,"fm-effects");level.addNewPlayer(p);
        var pos=h.absolutePos(new BlockPos(5,5,5));p.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);p.setHealth(20);var proof=new LinkedHashMap<String,Object>();
        check(DyeItem.byColor(DyeColor.BLACK)==Items.BLACK_DYE,"Floral powder replaced vanilla dye lookup");
        var blackSheep=EntityType.SHEEP.create(level);var whiteSheep=EntityType.SHEEP.create(level);blackSheep.setColor(DyeColor.BLACK);whiteSheep.setColor(DyeColor.WHITE);
        check(blackSheep.getBreedOffspring(level,whiteSheep).getColor()==DyeColor.GRAY,"Vanilla sheep colour mixing changed");var powder=Content.stack("black_dye");powder.getItem().interactLivingEntity(powder,p,whiteSheep,InteractionHand.MAIN_HAND);check(whiteSheep.getColor()==DyeColor.BLACK&&powder.isEmpty(),"Floral powder cannot dye sheep");proof.put("floral_powder_dyes_sheep_without_shadowing_vanilla_breeding",true);
        Baubles.set(p,0,Content.stack("submissive_collar"));var collar=Baubles.get(p,0);check(Baubles.isEquipped(p,Content.get("submissive_collar")),"Native Curios necklace missing");p.hurt(level.damageSources().genericKill(),4);check(VisStorage.getAllVis(collar).visSize()==1200,"Pain vis not generated "+VisStorage.getAllVis(collar).visSize());
        var held=Wands.create(WandRegistry.CAP_IRON,WandRegistry.ROD_GREATWOOD,false);p.setItemInHand(InteractionHand.MAIN_HAND,held);p.tickCount=5;((Trinkets.Collar)collar.getItem()).onWornTick(collar,p);check(Wands.getAllVis(held).visSize()>0&&VisStorage.getAllVis(collar).visSize()<1200,"Collar native amulet transfer");proof.put("actual_damage_curios_collar_vis_and_native_amulet_transfer",true);
        Baubles.set(p,1,Content.stack("ring_of_nutrition"));Baubles.set(p,2,Content.stack("ring_of_nutrition"));p.getFoodData().setFoodLevel(10);p.getFoodData().setSaturation(0);
        var finish=new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(p,new ItemStack(Items.APPLE),0,ItemStack.EMPTY);ToolEvents.food(finish);check(p.getFoodData().getFoodLevel()==12&&p.getFoodData().getSaturationLevel()==8,"Nutrition double stacked/missing");proof.put("nutrition_two_curios_rings_single_food_bonus",true);
        Baubles.set(p,0,ItemStack.EMPTY);Baubles.set(p,1,ItemStack.EMPTY);Baubles.set(p,2,ItemStack.EMPTY);
        var cakePos=pos.east(4);level.setBlockAndUpdate(cakePos.below(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(cakePos,Completion.block("arcane_cake").defaultBlockState());p.getFoodData().setFoodLevel(0);var hit=new BlockHitResult(Vec3.atCenterOf(cakePos),Direction.UP,cakePos,false);
        for(int i=0;i<5;i++){p.getFoodData().setFoodLevel(0);level.getBlockState(cakePos).useWithoutItem(level,p,hit);}check(level.getBlockState(cakePos).getValue(Ecology.Cake.BITES)==5,"Cake not eaten");
        level.getBlockState(cakePos).randomTick(level,cakePos,level.random);check(level.getBlockState(cakePos).getValue(Ecology.Cake.BITES)==4,"Cake not regrown");
        for(int i=0;i<8;i++){p.getFoodData().setFoodLevel(0);level.getBlockState(cakePos).useWithoutItem(level,p,hit);}check(level.isEmptyBlock(cakePos),"Cake did not have exactly twelve slices");proof.put("cake_twelve_slices_random_tick_regrowth",true);
        var wand=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_SILVERWOOD,false);var focus=Content.stack("blink_focus");var item=(BlinkFocus)focus.getItem();check(item.applyUpgrade(focus,BlinkFocus.HELLFIRE,2),"Hellfire apply failed");item.applyUpgrade(focus,thaumcraft.wand.focus.FocusUpgradeType.POTENCY,3);Wands.setFocus(wand,focus);Wands.fill(wand);p.setItemInHand(InteractionHand.MAIN_HAND,wand);
        var target=pos.south(9);level.setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());var zombie=EntityType.ZOMBIE.create(level);zombie.setNoAi(true);thaumcraft.entity.monster.ChampionKeys.MODIFIER.set(zombie,thaumcraft.entity.monster.ChampionModifier.NORMAL);zombie.moveTo(target.getX()+1.5,target.getY(),target.getZ()-0.5);level.addFreshEntity(zombie);
        // Aim at the front face; the real focus ray and native wand cost pipeline are used.
        var caster=HeadlessPlayer.create(level,"fm-blink");level.addNewPlayer(caster);
        caster.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);caster.setItemInHand(InteractionHand.MAIN_HAND,wand);double dy=target.getY()+0.5-caster.getEyeY(),distance=target.getZ()-caster.getZ();caster.setXRot((float)-Math.toDegrees(Math.atan2(dy,distance)));caster.setYHeadRot(0);caster.yHeadRotO=0;caster.xRotO=caster.getXRot();
        var aimed=thaumcraft.wand.focus.FocusUtils.getTargetBlock(level,caster,false,128);check(aimed!=null&&aimed.getBlockPos().equals(target),"Fixture aim misses target: "+aimed+" yaw="+caster.getYRot()+" head="+caster.getYHeadRot());
        int entropy=Wands.getVis(wand,Aspect.ENTROPY),fire=Wands.getVis(wand,Aspect.FIRE);var result=item.onFocusRightClick(wand,level,caster,null);check(result.consumesAction()&&caster.getZ()>target.getZ()-1,"Blink did not teleport via ray "+caster.position());check(Wands.getVis(wand,Aspect.ENTROPY)<entropy&&Wands.getVis(wand,Aspect.FIRE)<fire,"Blink no native cost");check(zombie.getHealth()<20&&zombie.isOnFire(),"Blink Hellfire not applied: hp="+zombie.getHealth()+" fire="+zombie.getRemainingFireTicks()+" mob="+zombie.position()+" caster="+caster.position());proof.put("blink_real_ray_native_vis_hellfire_potency",true);
        var pandemonium=Content.stack("blink_focus");item.applyUpgrade(pandemonium,BlinkFocus.PANDEMONIUM,2);Wands.setFocus(wand,pandemonium);Wands.fill(wand);caster.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);caster.setYHeadRot(0);caster.setXRot((float)-Math.toDegrees(Math.atan2(dy,distance)));caster.xRotO=caster.getXRot();caster.yHeadRotO=0;var oldPosition=caster.position();
        result=item.onFocusRightClick(wand,level,caster,null);check(result.consumesAction()&&zombie.position().distanceTo(oldPosition)<0.1&&caster.position().distanceTo(oldPosition)>5,"Pandemonium did not swap hostile to original location");proof.put("blink_pandemonium_actual_hostile_relocation",true);zombie.discard();caster.discard();
        var ignition=Wands.create(WandRegistry.CAP_GOLD,WandRegistry.ROD_GREATWOOD,false);Wands.storeVis(ignition,Aspect.FIRE,100);thaumcraft.research.Knowledge.get(p).addResearch("FM_HELLFIRE");var firePos=pos.west(4);level.setBlockAndUpdate(firePos,Blocks.OBSIDIAN.defaultBlockState());level.removeBlock(firePos.above(),false);
        check(WandTriggers.perform(level,ignition,p,firePos,Direction.UP)&&level.getBlockState(firePos.above()).is(Blocks.FIRE)&&Wands.getVis(ignition,Aspect.FIRE)==98,"Hidden Hellfire native wand trigger/cost");proof.put("hidden_hellfire_native_trigger_two_centivis",true);
        var fruit=Content.stack("tainted_fruit");ToolEvents.food(new net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish(p,fruit,0,ItemStack.EMPTY));check(p.hasEffect(thaumcraft.registry.ModEffects.TAINT_POISON)&&p.getEffect(thaumcraft.registry.ModEffects.TAINT_POISON).getCures().isEmpty(),"Fruit taint/cure rule");proof.put("fruit_taint_and_uncurable_effect",true);
        proof.put("status","PASS");p.discard();Files.writeString(Path.of("completion-effects-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();
    }
}
