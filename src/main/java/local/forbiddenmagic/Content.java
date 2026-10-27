package local.forbiddenmagic;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;

public final class Content {
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("forbiddenmagic");
    public static final Map<String,Supplier<? extends Item>> ALL=new LinkedHashMap<>();
    private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"forbiddenmagic");
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"forbiddenmagic");
    public static final DeferredHolder<MobEffect,MobEffect> BLOOD_SEAL=EFFECTS.register("blood_seal",()->new MobEffect(MobEffectCategory.HARMFUL,11278617){});
    public static final String[] SHARDS={"wrath","envy","taint","pride","lust","sloth","greed","gluttony"};
    public static Supplier<? extends Item> add(String id,Supplier<? extends Item> factory) {
        var item=ITEMS.register(id,factory); ALL.put(id,item); return item;
    }
    public static Supplier<? extends Item> simple(String id) { return add(id,()->new Item(new Item.Properties())); }
    public static Item get(String id) { return ALL.get(id).get(); }
    public static ItemStack stack(String id) { return new ItemStack(get(id)); }
    public static ItemStack wandExample(WandRod rod){
        String cap=rod.tag().contains("neutronium")?"fm_orichalcum":rod.tag().startsWith("fm_profane")?"iron":rod.tag().contains("blood")?"fm_alchemical":rod.tag().contains("wood")?"fm_elementium":"thaumium";
        var actual=WandRegistry.findCap(cap);if(actual==null)actual=WandRegistry.CAP_THAUMIUM;
        var wand=Wands.create(actual,rod,false);Wands.fill(wand);return wand;
    }
    public static void init(IEventBus bus) {
        for(String name:SHARDS)if(name.equals("gluttony"))add(name+"_shard",()->new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));else simple(name+"_shard");
        simple("tainted_blood_shard");
        simple("infernal_rod"); simple("tainted_rod");
        add("crystal_scribing_tools",()->new Scribes(Scribes.Kind.CRYSTAL));
        add("primal_scribing_tools",()->new Scribes(Scribes.Kind.PRIMAL));
        add("riding_crop",CoreEvents.Crop::new);
        if(ForbiddenMagic.loaded("botania")) {
            for(String id:List.of("livingwood_rod","inert_livingwood_rod","dreamwood_rod","inert_dreamwood_rod","dreamwood_staff_core","manasteel_cap","inert_manasteel_cap","elementium_cap","inert_elementium_cap")) simple(id);
        }
        if(ForbiddenMagic.loaded("bloodmagic")) {
            for(String id:List.of("blood_rod","inert_blood_rod","blood_staff_core","alchemical_cap")) simple(id);
            add("bloody_scribing_tools",()->new Scribes(Scribes.Kind.BLOODY));
            add("bound_scribing_tools",()->new Scribes(Scribes.Kind.BOUND));
            add("eldritch_blood_orb",BloodCompat::orbItem);
            add("rapier_of_the_strix",BloodCompat::rapier);
        }
        TABS.register("apocrypha",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.forbiddenmagic"))
            .icon(()->stack("wrath_shard")).displayItems((parameters,out)->{
                ALL.forEach((id,i)->{if(LegacyConfig.itemVisible(id))out.accept(i.get());});
                for(var rod:WandRegistry.rods())if(rod.tag().startsWith("fm_")&&LegacyConfig.rodVisible(rod.tag())) {
                    out.accept(wandExample(rod));
                }
            }).build());
        TABS.register("mob_crystals",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.forbiddenmagic.souls")).icon(()->WrathCage.crystal("minecraft:pig")).displayItems((parameters,out)->{if(LegacyConfig.bool("wrathCage"))WrathCage.soulIds().forEach(id->out.accept(WrathCage.crystal(id.toString())));}).build());
        ITEMS.register(bus);TABS.register(bus);EFFECTS.register(bus);
    }
    public static void aspects(AspectRegistry registry) {
        Aspect[] as={Aspect.IRA,Aspect.INVIDIA,Aspect.TAINT,Aspect.SUPERBIA,Aspect.LUXURIA,Aspect.DESIDIA,Aspect.GREED,Aspect.GULA};
        for(int n=0;n<SHARDS.length;n++){
            var tags=new AspectList().add(Aspect.CRYSTAL,1);if(as[n]!=Aspect.LUXURIA||!LegacyConfig.bool("noLust"))tags.add(as[n],2);
            registry.register(get(SHARDS[n]+"_shard"),tags);
        }
        for(var entry:ALL.entrySet()) {
            String id=entry.getKey();if(id.endsWith("_shard"))continue;
            AspectList a=new AspectList();
            if(id.contains("scribing"))a.add(Aspect.MIND,2).add(Aspect.TOOL,1).add(Aspect.MAGIC,1);
            else if(id.contains("blood_orb"))a.add(Aspect.CRYSTAL,4).add(Aspect.LIFE,4).add(Aspect.ELDRITCH,2);
            else if(id.contains("rapier"))a.add(Aspect.WEAPON,2).add(Aspect.HUNGER,2).add(Aspect.METAL,2);
            else if(id.contains("_cap"))a.add(Aspect.METAL,3).add(Aspect.MAGIC,2);
            else if(id.contains("rod")||id.contains("staff"))a.add(Aspect.TREE,3).add(Aspect.MAGIC,2);
            else if(id.equals("euclidaisy"))a.add(Aspect.PLANT,2).add(Aspect.AURA,1);
            else if(id.equals("whisperweed"))a.add(Aspect.PLANT,2).add(Aspect.MIND,1);
            else if(id.equals("tainthistle"))a.add(Aspect.PLANT,2).add(Aspect.TAINT,1);
            else if(id.equals("bloodthorn"))a.add(Aspect.PLANT,2).add(Aspect.LIFE,1);
            if(!a.isEmpty())registry.register(entry.getValue().get(),a);
        }
        if(ALL.containsKey("tainted_blood_shard"))registry.register(get("tainted_blood_shard"),new AspectList().add(Aspect.LIFE,2).add(Aspect.TAINT,2));
    }
}
