package local.forbiddenmagic;

import net.neoforged.fml.common.Mod;
import thaumcraft.aspect.AspectSync;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.resources.ResourceLocation;

@Mod("forbiddenmagic")
public final class ForbiddenMagic {
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("forbiddenmagic",path); }
    public static boolean loaded(String mod) { return ModList.get().isLoaded(mod); }
    public ForbiddenMagic(IEventBus bus) {
        LegacyConfig.load();
        var conditions=net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.CONDITION_CODECS,"forbiddenmagic");
        conditions.register("feature_enabled",()->FeatureCondition.CODEC);conditions.register(bus);
        Content.init(bus);
        Completion.init(bus);
        if(loaded("botania")) BotanyContent.init(bus);
        if(net.neoforged.fml.loading.FMLEnvironment.dist==net.neoforged.api.distmarker.Dist.CLIENT&&loaded("botania"))BotanyClient.init(bus);
        CoreEvents.init();
        ToolEvents.init();
        ResearchMigration.init();
        AspectSync.addRegistrar(CuratedAspects::register);
        AspectSync.addRegistrar(Content::aspects);
        AspectSync.addRegistrar(ClassicAspects::register);
        bus.addListener((FMLCommonSetupEvent event)->event.enqueueWork(()->{
            LegacyWands.init(); PortRecipes.init();
            Completion.setup();
            if(loaded("bloodmagic")) BloodCompat.setup();
        }));
    }
}
