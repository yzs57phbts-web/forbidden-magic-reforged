package local.forbiddenmagic.mixin;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.*;
import local.forbiddenmagic.LegacyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Keep registry entries for saved items, but never roll disabled FM enchantments. */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentConfigMixin {
    @ModifyVariable(method="getAvailableEnchantmentResults",at=@At("HEAD"),argsOnly=true)
    private static Stream<Holder<Enchantment>> fmEnabledOnly(Stream<Holder<Enchantment>> stream){
        return stream.filter(holder->holder.unwrapKey().map(key->!key.location().getNamespace().equals("forbiddenmagic")||LegacyConfig.enchantment(key.location().getPath())).orElse(true));
    }
}
