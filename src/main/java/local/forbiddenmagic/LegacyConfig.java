package local.forbiddenmagic;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

/** Modern named settings for implemented 0.575 features. Registry IDs remain stable for saves. */
public final class LegacyConfig {
    public static final String FILE="forbiddenmagic-common.json";
    private static final Set<String> BLOOD_ITEMS=Set.of("blood_rod","inert_blood_rod","blood_staff_core","alchemical_cap","bloody_scribing_tools","bound_scribing_tools","eldritch_blood_orb","rapier_of_the_strix","bloodthorn","floating_bloodthorn");
    private static final Set<String> BOTANIA_ITEMS=Set.of("livingwood_rod","inert_livingwood_rod","dreamwood_rod","inert_dreamwood_rod","dreamwood_staff_core","manasteel_cap","inert_manasteel_cap","elementium_cap","inert_elementium_cap","euclidaisy","floating_euclidaisy","whisperweed","floating_whisperweed","tainthistle","floating_tainthistle","bloodthorn","floating_bloodthorn");
    private static final Map<String,Object> DEFAULTS;
    private static volatile Map<String,Object> values;
    static {
        var d=new LinkedHashMap<String,Object>();d.put("schema",1);
        d.put("noLust",false);d.put("silverfishEmeralds",true);d.put("tagResearch",true);
        d.put("wrathCage",true);d.put("wrathCost",5);d.put("wrathEff",4);d.put("wrathCrazy",false);
        d.put("enchanting",true);d.put("greedyEnch",true);d.put("emeraldTrans",true);d.put("gluttony",0);d.put("spork",false);
        d.put("crossMod",true);d.put("crossWand",true);d.put("botan",true);d.put("bloodMagic",true);d.put("twilight",true);
        d.put("bloodvis",5);d.put("manavis",8);d.put("researchDifficulty",0);
        d.put("garbageBlocks",List.of("#forbiddenmagic:consuming_trash"));
        DEFAULTS=Collections.unmodifiableMap(d);values=DEFAULTS;
    }
    public static void load(){
        Path file=FMLPaths.CONFIGDIR.get().resolve(FILE);
        try{
            Files.createDirectories(file.getParent());
            if(!Files.exists(file))Files.writeString(file,new GsonBuilder().setPrettyPrinting().create().toJson(DEFAULTS)+"\n");
            var object=JsonParser.parseString(Files.readString(file)).getAsJsonObject();var parsed=new LinkedHashMap<String,Object>();
            for(var e:DEFAULTS.entrySet()){
                String key=e.getKey();var input=object.get(key);Object def=e.getValue();
                if(input==null){parsed.put(key,def);continue;}
                if(def instanceof Boolean){if(!input.isJsonPrimitive()||!input.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException(key+" must be boolean");parsed.put(key,input.getAsBoolean());}
                else if(def instanceof Integer){
                    if(!input.isJsonPrimitive()||!input.getAsString().matches("-?\\d+"))throw new IllegalArgumentException(key+" must be integer");int n=input.getAsInt();
                    int min=key.equals("researchDifficulty")?-1:0,max=switch(key){case "schema"->1;case "wrathCost"->64;case "wrathEff"->64;case "gluttony","researchDifficulty"->2;default->10000;};
                    if(key.equals("schema")&&n!=1)throw new IllegalArgumentException("Unsupported schema "+n);
                    if(key.equals("wrathEff")&&n<1)n=4;
                    if(key.equals("gluttony")&&(n<0||n>2))n=0;
                    parsed.put(key,Math.clamp(n,min,max));
                }else{
                    var entries=new ArrayList<String>();for(var element:input.getAsJsonArray()){
                        String s=element.getAsString(),id=s.startsWith("#")?s.substring(1):s;
                        if(ResourceLocation.tryParse(id)==null)throw new IllegalArgumentException("Invalid garbageBlocks entry "+s);entries.add(s);
                    }parsed.put(key,List.copyOf(entries));
                }
            }
            if((int)parsed.get("schema")!=1)throw new IllegalArgumentException("Unsupported schema");values=Collections.unmodifiableMap(parsed);
        }catch(Exception e){throw new IllegalStateException("Invalid Forbidden Magic configuration: "+file,e);}
    }
    public static boolean bool(String key){return (boolean)values.get(key);}
    public static int integer(String key){return (int)values.get(key);}
    public static boolean integration(String mod){
        if(!bool("crossMod")||!ForbiddenMagic.loaded(mod))return false;
        return switch(mod){case "botania"->bool("botan");case "bloodmagic"->bool("bloodMagic");case "twilightforest"->bool("twilight");default->true;};
    }
    public static boolean enchantment(String name){return bool("enchanting")&&(!name.equals("greedy")||bool("greedyEnch"));}
    public static boolean feature(String name){return switch(name){
        case "conversion_wands"->bool("crossMod")&&bool("crossWand");
        case "cage"->bool("wrathCage");case "gluttony"->integer("gluttony")!=1;
        case "enchanting"->bool("enchanting");case "emerald"->bool("emeraldTrans");case "lust"->!bool("noLust");
        default->integration(name);
    };}
    public static boolean research(String key){
        if(key.startsWith("FM_ENCH_"))return enchantment(key.substring("FM_ENCH_".length()).toLowerCase(Locale.ROOT));
        if(key.equals("FM_COLLAR")&&bool("noLust"))return false;
        if((key.equals("FM_CAGE"))&&!bool("wrathCage"))return false;
        if((key.equals("FM_ARCANECAKE")||key.equals("FM_RINGFOOD"))&&integer("gluttony")==1)return false;
        if(key.equals("FM_EMERALD")&&!bool("emeraldTrans"))return false;
        if((key.startsWith("ROD_fm_blood")||key.startsWith("ROD_fm_livingwood")||key.startsWith("ROD_fm_dreamwood")||key.startsWith("CAP_fm_"))&&!feature("conversion_wands"))return false;
        return true;
    }
    public static boolean itemVisible(String id){
        if(id.equals("black_dye"))return false;
        if(Set.of("profaned_rod","neutronium_rod","neutronium_staff_core","orichalcum_cap").contains(id))return false;
        if(BLOOD_ITEMS.contains(id)&&!integration("bloodmagic"))return false;
        if(BOTANIA_ITEMS.contains(id)&&!integration("botania"))return false;
        if(!feature("conversion_wands")&&(BLOOD_ITEMS.contains(id)||BOTANIA_ITEMS.contains(id))&&(id.endsWith("_rod")||id.endsWith("_cap")||id.endsWith("_core")))return false;
        if(bool("noLust")&&(id.equals("lust_shard")||id.equals("submissive_collar")))return false;
        if(!bool("wrathCage")&&(id.equals("wrath_cage")||id.equals("soul_crystal")))return false;
        if(integer("gluttony")==1&&(id.equals("arcane_cake")||id.equals("ring_of_nutrition")))return false;
        return true;
    }
    public static boolean rodVisible(String tag){
        if(tag.equals("fm_profaned"))return false;
        if(tag.contains("blood"))return feature("conversion_wands")&&integration("bloodmagic");
        if(tag.contains("livingwood")||tag.contains("dreamwood"))return feature("conversion_wands")&&integration("botania");
        return true;
    }
    @SuppressWarnings("unchecked") public static boolean garbage(ItemStack stack){
        for(String s:(List<String>)values.get("garbageBlocks")){
            if(s.startsWith("#")){if(stack.is(TagKey.create(Registries.ITEM,ResourceLocation.parse(s.substring(1)))))return true;}
            else if(stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(s))))return true;
        }return false;
    }
}
