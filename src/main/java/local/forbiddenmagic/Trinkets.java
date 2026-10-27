package local.forbiddenmagic;

import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.item.bauble.VisAmuletItem;
import thaumcraft.platform.*;
import thaumcraft.item.gear.RunicArmor;

public final class Trinkets {
    public static Item collar(){return ForbiddenMagic.loaded("botania")?BotanyTrinkets.collar():new Collar();}
    public static Item ring(){return ForbiddenMagic.loaded("botania")?BotanyTrinkets.ring():new Ring();}
    public static class Collar extends VisAmuletItem {
        public Collar(){super(new Properties().stacksTo(1).rarity(Rarity.RARE),false);}
        @Override public boolean canEquip(ItemStack s,LivingEntity wearer){return true;}
        @Override public InteractionResult interactLivingEntity(ItemStack s,Player owner,LivingEntity target,InteractionHand hand){
            if(!(target instanceof Player sub)||!Baubles.get(sub,0).isEmpty())return InteractionResult.PASS;
            if(!owner.level().isClientSide()){ItemStack copy=s.copyWithCount(1);var t=copy.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();t.putString("fm_owner",owner.getName().getString());copy.set(DataComponents.CUSTOM_DATA,CustomData.of(t));Baubles.set(sub,0,copy);s.shrink(1);}
            return InteractionResult.sidedSuccess(owner.level().isClientSide());
        }
        @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> out,TooltipFlag f){super.appendHoverText(s,c,out,f);String owner=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("fm_owner");if(!owner.isEmpty())out.add(net.minecraft.network.chat.Component.translatable("fm.collar.owner",owner));}
    }
    public static class Ring extends Item implements Bauble,RunicArmor {
        public Ring(){super(new Properties().stacksTo(1).rarity(Rarity.RARE));}
        @Override public BaubleType getBaubleType(ItemStack s){return BaubleType.RING;}
        @Override public int getRunicCharge(ItemStack s){return 0;}
    }
}
