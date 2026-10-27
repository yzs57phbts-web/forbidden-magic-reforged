package local.forbiddenmagic;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.gear.*;
import thaumcraft.platform.Registration;

/** Original elemental material and three independent enchantment/name loadouts. */
public final class DarkTools {
    static final Tier ELEMENTAL=Registration.tier(TcMaterials.ELEMENTAL_TOOL);
    public interface Morph extends Repairable,WarpingGear {
        @Override default int getWarp(ItemStack stack,Player player){return ToolEvents.enchant(player,stack,"voidtouched")>0?1:0;}
    }
    public static class Pick extends PickaxeItem implements Morph {
        public Pick(){super(ELEMENTAL,new Properties().attributes(PickaxeItem.createAttributes(ELEMENTAL,1,-2.8F)));}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Content.get("envy_shard"));}
    }
    public static class Axe extends AxeItem implements Morph {
        public Axe(){super(ELEMENTAL,new Properties().attributes(AxeItem.createAttributes(ELEMENTAL,6,-3.1F)));}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Content.get("envy_shard"));}
    }
    public static class Shovel extends ShovelItem implements Morph {
        public Shovel(){super(ELEMENTAL,new Properties().attributes(ShovelItem.createAttributes(ELEMENTAL,1.5F,-3)));}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Content.get("envy_shard"));}
        @Override public InteractionResult useOn(UseOnContext c){return c.getPlayer()!=null&&c.getPlayer().isShiftKeyDown()?InteractionResult.PASS:super.useOn(c);}
    }
    public static class Sword extends SwordItem implements Morph {
        public Sword(){super(ELEMENTAL,new Properties().attributes(SwordItem.createAttributes(ELEMENTAL,3,-2.4F)));}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Content.get("envy_shard"));}
    }
    public static class Skull extends AxeItem implements Repairable {
        public Skull(){super(ELEMENTAL,new Properties().attributes(AxeItem.createAttributes(ELEMENTAL,6,-3.1F)));}
        @Override public float getDestroySpeed(ItemStack s,BlockState b){return 1;}
        // A battle axe: modern combat/shield handling, without acquiring a lumberjack role.
        @Override public InteractionResult useOn(UseOnContext context){return InteractionResult.PASS;}
        @Override public boolean canPerformAction(ItemStack s,net.neoforged.neoforge.common.ItemAbility action){return false;}
        private boolean combatEnchantment(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){
            return enchantment.unwrapKey().map(key->switch(key.location().toString()){
                case "minecraft:looting","minecraft:fire_aspect","minecraft:knockback","forbiddenmagic:greedy","forbiddenmagic:educational"->true;
                default->false;
            }).orElse(false);
        }
        @Override public boolean supportsEnchantment(ItemStack s,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){return combatEnchantment(enchantment)||super.supportsEnchantment(s,enchantment);}
        @Override public boolean isPrimaryItemFor(ItemStack s,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){return combatEnchantment(enchantment)||super.isPrimaryItemFor(s,enchantment);}
    }
    public static class Fork extends AxeItem implements Repairable {
        public Fork(){super(Tiers.IRON,new Properties().attributes(AxeItem.createAttributes(Tiers.IRON,6,-3.1F)));}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return InteractionResultHolder.pass(player.getItemInHand(hand));}
        @Override public InteractionResult useOn(UseOnContext context){return InteractionResult.PASS;}
        @Override public boolean canPerformAction(ItemStack stack,net.neoforged.neoforge.common.ItemAbility ability){return false;}
        @Override public String getDescriptionId(ItemStack stack){return LegacyConfig.bool("spork")?"item.forbiddenmagic.spork":super.getDescriptionId(stack);}
        @Override public float getDestroySpeed(ItemStack s,BlockState b){return 1;}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Items.IRON_INGOT);}
    }
    public static class TaintPick extends PickaxeItem implements Repairable {
        public TaintPick(){super(Registration.tier(new thaumcraft.platform.TcToolMaterial(1500,10,3,18,TcMaterials.REPAIRS_THAUMIUM,net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL)),new Properties().attributes(PickaxeItem.createAttributes(ELEMENTAL,1,-2.8F)));}
        @Override public float getDestroySpeed(ItemStack s,BlockState state){float h=state.getBlock().defaultDestroyTime();return h==0?0:h<5?0.1F:h<20?h/2:5+h;}
    }
    public static class TaintShovel extends ShovelItem implements Repairable {
        public TaintShovel(){super(ELEMENTAL,new Properties().attributes(ShovelItem.createAttributes(ELEMENTAL,1.5F,-3)));}
        @Override public float getDestroySpeed(ItemStack s,BlockState b){return b.is(thaumcraft.wand.focus.FocusUtils.TAINT)?10:super.getDestroySpeed(s,b);}
        @Override public InteractionResult useOn(UseOnContext c){
            var player=c.getPlayer();if(player==null)return InteractionResult.PASS;
            var level=c.getLevel();if(level.isClientSide())return InteractionResult.SUCCESS;
            int removed=0;var center=c.getClickedPos();
            for(var pos:net.minecraft.core.BlockPos.betweenClosed(center.offset(-5,-4,-5),center.offset(5,4,5))){
                if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos))continue;
                var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
                if(id.getNamespace().equals("thaumcraft")&&(id.getPath().equals("flux_goo")||id.getPath().equals("flux_gas"))){level.removeBlock(pos,false);removed++;}
            }
            if(removed==0)return InteractionResult.PASS;
            c.getItemInHand().hurtAndBreak(Math.min(15,removed),player,c.getHand()==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
            level.playSound(null,center,thaumcraft.registry.ModSounds.WANDFAIL.get(),net.minecraft.sounds.SoundSource.PLAYERS,0.2F,0.5F);return InteractionResult.SUCCESS;
        }
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return InteractionResultHolder.pass(player.getItemInHand(hand));}
    }
    public static int phase(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("fm_phase");}
    public static boolean cycle(ItemStack stack,Player player){
        if(!(stack.getItem() instanceof Morph)||stack.getMaxDamage()-stack.getDamageValue()<=5)return false;
        if(player.level().isClientSide())return true;
        CompoundTag data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int old=Math.floorMod(data.getInt("fm_phase"),3),next=(old+1)%3;
        ItemStack snapshot=new ItemStack(stack.getItem());snapshot.set(DataComponents.ENCHANTMENTS,stack.getOrDefault(DataComponents.ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));
        if(stack.has(DataComponents.CUSTOM_NAME))snapshot.set(DataComponents.CUSTOM_NAME,stack.get(DataComponents.CUSTOM_NAME));
        data.put("fm_loadout_"+old,snapshot.save(player.registryAccess()));
        ItemStack restore=ItemStack.parseOptional(player.registryAccess(),data.getCompound("fm_loadout_"+next));
        stack.set(DataComponents.ENCHANTMENTS,restore.getOrDefault(DataComponents.ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));
        if(restore.has(DataComponents.CUSTOM_NAME))stack.set(DataComponents.CUSTOM_NAME,restore.get(DataComponents.CUSTOM_NAME));else stack.remove(DataComponents.CUSTOM_NAME);
        data.putInt("fm_phase",next);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));stack.hurtAndBreak(5,player,EquipmentSlot.MAINHAND);
        player.level().playSound(null,player.blockPosition(),thaumcraft.registry.ModSounds.WANDFAIL.get(),net.minecraft.sounds.SoundSource.PLAYERS,0.2F,0.3F);return true;
    }
}
