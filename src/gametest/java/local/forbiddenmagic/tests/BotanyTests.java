package local.forbiddenmagic.tests;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import local.forbiddenmagic.*;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;
import thaumcraft.research.Knowledge;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.*;
import static local.forbiddenmagic.tests.PortTests.check;

public final class BotanyTests {
    public static void verifyFloating(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos){for(var name:BotanyContent.FLOWERS.keySet()){level.setBlockAndUpdate(pos,BotanyContent.FLOATERS.get(name).get().defaultBlockState());check(level.getBlockEntity(pos) instanceof velrondevs.botania.api.block_entity.SpecialFlowerBlockEntity f&&f.isFloating(),"Floating flower not native "+name);check(level.getRecipeManager().byKey(ForbiddenMagic.id("floating_"+name)).isPresent(),"Missing floating recipe "+name);}}
    static void set(GameTestHelper h,BlockPos p,String id){h.getLevel().setBlock(p,BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState(),3);}
    public static Map<String,Object> verify(GameTestHelper h,ServerPlayer player)throws Exception{
        var level=h.getLevel();var proof=new LinkedHashMap<String,Object>();
        player.getInventory().clearContent();
        var tablet=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("botania:mana_tablet")));
        var mana=tablet.getCapability(BotaniaCapabilities.MANA_ITEM);check(mana!=null,"No real Botania mana capability");mana.addMana(50000);
        player.getInventory().setItem(10,tablet);
        var wand=Wands.create(WandRegistry.cap("fm_manasteel"),WandRegistry.rod("fm_livingwood"),false);
        Wands.fill(wand);Wands.storeVis(wand,Aspect.AIR,0);player.getInventory().setItem(0,wand);player.tickCount=40;
        int before=mana.getMana();wand.getItem().inventoryTick(wand,level,player,0,true);
        check(before-mana.getMana()==600&&Wands.getVis(wand,Aspect.AIR)==100,"Mana to vis rate");
        Wands.storeVis(wand,Aspect.AIR,Wands.getMaxVis(wand)-35);before=mana.getMana();BotanyContent.recharge(wand,player);
        check(before-mana.getMana()==210&&Wands.getVis(wand,Aspect.AIR)==Wands.getMaxVis(wand),"Partial mana overcharge");
        player.getInventory().setItem(0,ItemStack.EMPTY);player.getInventory().setItem(9,wand);Wands.storeVis(wand,Aspect.AIR,0);before=mana.getMana();BotanyContent.recharge(wand,player);
        check(before==mana.getMana()&&Wands.getVis(wand,Aspect.AIR)==0,"Botania wand charged outside hotbar");
        proof.put("real_mana_tablet_cost",600);proof.put("partial_35_centivis_cost",210);proof.put("hotbar_guard",true);

        var pos=h.absolutePos(new BlockPos(5,4,5));var poolPos=pos.east(3);level.setBlock(pos.below(),Blocks.DIRT.defaultBlockState(),3);
        set(h,poolPos,"botania:mana_pool");
        var poolTile=(velrondevs.botania.common.block.block_entity.mana.ManaPoolBlockEntity)level.getBlockEntity(poolPos);
        velrondevs.botania.common.block.block_entity.mana.ManaPoolBlockEntity.serverTick(level,poolPos,poolTile.getBlockState(),poolTile);
        var pool=(ManaPool)poolTile;pool.receiveMana(9000);check(pool.getCurrentMana()==9000,"Native pool not initialized");
        set(h,pos,"forbiddenmagic:euclidaisy");var flower=(Flowers.Functional)level.getBlockEntity(pos);flower.setBindingPos(poolPos);flower.ticksExisted=399;
        var box=new AABB(pos).inflate(2);level.getEntitiesOfClass(ItemEntity.class,box).forEach(Entity::discard);flower.tickFlower();
        var drops=level.getEntitiesOfClass(ItemEntity.class,box);check(drops.size()==1&&drops.getFirst().getItem().is(thaumcraft.registry.ModItems.WISP_ESSENCE.get()),"Euclidaisy did not create real ethereal essence: drops="+drops.size()+" mana="+flower.getMana()+" pool="+pool.getCurrentMana()+" tick="+flower.ticksExisted);
        check(pool.getCurrentMana()==0&&flower.getMana()==0,"Euclidaisy did not debit real pool");check(AspectTooltipsAmount(drops.getFirst().getItem())==2,"Euclidaisy essence amount");
        drops.forEach(Entity::discard);flower.addMana(9000);flower.ticksExisted=399;level.setBlock(pos.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);flower.tickFlower();
        check(flower.getMana()==9000&&level.getEntitiesOfClass(ItemEntity.class,box).isEmpty(),"Redstone did not stop Euclidaisy");
        level.removeBlock(pos.north(),false);level.removeBlock(poolPos,false);proof.put("euclidaisy_pool_and_redstone",true);

        set(h,pos,"forbiddenmagic:whisperweed");flower=(Flowers.Functional)level.getBlockEntity(pos);flower.addMana(6000);flower.ticksExisted=299;
        player.moveTo(pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,0,0);
        int knowledge=0;for(var a:Aspect.getEnabledAspects())knowledge+=Knowledge.get(player).getAspectPool(a);
        flower.tickFlower();int after=0;for(var a:Aspect.getEnabledAspects())after+=Knowledge.get(player).getAspectPool(a);
        check(after>knowledge&&flower.getMana()==0,"Whisperweed did not grant knowledge: mana="+flower.getMana()+" ticks="+flower.ticksExisted+" player="+player.position()+" flower="+pos+" players="+level.players().size());proof.put("whisperweed_research_awarded",after-knowledge);
        if(ForbiddenMagic.loaded("bloodmagic")){
            set(h,pos,"forbiddenmagic:bloodthorn");flower=(Flowers.Functional)level.getBlockEntity(pos);flower.addMana(80);
            var zombie=EntityType.ZOMBIE.create(level);zombie.setNoAi(true);zombie.moveTo(pos.getX()+1,pos.getY(),pos.getZ()+0.5);level.addFreshEntity(zombie);
            flower.tickFlower();check(zombie.hasEffect(MobEffects.WEAKNESS)&&zombie.getEffect(MobEffects.WEAKNESS).getAmplifier()==2&&flower.getMana()==40,"Bloodthorn effect/cost");
            flower.tickFlower();check(flower.getMana()==40,"Bloodthorn charged twice for existing weakness");zombie.discard();proof.put("bloodthorn_weakness_iii_cost",40);
        }
        set(h,pos,"forbiddenmagic:tainthistle");var thistle=(Flowers.Tainthistle)level.getBlockEntity(pos);
        var fpos=pos.west();level.setBlock(fpos,thaumcraft.registry.ModBlocks.FLUX_GOO.get().defaultBlockState().setValue(thaumcraft.alchemy.FluxBlock.LEVEL,2),3);
        level.setBlock(fpos.north(),thaumcraft.registry.ModBlocks.FLUX_GAS.get().defaultBlockState().setValue(thaumcraft.alchemy.FluxBlock.LEVEL,3),3);
        thistle.ticksExisted=79;thistle.tickFlower();check(level.isEmptyBlock(fpos)&&level.isEmptyBlock(fpos.north()),"Tainthistle left flux");
        check(thistle.burnTime()==99&&thistle.getMana()==3,"Modern flux quanta conversion "+thistle.burnTime()+"/"+thistle.getMana());
        set(h,poolPos,"botania:mana_spreader");thistle.setBindingPos(poolPos);thistle.tickFlower();var spreader=(ManaCollector)level.getBlockEntity(poolPos);
        check(spreader.getCurrentMana()==3,"Thistle mana not transferred to real spreader");
        var tag=new net.minecraft.nbt.CompoundTag();thistle.writeToPacketNBT(tag,level.registryAccess());var restored=new Flowers.Tainthistle(pos,thistle.getBlockState());restored.readFromPacketNBT(tag,level.registryAccess());
        check(restored.burnTime()==thistle.burnTime(),"Burn time not persistent");proof.put("tainthistle_quanta_and_real_spreader_and_nbt",true);
        level.removeBlock(pos,false);level.removeBlock(poolPos,false);player.getInventory().clearContent();return proof;
    }
    static int AspectTooltipsAmount(ItemStack stack){return thaumcraft.item.AspectTooltips.getAspects(stack).visSize();}
}
