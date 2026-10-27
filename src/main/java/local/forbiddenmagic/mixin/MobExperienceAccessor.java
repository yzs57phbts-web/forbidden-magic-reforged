package local.forbiddenmagic.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobExperienceAccessor {
    @Accessor("xpReward") int fmBaseExperience();
}
