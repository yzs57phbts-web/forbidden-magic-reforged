package local.forbiddenmagic;

import com.mojang.serialization.*;
import net.neoforged.neoforge.common.conditions.ICondition;

public record FeatureCondition(String feature) implements ICondition {
    public static final MapCodec<FeatureCondition> CODEC=Codec.STRING.fieldOf("feature").xmap(FeatureCondition::new,FeatureCondition::feature);
    @Override public boolean test(IContext context){return LegacyConfig.feature(feature);}
    @Override public MapCodec<? extends ICondition> codec(){return CODEC;}
}
