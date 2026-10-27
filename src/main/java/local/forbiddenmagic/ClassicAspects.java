package local.forbiddenmagic;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import thaumcraft.aspect.*;

/** FM's core sin sources; the approved external four-mod map remains separate and unchanged. */
public final class ClassicAspects {
    private static AspectList list(Object... pairs){var a=new AspectList();for(int i=0;i<pairs.length;i+=2)if(pairs[i]!=Aspect.LUXURIA||!LegacyConfig.bool("noLust"))a.add((Aspect)pairs[i],(int)pairs[i+1]);return a;}
    private static void own(AspectRegistry registry,String id,Object... pairs){registry.register(Content.get(id),list(pairs));}
    private static void add(AspectRegistry registry,String id,Aspect aspect,int amount){if(aspect==Aspect.LUXURIA&&LegacyConfig.bool("noLust"))return;var key=ResourceLocation.parse(id);if(!BuiltInRegistries.ITEM.containsKey(key))return;var item=BuiltInRegistries.ITEM.get(key);if(item==Items.AIR)return;var old=registry.getObjectTags(item);var result=old==null?new AspectList():old.copy();result.add(aspect,amount);registry.register(item,result);}
    public static void register(AspectRegistry r){
        for(String name:Content.SHARDS){if(!name.equals("taint"))add(r,"forbiddenmagic:"+name+"_shard",Aspect.INFERNUS,1);}
        own(r,"taint_shard",Aspect.TAINT,3,Aspect.CRYSTAL,1);
        own(r,"emerald_nugget",Aspect.CRYSTAL,1);own(r,"black_dye",Aspect.DARKNESS,1);
        for(String id:List.of("umbral_rose","umbral_rose_bush"))own(r,id,Aspect.PLANT,1,Aspect.LIFE,1,Aspect.SENSES,1,Aspect.DARKNESS,1);
        own(r,"riding_crop",Aspect.TREE,2,Aspect.CLOTH,2,Aspect.BEAST,1,Aspect.LUXURIA,1,Aspect.WEAPON,1);
        own(r,"tainted_fruit",Aspect.TAINT,2,Aspect.POISON,1);own(r,"hexcoal",Aspect.TAINT,2,Aspect.ENTROPY,2);
        own(r,"tainted_leaves",Aspect.TAINT,1,Aspect.PLANT,1);own(r,"tainted_log",Aspect.TAINT,1,Aspect.TREE,3);own(r,"tainted_sapling",Aspect.TAINT,1,Aspect.PLANT,1,Aspect.TREE,1);
        own(r,"tainted_planks",Aspect.TREE,1);for(String id:List.of("tainted_stone","tainted_bricks"))own(r,id,Aspect.EARTH,1);
        for(String id:List.of("morph_pickaxe","morph_axe","morph_shovel","morph_sword"))own(r,id,Aspect.METAL,3,id.endsWith("sword")?Aspect.WEAPON:Aspect.TOOL,3,Aspect.INVIDIA,2,Aspect.EXCHANGE,2);
        own(r,"taint_shovel",Aspect.METAL,3,Aspect.TOOL,3,Aspect.HEAL,2);own(r,"taint_pickaxe",Aspect.METAL,3,Aspect.MINE,3,Aspect.TAINT,2);
        own(r,"skull_axe",Aspect.METAL,3,Aspect.WEAPON,3,Aspect.IRA,2);own(r,"diabolists_fork",Aspect.METAL,3,Aspect.WEAPON,2,Aspect.SOUL,2);
        own(r,"soul_crystal",Aspect.CRYSTAL,3,Aspect.MIND,2);own(r,"wrath_cage",Aspect.METAL,8,Aspect.MECHANISM,4,Aspect.IRA,4);
        own(r,"submissive_collar",Aspect.MAGIC,4,Aspect.LUXURIA,2,Aspect.TRAP,2);own(r,"ring_of_nutrition",Aspect.METAL,2,Aspect.GULA,2,Aspect.HUNGER,2);
        own(r,"blink_focus",Aspect.ELDRITCH,3,Aspect.TRAVEL,3,Aspect.DESIDIA,2);own(r,"arcane_cake",Aspect.GULA,4,Aspect.HUNGER,4,Aspect.CRAFT,3);own(r,"nether_star_block",Aspect.INFERNUS,32,Aspect.SUPERBIA,32,Aspect.MAGIC,16);
        for(String id:List.of("netherrack","nether_wart","nether_brick"))add(r,"minecraft:"+id,Aspect.INFERNUS,1);
        add(r,"minecraft:nether_bricks",Aspect.INFERNUS,2);add(r,"minecraft:nether_quartz_ore",Aspect.INFERNUS,2);
        add(r,"minecraft:nether_star",Aspect.INFERNUS,8);add(r,"minecraft:nether_star",Aspect.SUPERBIA,8);add(r,"minecraft:ghast_tear",Aspect.IRA,4);
        add(r,"minecraft:wither_skeleton_skull",Aspect.INFERNUS,4);add(r,"minecraft:creeper_head",Aspect.IRA,2);add(r,"minecraft:tnt",Aspect.IRA,2);
        for(String id:List.of("golden_helmet","golden_chestplate","golden_leggings","golden_boots","golden_sword"))add(r,"minecraft:"+id,Aspect.SUPERBIA,id.equals("golden_helmet")||id.equals("golden_sword")?2:1);
        add(r,"minecraft:ender_eye",Aspect.INVIDIA,4);add(r,"minecraft:cake",Aspect.GULA,7);add(r,"minecraft:cookie",Aspect.GULA,1);add(r,"minecraft:blaze_powder",Aspect.IRA,1);
        add(r,"minecraft:repeater",Aspect.INVIDIA,2);add(r,"minecraft:wheat",Aspect.LUXURIA,2);add(r,"minecraft:lead",Aspect.LUXURIA,2);
        for(var item:BuiltInRegistries.ITEM)if(item instanceof net.minecraft.world.item.BlockItem b&&b.getBlock() instanceof net.minecraft.world.level.block.BedBlock)add(r,BuiltInRegistries.ITEM.getKey(item).toString(),Aspect.DESIDIA,4);
        var entities=r.exportEntities();
        Map<String,AspectList> additions=Map.ofEntries(Map.entry("minecraft:creeper",list(Aspect.IRA,2)),Map.entry("minecraft:wither",list(Aspect.INFERNUS,7,Aspect.IRA,7)),Map.entry("minecraft:wither_skeleton",list(Aspect.INFERNUS,3)),Map.entry("minecraft:ocelot",list(Aspect.DESIDIA,3)),Map.entry("minecraft:enderman",list(Aspect.SUPERBIA,4,Aspect.INVIDIA,4)),Map.entry("minecraft:ghast",list(Aspect.INFERNUS,3,Aspect.IRA,3)),Map.entry("minecraft:zombified_piglin",list(Aspect.IRA,6)),Map.entry("minecraft:ender_dragon",list(Aspect.SUPERBIA,10)),Map.entry("minecraft:pig",list(Aspect.GULA,3)),Map.entry("minecraft:magma_cube",list(Aspect.INFERNUS,1)),Map.entry("thaumcraft:brainy_zombie",list(Aspect.IRA,3)),Map.entry("thaumcraft:giant_brainy_zombie",list(Aspect.IRA,4)),Map.entry("thaumcraft:firebat",list(Aspect.IRA,1,Aspect.INFERNUS,2)),Map.entry("thaumcraft:taintacle",list(Aspect.LUXURIA,3)));
        additions.forEach((id,extra)->{var key=ResourceLocation.parse(id);if(!BuiltInRegistries.ENTITY_TYPE.containsKey(key))return;var type=BuiltInRegistries.ENTITY_TYPE.get(key);var old=entities.get(type);if(old==null)return;for(var tag:old){var a=tag.aspects().copy();for(var aspect:extra.getAspects())a.add(aspect,extra.getAmount(aspect));if(id.equals("minecraft:creeper")&&!tag.conditions().isEmpty())a.add(Aspect.IRA,2);var pairs=new ArrayList<Object>();for(var c:tag.conditions()){pairs.add(c.name());pairs.add(c.value());}r.registerEntity(type,a,pairs.toArray());}});
    }
}
