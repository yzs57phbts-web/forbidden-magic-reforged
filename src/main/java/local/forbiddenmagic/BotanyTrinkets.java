package local.forbiddenmagic;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import velrondevs.botania.api.item.CosmeticAttachable;
import velrondevs.botania.common.item.ItemStackSerialization;

/** Kept out of the optional-mod-free class-loading path. Uses Botania's own serializer. */
public final class BotanyTrinkets {
    public static net.minecraft.world.item.Item collar(){return new Collar();}
    public static net.minecraft.world.item.Item ring(){return new Ring();}
    interface Cosmetic extends CosmeticAttachable {
        @Override default ItemStack getCosmeticItem(ItemStack stack){return ItemStackSerialization.read(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("cosmeticItem"));}
        @Override default void setCosmeticItem(ItemStack stack,ItemStack cosmetic){var t=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();t.put("cosmeticItem",ItemStackSerialization.write(cosmetic));stack.set(DataComponents.CUSTOM_DATA,CustomData.of(t));}
    }
    public static final class Collar extends Trinkets.Collar implements Cosmetic {}
    public static final class Ring extends Trinkets.Ring implements Cosmetic {}
}
