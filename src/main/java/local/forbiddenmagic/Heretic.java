package local.forbiddenmagic;

import java.util.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;
import thaumcraft.research.*;

public final class Heretic {
    private static final DeferredRegister<PoiType> POIS=DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE,"forbiddenmagic");
    private static final DeferredRegister<VillagerProfession> JOBS=DeferredRegister.create(Registries.VILLAGER_PROFESSION,"forbiddenmagic");
    public static final DeferredHolder<PoiType,PoiType> POI=POIS.register("heretic",()->{
        var states=new java.util.HashSet<>(Completion.block("nether_star_block").getStateDefinition().getPossibleStates());
        Completion.block("umbral_rose_bush").getStateDefinition().getPossibleStates().stream().filter(s->s.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER).forEach(states::add);
        return new PoiType(ImmutableSet.copyOf(states),1,1);
    });
    public static final DeferredHolder<VillagerProfession,VillagerProfession> JOB=JOBS.register("heretic",()->new VillagerProfession("heretic",h->h.is(POI.getKey()),h->h.is(POI.getKey()),ImmutableSet.of(),ImmutableSet.of(),net.minecraft.sounds.SoundEvents.VILLAGER_WORK_CLERIC));
    public static void init(IEventBus bus){POIS.register(bus);JOBS.register(bus);NeoForge.EVENT_BUS.addListener(Heretic::trades);}
    public static void setup(){
        for(String name:List.of("profane","profaned","neutronium"))WandRegistry.registerRod("fm_"+name,name.equals("neutronium")?9001:50,ForbiddenMagic.id(name+"_rod"),name.equals("profane")?12:1000,name.equals("profane")?Heretic::contract:name.equals("neutronium")?Heretic::creative:null,name.equals("neutronium"),false,false,ForbiddenMagic.id("textures/models/wand_rod_"+name+".png"));
        WandRegistry.registerRod("fm_neutronium",9002,ForbiddenMagic.id("neutronium_staff_core"),1000,Heretic::creative,true,true,false,ForbiddenMagic.id("textures/models/wand_rod_neutronium.png"));
        WandRegistry.registerCap("fm_orichalcum",0,List.of(),0,ForbiddenMagic.id("orichalcum_cap"),1000,null,ForbiddenMagic.id("textures/models/wand_cap_orichalcum.png"));
        if(ForbiddenMagic.loaded("botania"))WandRegistry.registerCap("fm_terrasteel",1.8F,List.of(),0,ForbiddenMagic.id("terrasteel_cap"),1,null,ForbiddenMagic.id("textures/models/wand_cap_terrasteel.png"));
    }
    public static void creative(ItemStack stack,net.minecraft.world.entity.player.Player p){if(p.level().isClientSide())return;if(p.isCreative())Wands.fill(stack);else for(var a:Aspect.getPrimalAspects())Wands.storeVis(stack,a,0);}
    public static void contract(ItemStack stack,net.minecraft.world.entity.player.Player player){
        if(!(player instanceof ServerPlayer sp)||player.tickCount%20!=0)return;
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(!data.contains("fm_contract")){data.putInt("fm_contract",25000);ResearchManager.complete(sp,"ROD_fm_profane");}
        int balance=data.getInt("fm_contract");if(balance<=0)return;
        for(var a:Aspect.getPrimalAspects()){int restore=Math.min(balance,Wands.getMaxVis(stack)-Wands.getVis(stack,a));if(restore>0){Wands.addRealVis(stack,a,restore,true);balance-=restore;if(player.getRandom().nextInt(2501)<restore)Warp.add(sp,1,WarpType.STICKY);}if(balance<=0)break;}
        data.putInt("fm_contract",balance);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        if(balance==0){var old=WandKeys.PARTS.getOrDefault(stack,WandParts.DEFAULT);WandKeys.PARTS.set(stack,new WandParts(old.cap(),"fm_profaned",old.sceptre()));Warp.add(sp,1,WarpType.STICKY);sp.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,sp.getX(),sp.getY()+1,sp.getZ(),1,0,0,0,0);}
    }
    static MerchantOffer offer(ItemStack input,ItemStack second,ItemStack result,int max){return new MerchantOffer(new ItemCost(input.getItem(),input.getCount()),second.isEmpty()?Optional.empty():Optional.of(new ItemCost(second.getItem(),second.getCount())),result,max,2,0.05F);}
    private static void trades(VillagerTradesEvent event){
        if(event.getType()!=JOB.get())return;var t=event.getTrades();
        t.get(1).add((e,r)->offer(new ItemStack(Items.EMERALD,1+r.nextInt(2)),ItemStack.EMPTY,Content.stack("greed_shard").copyWithCount(3+r.nextInt(2)),12));
        for(String s:List.of("lust","wrath","gluttony"))t.get(1).add((e,r)->offer(Content.stack(s+"_shard").copyWithCount(s.equals("lust")?2+r.nextInt(4):4+r.nextInt(6)),ItemStack.EMPTY,new ItemStack(Items.EMERALD),12));
        t.get(2).add((e,r)->offer(new ItemStack(Items.EMERALD,1+r.nextInt(3)),ItemStack.EMPTY,new ItemStack(Items.SKELETON_SKULL),12));
        t.get(2).add((e,r)->offer(new ItemStack(Items.EMERALD,4+r.nextInt(4)),ItemStack.EMPTY,new ItemStack(Items.GHAST_TEAR),12));
        t.get(2).add((e,r)->offer(new ItemStack(Items.EMERALD),ItemStack.EMPTY,new ItemStack(Items.GLOWSTONE_DUST,8+r.nextInt(9)),12));
        t.get(3).add((e,r)->{var wand=Wands.create(WandRegistry.CAP_IRON,WandRegistry.rod("fm_profane"),false);Wands.fill(wand);return offer(new ItemStack(Items.EMERALD,8+r.nextInt(5)),new ItemStack(Items.STICK),wand,1);});
        t.get(3).add((e,r)->offer(new ItemStack(Items.EMERALD,4+r.nextInt(5)),new ItemStack(Items.DIAMOND),new ItemStack(BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:focus_fire"))),1));
    }
}
