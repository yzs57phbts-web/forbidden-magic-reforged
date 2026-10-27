package local.forbiddenmagic;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import thaumcraft.aspect.Aspect;

/** Only explicit species, no optional-mod classes and no copying of captured entity data. */
public final class CageRules {
    public enum Habitat { LAND, AIR, WATER, AMPHIBIOUS, STRIDER }
    private static final Map<ResourceLocation,Habitat> HABITATS=new LinkedHashMap<>();
    private static final Set<ResourceLocation> EXCLUDED=new HashSet<>();
    private static final Set<ResourceLocation> EXPLICIT=new HashSet<>();
    public static void load(){
        WrathCage.SOULS.clear();HABITATS.clear();EXCLUDED.clear();EXPLICIT.clear();
        try(var reader=new InputStreamReader(Objects.requireNonNull(CageRules.class.getResourceAsStream("/forbiddenmagic/cage-souls.json")),StandardCharsets.UTF_8)){
            var data=JsonParser.parseReader(reader).getAsJsonObject();
            for(var e:data.getAsJsonObject("excluded").entrySet())for(var name:e.getValue().getAsJsonArray())EXCLUDED.add(ResourceLocation.fromNamespaceAndPath(e.getKey(),name.getAsString()));
            for(var element:data.getAsJsonArray("souls")){
                var row=element.getAsJsonObject();var id=ResourceLocation.parse(row.get("entity").getAsString());
                if(!BuiltInRegistries.ENTITY_TYPE.containsKey(id))continue;
                if(id.getNamespace().equals("twilightforest")&&!LegacyConfig.integration("twilightforest"))continue;
                if(EXCLUDED.contains(id))throw new IllegalStateException("Excluded cage soul "+id);
                var aspect=Aspect.getAspect(row.get("aspect").getAsString());if(aspect==null)throw new IllegalStateException("Unknown soul aspect "+row);
                WrathCage.SOULS.put(id,aspect);EXPLICIT.add(id);HABITATS.put(id,Habitat.valueOf(row.get("habitat").getAsString().toUpperCase(Locale.ROOT)));
            }
        }catch(Exception e){throw new IllegalStateException("Cage soul definitions",e);}
    }
    public static Habitat habitat(EntityType<?> type){return HABITATS.getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(type),Habitat.LAND);}
    public static String explain(ServerLevel level,Mob mob){var below=mob.blockPosition().below();return habitat(mob.getType())+" box="+mob.getBoundingBox()+" collisionFree="+level.noCollision(mob)+" liquid="+level.containsAnyLiquid(mob.getBoundingBox())+" below="+level.getBlockState(below)+" support="+level.getBlockState(below).isFaceSturdy(level,below,Direction.UP);}
    public static boolean allowed(EntityType<?> type){
        var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return LegacyConfig.bool("wrathCage")&&(EXPLICIT.contains(id)||LegacyConfig.bool("wrathCrazy")&&type.getCategory()!=MobCategory.MISC)&&!EXCLUDED.contains(id)&&!type.is(TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("c:bosses")))&&!type.is(TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("twilightforest:bosses")))&&(!id.getNamespace().equals("twilightforest")||LegacyConfig.integration("twilightforest"));
    }
    public static boolean ensureSoul(net.minecraft.world.level.Level level,EntityType<?> type){
        if(!allowed(type))return false;var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);if(WrathCage.SOULS.containsKey(id))return true;
        if(!(type.create(level) instanceof Mob mob))return false;
        Habitat habitat=mob instanceof net.minecraft.world.entity.animal.WaterAnimal?Habitat.WATER:mob instanceof FlyingMob||type.getCategory()==MobCategory.AMBIENT?Habitat.AIR:Habitat.LAND;
        WrathCage.SOULS.put(id,Aspect.GREED);HABITATS.put(id,habitat);return true;
    }
    public static boolean position(ServerLevel level,Mob entity){
        var box=entity.getBoundingBox();
        for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX-1e-7,box.maxY-1e-7,box.maxZ-1e-7)))
            if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos))return false;
        if(!level.getWorldBorder().isWithinBounds(box)||!level.noCollision(entity))return false;
        boolean liquid=level.containsAnyLiquid(box),water=true;
        if(liquid)for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX-1e-7,box.maxY-1e-7,box.maxZ-1e-7)))
            if(!level.getFluidState(pos).is(FluidTags.WATER)){water=false;break;}
        var below=entity.blockPosition().below();boolean support=level.getBlockState(below).isFaceSturdy(level,below,Direction.UP);
        return switch(habitat(entity.getType())){
            case WATER->liquid&&water;
            case AMPHIBIOUS->liquid?water:support;
            case AIR->!liquid;
            case LAND->!liquid&&support;
            case STRIDER->!liquid&&(support||level.getFluidState(below).is(FluidTags.LAVA));
        };
    }
}
