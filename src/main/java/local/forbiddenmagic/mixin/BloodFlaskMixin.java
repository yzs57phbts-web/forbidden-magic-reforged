package local.forbiddenmagic.mixin;

import java.util.Collection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** BM 3.4.0-46-alpha writes legacy NBT while drinking reads POTION_CONTENTS. */
@Pseudo @Mixin(targets="wayoftime.bloodmagic.common.item.potion.ItemAlchemyFlask",remap=false)
public abstract class BloodFlaskMixin {
    @Inject(method="setEffectsOfFlask",at=@At("RETURN"),remap=false)
    private void fmFlaskComponents(ItemStack stack,Collection<MobEffectInstance> effects,CallbackInfoReturnable<ItemStack> cir){local.forbiddenmagic.BloodCompat.syncFlask(stack,effects);}
}
