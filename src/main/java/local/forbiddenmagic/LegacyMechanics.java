package local.forbiddenmagic;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class LegacyMechanics {
    private static final Map<EntityType<?>,Class<?>> COUNTER_CLASSES=new HashMap<>();
    private static final Set<String> HORSES=Set.of("horse","donkey","mule","skeleton_horse","zombie_horse");
    /** Original Sword material damage+4. The old ItemTool collection cast contributed zero. */
    public static int wrathWeaponBonus(ItemStack weapon){
        if(weapon.getItem() instanceof SwordItem sword)return (int)(sword.getTier().getAttackDamageBonus()+4);
        if(weapon.is(Content.get("skull_axe")))return 7; // originally an elemental ItemSword
        if(weapon.is(Content.get("diabolists_fork")))return (int)(Tiers.IRON.getAttackDamageBonus()+4); // retain its original sword route after the requested axe adaptation
        return 0;
    }
    public static AABB cageLimitBox(net.minecraft.core.BlockPos pos){return new AABB(pos).inflate(8,4,8);}
    public static boolean sameCageFamily(EntityType<?> selected,Mob other,net.minecraft.world.level.Level level){
        String name=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(selected).toString();
        String candidate=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(other.getType()).toString();
        if(Set.of("minecraft:skeleton","minecraft:wither_skeleton","minecraft:stray","minecraft:bogged").contains(name))return other instanceof AbstractSkeleton;
        if(name.startsWith("minecraft:")&&HORSES.contains(name.substring(10)))return candidate.startsWith("minecraft:")&&HORSES.contains(candidate.substring(10));
        if(name.equals("minecraft:cat")||name.equals("minecraft:ocelot"))return candidate.equals("minecraft:cat")||candidate.equals("minecraft:ocelot");
        Class<?> counter=COUNTER_CLASSES.computeIfAbsent(selected,type->{var entity=type.create(level);return entity==null?Void.class:entity.getClass();});
        return counter.isInstance(other);
    }
    public static DamageSource hellfire(Player player){
        return new DamageSource(player.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,ForbiddenMagic.id("blink_hellfire"))),player);
    }
}
