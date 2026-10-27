package local.forbiddenmagic.tests;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import local.forbiddenmagic.CuratedAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.aspect.*;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.research.*;
import thaumcraft.network.AspectTagsPayload;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class AspectTests {
    static final Set<String> MODS=Set.of("botania","bloodmagic","witchery","twilightforest");
    static Map<String,Integer> aspects(AspectList list) {
        var out=new TreeMap<String,Integer>();
        if(list!=null) for(var aspect:list.getAspects()) out.put(aspect.getTag(),list.getAmount(aspect));
        return out;
    }
    static void inventory(GameTestHelper h) throws Exception {
        var rows=new ArrayList<Object>(); var registry=AspectRegistry.active();
        for(var item:BuiltInRegistries.ITEM) {
            var id=BuiltInRegistries.ITEM.getKey(item);
            if(MODS.contains(id.getNamespace())) rows.add(Map.of("kind",item instanceof BlockItem?"block":"item","id",id.toString(),"class",item.getClass().getName(),"description",item.getDescriptionId(),"aspects",aspects(registry.getObjectTags(item))));
        }
        for(var type:BuiltInRegistries.ENTITY_TYPE) {
            var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if(MODS.contains(id.getNamespace())) {
                var entity=type.create(h.getLevel());
                if(entity instanceof LivingEntity) rows.add(Map.of("kind","entity","id",id.toString(),"class",entity.getClass().getName(),"description",type.getDescriptionId(),"aspects",aspects(registry.getEntityTags(entity))));
                if(entity!=null) entity.discard();
            }
        }
        Files.writeString(Path.of("registry-inventory.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));
    }

    static void check(boolean result, String message) {
        if(!result) throw new IllegalStateException(message);
    }
    static net.minecraft.world.item.Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElseThrow();
    }
    static Map<String,Object> snapshot(AspectRegistry registry) {
        var out=new TreeMap<String,Object>();
        registry.exportTags().forEach((item,tags)->out.put("item/"+BuiltInRegistries.ITEM.getKey(item),aspects(tags)));
        registry.exportEntities().forEach((type,tags)->out.put("entity/"+BuiltInRegistries.ENTITY_TYPE.getKey(type),tags.stream().map(x->Map.of("tags",aspects(x.aspects()),"nbt",x.conditions().toString())).toList()));
        return out;
    }
    static Map<String,Integer> validateTargets(GameTestHelper h, AspectRegistry registry) {
        int items=0, blocks=0, mobs=0;
        for(var rule:CuratedAspects.rules()) {
            if(!ModList.get().isLoaded(rule.mod())) continue;
            for(var id:rule.targets()) {
                var expected=aspects(rule.aspects());
                if(rule.entity()) {
                    var type=BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow();
                    var entity=type.create(h.getLevel());
                    check(entity instanceof LivingEntity,"Not a living creature "+id);
                    check(expected.equals(aspects(registry.getEntityTags(entity))),"Entity aspects differ "+id);
                    var target=ScanManager.resolveEntity(entity);
                    check(target!=null&&expected.equals(aspects(target.aspects())),"Entity scan differs "+id);
                    check(registry.exportEntities().get(type).size()==1,"Duplicated entity rules "+id);
                    entity.discard(); mobs++;
                } else {
                    var item=BuiltInRegistries.ITEM.getOptional(id).orElseThrow();
                    check(expected.equals(aspects(registry.getObjectTags(item))),"Item aspects differ "+id);
                    var target=ScanManager.resolveStack(new ItemStack(item));
                    check(target!=null,"Missing item scan "+id);
                    var scanned=aspects(target.aspects());
                    // Thaumcraft may add its normal tool/armor bonuses after the base composition.
                    expected.forEach((aspect,count)->check(scanned.getOrDefault(aspect,0)>=count,"Scan lost "+aspect+" "+id));
                    if(item instanceof BlockItem) blocks++; else items++;
                }
            }
        }
        return Map.of("items",items,"blocks",blocks,"living_entities",mobs);
    }

    @GameTest(template="test/platform",timeoutTicks=1200)
    public static void curatedRulesAndReload(GameTestHelper h) throws Exception {
        var level=h.getLevel(); var server=level.getServer();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
        for(var aspect:Aspect.values())check(aspect.isEnabled(),"Dormant FM aspect "+aspect);
        var proof=new LinkedHashMap<String,Object>();
        var registry=AspectRegistry.active();
        proof.put("validated",validateTargets(h,registry));

        // Core 0.575 deliberately adds sin aspects; preserve every other native aspect.
        var nativeRegistry=new AspectRegistry(server);
        var modern=new ModernAspects(nativeRegistry); modern.registerBase();
        new ConfigAspects(nativeRegistry).registerAll();
        int nativeItems=0;
        for(var entry:nativeRegistry.exportTags().entrySet()) {
            var ns=BuiltInRegistries.ITEM.getKey(entry.getKey()).getNamespace();
            if(ns.equals("minecraft")||ns.equals("thaumcraft")) {
                var expected=new TreeMap<>(aspects(entry.getValue()));var actual=new TreeMap<>(aspects(registry.getObjectTags(entry.getKey())));
                for(String sin:List.of("infernus","ira","invidia","gula","superbia","luxuria","desidia")){expected.remove(sin);actual.remove(sin);}
                check(expected.equals(actual),"Native non-sin composition changed "+entry.getKey());
                nativeItems++;
            }
        }
        proof.put("native_non_sin_compositions_preserved",nativeItems);
        check(registry.getObjectTags(net.minecraft.world.item.Items.COOKIE).getAmount(Aspect.GULA)>=1,"Core cookie sin missing");

        // The real network codec must carry all new aspect names/values and entity targets.
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),level.registryAccess());
        var payload=new AspectTagsPayload(registry.exportTags(),registry.exportEntities());
        AspectTagsPayload.STREAM_CODEC.encode(buffer,payload);
        proof.put("sync_payload_bytes",buffer.readableBytes());
        var decoded=AspectTagsPayload.STREAM_CODEC.decode(buffer); buffer.release();
        var mirror=AspectRegistry.fromSync(decoded.items(),decoded.entities());
        check(snapshot(registry).equals(snapshot(mirror)),"Aspect network round trip changed data");
        proof.put("native_network_codec_roundtrip",true);

        if(ModList.get().isLoaded("bloodmagic")) {
            var first=aspects(registry.getObjectTags(item("bloodmagic:blankslate")));
            for(var id:List.of("reinforcedslate","infusedslate","demonslate","etherealslate"))
                check(first.equals(aspects(registry.getObjectTags(item("bloodmagic:"+id)))),"Slate tier inflation "+id);
            proof.put("all_five_slates_identical",first);
        }

        var player=FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"aspect-verifier"));
        for(var aspect:Aspect.getEnabledAspects())Knowledge.get(player).discoverAspect(aspect);
        int researchScans=0,placedBlocks=0,dissolutions=0;
        for(String ns:List.of("botania","bloodmagic","witchery","twilightforest")) {
            if(!ModList.get().isLoaded(ns))continue;
            String id=switch(ns) {case "botania"->"botania:rune_wrath";case "bloodmagic"->"bloodmagic:blankslate";case "witchery"->"witchery:infernal_animus";default->"twilightforest:carminite";};
            var stack=new ItemStack(item(id)); var target=ScanManager.resolveStack(stack);
            check(target!=null&&ScanManager.completeScan(player,target)&&ScanManager.hasBeenScanned(player,target),"Research scan failed "+id);
            for(var aspect:target.aspects().getAspects())check(Knowledge.get(player).getAspectPool(aspect)>0,"Scan did not award research "+id);
            researchScans++;

            var pos=h.absolutePos(new BlockPos(5,3,5));
            var crucibleBlock=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:crucible"));
            level.setBlock(pos,crucibleBlock.defaultBlockState(),3);
            var crucible=(CrucibleBlockEntity)level.getBlockEntity(pos);
            check(crucible!=null,"No crucible block entity");crucible.fillWater(1000);crucible.setHeat(200);
            var dropped=new ItemEntity(level,pos.getX()+0.5,pos.getY()+0.8,pos.getZ()+0.5,stack.copy());
            level.addFreshEntity(dropped);crucible.attemptSmelt(dropped);
            check(dropped.isRemoved(),"Crucible did not consume "+id);
            check(aspects(target.aspects()).equals(aspects(crucible.getAspects())),"Crucible lost aspects "+id);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);dissolutions++;

            String blockId=switch(ns) {case "botania"->"botania:livingrock";case "bloodmagic"->"bloodmagic:blankrune";case "witchery"->"witchery:cauldron";default->"twilightforest:uncrafting_table";};
            var block=BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(blockId)).orElseThrow();
            level.setBlock(pos,block.defaultBlockState(),3);
            var blockTarget=ScanManager.resolveBlock(level,pos);
            check(blockTarget!=null&&aspects(registry.getObjectTags(item(blockId))).equals(aspects(blockTarget.aspects())),"Placed block scan failed "+blockId);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);placedBlocks++;
        }
        proof.put("actual_research_scans",researchScans);proof.put("actual_crucible_dissolutions",dissolutions);proof.put("placed_block_scans",placedBlocks);

        var before=snapshot(AspectRegistry.active());
        var reload=server.reloadResources(server.getPackRepository().getSelectedIds());
        h.succeedWhen(()->{
            h.assertTrue(reload.isDone(),"Waiting for real datapack reload");
            reload.join();
            check(before.equals(snapshot(AspectRegistry.active())),"Reload changed compositions/duplicated entities");
            validateTargets(h,AspectRegistry.active());
            proof.put("real_datapack_reload_preserved_all_tags",true);
            proof.put("status","PASS");
            try {inventory(h);Files.writeString(Path.of("aspect-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));}
            catch(Exception error){throw new IllegalStateException(error);}
        });
    }
}
