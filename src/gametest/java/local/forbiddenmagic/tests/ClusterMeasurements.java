package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import local.forbiddenmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.device.*;
import static local.forbiddenmagic.tests.PortTests.check;

/** Native loot + production FM BlockDrops handler; unchanged cluster balance. */
@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class ClusterMeasurements {
    private static final int DRAWS=2000;
    private static final long[] SEEDS={5489,20419,867,1201,70717};
    private record Trial(Block block,String mode,int level,long seed){}
    @GameTest(batch="measurements",template="test/platform",timeoutTicks=2400)
    public static void nativeOreYields(GameTestHelper h){
        if(!Boolean.getBoolean("forbiddenmagic.measurements")){h.succeed();return;}
        var l=h.getLevel();var p=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"cluster-measure"));
        var pos=h.absolutePos(new BlockPos(300,8,0));l.getChunkAt(pos);p.moveTo(pos.getX(),pos.getY()+3,pos.getZ(),0,0);
        var trials=new ArrayList<Trial>();
        for(var block:List.of(Blocks.IRON_ORE,Blocks.DEEPSLATE_IRON_ORE,Blocks.GOLD_ORE,Blocks.DEEPSLATE_GOLD_ORE,Blocks.COPPER_ORE,Blocks.DEEPSLATE_COPPER_ORE,thaumcraft.registry.ModBlocks.CINNABAR_ORE.get(),thaumcraft.registry.ModBlocks.DEEPSLATE_CINNABAR_ORE.get()))
            for(int kind=0;kind<8;kind++)for(long seed:SEEDS)trials.add(new Trial(block,kind<4?"fortune":"cluster",kind<4?kind:kind-3,seed));
        var rows=new ArrayList<Map<String,Object>>();var cooking=new HashMap<String,Integer>();var index=new int[]{0};
        h.onEachTick(()->{
            if(index[0]<trials.size()){
                var t=trials.get(index[0]++);var tool=Content.stack("morph_pickaxe");
                if(t.level>0)tool.enchant(l.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(t.mode.equals("cluster")?net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id("cluster")):Enchantments.FORTUNE),t.level);
                p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,tool);p.getRandom().setSeed(t.seed^0x9E3779B97F4A7C15L);var random=RandomSource.create(t.seed);
                var state=t.block.defaultBlockState();var table=l.getServer().reloadableRegistries().getLootTable(t.block.getLootTable());
                var params=new LootParams.Builder(l).withParameter(LootContextParams.BLOCK_STATE,state).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL,tool).withParameter(LootContextParams.THIS_ENTITY,p).create(LootContextParamSets.BLOCK);
                var items=new TreeMap<String,Long>();double metal=0,squares=0;long nativeCount=0;
                for(int draw=0;draw<DRAWS;draw++){
                    var drops=new ArrayList<ItemEntity>();for(var s:table.getRandomItems(params,random))drops.add(new ItemEntity(l,pos.getX(),pos.getY(),pos.getZ(),s));
                    ToolEvents.blocks(new BlockDropsEvent(l,pos,state,null,drops,p,tool));double result=0;
                    for(var d:drops){var s=d.getItem();String id=BuiltInRegistries.ITEM.getKey(s.getItem()).toString();items.merge(id,(long)s.getCount(),Long::sum);nativeCount+=s.getCount();
                        int yield=cooking.computeIfAbsent(id,key->{var input=new SingleRecipeInput(s.copyWithCount(1));return l.getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,l).map(r->r.value().assemble(input,l.registryAccess()).getCount()).orElseThrow(()->new IllegalStateException("No smelting recipe "+key));});result+=yield*s.getCount();}
                    metal+=result;squares+=result*result;
                }
                var row=new LinkedHashMap<String,Object>();row.put("cluster_rng_seed",t.seed^0x9E3779B97F4A7C15L);row.putAll(Map.of("ore",BuiltInRegistries.BLOCK.getKey(t.block).toString(),"mode",t.mode,"level",t.level,"seed",t.seed,"draws",DRAWS,"items",items,"native_item_count",nativeCount,"smelted_metal",metal,"sum_metal_squared",squares));
                if(t.seed==SEEDS[0]){var actual=new TreeMap<String,Long>();for(int n=0;n<32;n++){l.setBlockAndUpdate(pos,state);var emitted=DropCapture.take(l,pos,()->check(p.gameMode.destroyBlock(pos),"Native ore harvest failed"));check(!emitted.isEmpty()||!tool.isCorrectToolForDrops(state),"Correct-tool native harvest produced no resources "+t.block);for(var s:emitted)actual.merge(BuiltInRegistries.ITEM.getKey(s.getItem()).toString(),(long)s.getCount(),Long::sum);}row.put("native_harvest_32_breaks",actual);row.put("native_harvest_correct_tool",tool.isCorrectToolForDrops(state));row.put("vanilla_netherite_correct_tool",new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(state));}
                rows.add(row);
            }else{
                try{var proof=new LinkedHashMap<String,Object>();proof.put("status","PASS");proof.put("method","Native seeded loot tables, production ToolEvents.blocks, actual RecipeManager smelting outputs; trials split across ticks. No cluster balance changes.");proof.put("trials",rows);proof.put("draws",rows.size()*DRAWS);proof.put("mods",net.neoforged.fml.ModList.get().getMods().stream().map(m->m.getModId()+":"+m.getVersion()).toList());Files.writeString(Path.of("cluster-measurements.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));h.succeed();}catch(Exception e){throw new RuntimeException(e);}
            }
        });
    }
    @GameTest(batch="furnace_measurements",template="test/platform",timeoutTicks=800)
    public static void infernalOutput(GameTestHelper h){
        if(!Boolean.getBoolean("forbiddenmagic.measurements")){h.succeed();return;}
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(300,8,40));l.getChunkAt(pos);
        var state=thaumcraft.registry.ModBlocks.INFERNAL_FURNACE.get().defaultBlockState();l.setBlockAndUpdate(pos,state);var furnace=(InfernalFurnaceBlockEntity)l.getBlockEntity(pos);
        var inputs=List.of(Items.RAW_IRON,Items.RAW_GOLD,Items.RAW_COPPER,BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:raw_cinnabar")),BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:native_iron_cluster")),BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:native_gold_cluster")),BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:native_copper_cluster")),BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:native_cinnabar_cluster")));
        var rows=new ArrayList<Map<String,Object>>();var index=new int[]{0};
        h.onEachTick(()->{
            if(index[0]<inputs.size()*2){
                int step=index[0]++,bellows=step<inputs.size()?0:3;var item=inputs.get(step%inputs.size());
                for(var dir:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST)){
                    var at=pos.relative(dir,2);l.setBlockAndUpdate(at,bellows==0?Blocks.AIR.defaultBlockState():thaumcraft.registry.ModBlocks.ARCANE_BELLOWS.get().defaultBlockState().setValue(thaumcraft.alchemy.BellowsBlock.FACING,dir.getOpposite()));}
                var source=new ItemStack(item);var input=new SingleRecipeInput(source);var cooked=l.getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,l).orElseThrow().value().assemble(input,l.registryAccess());
                var outputs=new TreeMap<String,Long>();l.random.setSeed(1717+step);
                for(int n=0;n<1000;n++){
                    for(var s:DropCapture.take(l,pos,()->furnace.ejectItem(l,cooked.copy(),source.copy())))outputs.merge(BuiltInRegistries.ITEM.getKey(s.getItem()).toString(),(long)s.getCount(),Long::sum);
                    l.getEntitiesOfClass(ExperienceOrb.class,new AABB(pos).inflate(5)).forEach(Entity::discard);
                }
                check(outputs.getOrDefault(BuiltInRegistries.ITEM.getKey(cooked.getItem()).toString(),0L)==1000L*cooked.getCount(),"Furnace main output was not fully captured");
                try{var method=InfernalFurnaceBlockEntity.class.getDeclaredMethod("getBellows");method.setAccessible(true);check((int)method.invoke(furnace)==bellows,"Physical bellows not recognized");}catch(ReflectiveOperationException e){throw new RuntimeException(e);}
                rows.add(Map.of("input",BuiltInRegistries.ITEM.getKey(item).toString(),"bellows",bellows,"operations",1000,"smelting_output_count",cooked.getCount(),"outputs",outputs));
            }else try{Files.writeString(Path.of("infernal-measurements.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","method","Actual InfernalFurnaceBlockEntity.ejectItem; synchronous EntityJoinLevel drop-emission capture, native smelting results, verified physical 0/3 bellows. Main outputs checked exactly. One source item per operation; emitted drops intercepted before entering the world.","trials",rows)));l.removeBlock(pos,false);h.succeed();}catch(Exception e){throw new RuntimeException(e);}
        });
    }
}
