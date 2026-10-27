package local.forbiddenmagic;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModEffects;
import thaumcraft.wand.*;

public final class LegacyWands {
    public static void init() {
        rod("infernal",150,"infernal_rod",12,false,LegacyWands::infernal,false);
        rod("tainted",150,"tainted_rod",12,false,LegacyWands::tainted,false);
        if(ForbiddenMagic.loaded("botania")) {
            rod("livingwood",100,"livingwood_rod",12,false,BotanyContent::recharge,false);
            rod("dreamwood",100,"dreamwood_rod",12,false,BotanyContent::recharge,false);
            rod("dreamwood",250,"dreamwood_staff_core",25,true,BotanyContent::recharge,true);
            cap("manasteel",0.9F,List.of(),0,"manasteel_cap",6);
            cap("elementium",0.8F,List.of(),0,"elementium_cap",9);
        }
        if(ForbiddenMagic.loaded("bloodmagic")) {
            rod("blood",100,"blood_rod",12,false,BloodCompat::recharge,false);
            // Exact original: the fast, dangerous Blood Staff holds only 50 vis, not 250.
            rod("blood",50,"blood_staff_core",24,true,BloodCompat::recharge,false);
            cap("alchemical",0.9F,List.of(Aspect.WATER),0.8F,"alchemical_cap",7);
        }
    }
    private static void rod(String name,int capacity,String item,int craft,boolean staff,RodUpdate update,boolean runes) {
        WandRegistry.registerRod("fm_"+name,capacity,ForbiddenMagic.id(item),craft,update,false,staff,runes,ForbiddenMagic.id("textures/models/wand_rod_"+name+".png"));
    }
    private static void cap(String name,float cost,List<Aspect> special,float specialCost,String item,int craft) {
        WandRegistry.registerCap("fm_"+name,cost,special,specialCost,ForbiddenMagic.id(item),craft,null,ForbiddenMagic.id("textures/models/wand_cap_"+name+".png"));
    }
    public static boolean hotbar(ItemStack stack,Player player) {
        for(int n=0;n<9;n++)if(player.getInventory().getItem(n)==stack)return true;
        return false;
    }
    public static void infernal(ItemStack stack,Player player) {
        if(player.level().isClientSide())return;
        if(player.tickCount%100==0)for(Aspect aspect:Aspect.getPrimalAspects()) {
            int fraction=aspect==Aspect.FIRE?5:10;
            if((aspect==Aspect.FIRE||player.level().dimension()==Level.NETHER)&&Wands.getVis(stack,aspect)<Wands.getMaxVis(stack)/fraction)
                Wands.addVis(stack,aspect,1,true);
        }
        player.clearFire(); player.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
    }
    public static void tainted(ItemStack stack,Player player) {
        if(player.level().isClientSide())return;
        if(player.tickCount%100==0&&player.level().getBiome(player.blockPosition()).is(ResourceLocation.parse("thaumcraft:tainted_land")))
            for(Aspect aspect:Aspect.getPrimalAspects())if(Wands.getVis(stack,aspect)<Wands.getMaxVis(stack)/10)Wands.addVis(stack,aspect,1,true);
        // The original held-wand taint-damage reaction is implemented in CoreEvents.damage.
    }
}
