package local.forbiddenmagic;

import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.research.*;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModItems;

/** Subclassing the actual TC scribe class keeps ordinary research-table slot/ink handling. */
public class Scribes extends ScribingToolsItem {
    public enum Kind { CRYSTAL, BLOODY, BOUND, PRIMAL }
    public final Kind kind;
    public Scribes(Kind kind) { super(new Item.Properties().durability(kind==Kind.BLOODY?5:100).setNoRepair());this.kind=kind; }
    @Override public void inventoryTick(ItemStack stack,Level level,Entity owner,int slot,boolean selected) {
        if(!level.isClientSide()&&kind==Kind.CRYSTAL&&LegacyConfig.integer("researchDifficulty")==-1&&owner instanceof ServerPlayer player&&owner.tickCount%200==0){
            var primals=Aspect.getPrimalAspects();var aspect=primals.get(level.random.nextInt(primals.size()));Knowledge.get(player).addAspectPool(aspect,1+level.random.nextInt(4));Knowledge.sync(player);
            setStoredDamage(stack,stack.getDamageValue()+1);if(stack.getDamageValue()>=getMaxDamage(stack))stack.shrink(1);
        }
        if(!level.isClientSide()&&kind==Kind.BLOODY&&LegacyConfig.integration("bloodmagic")&&owner instanceof Player player&&stack.getDamageValue()>0)
            BloodCompat.refillInk(stack,player);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if((kind==Kind.BLOODY||kind==Kind.BOUND)&&!LegacyConfig.integration("bloodmagic"))return InteractionResultHolder.pass(stack);
        if(kind==Kind.CRYSTAL&&LegacyConfig.integer("researchDifficulty")==-1)return InteractionResultHolder.pass(stack);
        if(level.isClientSide())return InteractionResultHolder.success(stack);
        if(kind==Kind.BLOODY||kind==Kind.BOUND)BloodCompat.bind(stack,player);
        if(stack.getDamageValue()<stack.getMaxDamage())return InteractionResultHolder.pass(stack);
        if(kind==Kind.BLOODY)return InteractionResultHolder.pass(stack);
        if(kind==Kind.BOUND&&!player.hasInfiniteMaterials()&&!BloodCompat.payOrHurt(stack,player,10000))return InteractionResultHolder.fail(stack);
        if(player instanceof ServerPlayer serverPlayer)for(var aspect:Aspect.getPrimalAspects())ResearchManager.awardAspect(serverPlayer,aspect,4+level.random.nextInt(4));
        Knowledge.sync(player);
        if(kind==Kind.CRYSTAL) { var empty=new ItemStack(ModItems.SCRIBING_TOOLS.get());empty.setDamageValue(100);return InteractionResultHolder.success(empty); }
        stack.setDamageValue(0);return InteractionResultHolder.success(stack);
    }
    @Override public int getMaxDamage(ItemStack stack){return kind==Kind.CRYSTAL&&LegacyConfig.integer("researchDifficulty")==-1?50:super.getMaxDamage(stack);}
    @Override public boolean isFoil(ItemStack stack) { return kind!=Kind.BLOODY&&!(kind==Kind.CRYSTAL&&LegacyConfig.integer("researchDifficulty")==-1)&&stack.getDamageValue()>=stack.getMaxDamage(); }
    public void setStoredDamage(ItemStack stack,int damage){super.setDamage(stack,kind==Kind.PRIMAL?0:damage);}
    @Override public void setDamage(ItemStack stack,int damage){
        if(kind==Kind.BLOODY&&damage>0&&LegacyConfig.integration("bloodmagic")&&BloodCompat.payBoundInk(stack,25*damage)){super.setDamage(stack,0);return;}
        setStoredDamage(stack,damage);
    }
}
