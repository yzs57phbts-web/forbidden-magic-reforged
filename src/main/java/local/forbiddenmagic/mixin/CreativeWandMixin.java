package local.forbiddenmagic.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.*;

/** One central server-side guard covers foci and workbench crafting, including zero-cost caps. */
@Mixin(value=WandItem.class,remap=false)
public abstract class CreativeWandMixin {
    @Inject(method="consumeVis",at=@At("HEAD"),cancellable=true,remap=false)
    private void fmCreativeSingle(ItemStack stack,Player player,thaumcraft.aspect.Aspect aspect,int amount,boolean crafting,CallbackInfoReturnable<Boolean> cir){
        if((Wands.getRod(stack).tag().startsWith("fm_neutronium")||Wands.getCap(stack).tag().equals("fm_orichalcum"))&&(player==null||!player.isCreative()))cir.setReturnValue(false);
    }
    @Inject(method="consumeAllVis",at=@At("HEAD"),cancellable=true,remap=false)
    private void fmCreativeOnly(ItemStack stack,Player player,AspectList aspects,boolean doit,boolean crafting,CallbackInfoReturnable<Boolean> cir){
        if((Wands.getRod(stack).tag().startsWith("fm_neutronium")||Wands.getCap(stack).tag().equals("fm_orichalcum"))&&(player==null||!player.isCreative()))cir.setReturnValue(false);
    }
}
