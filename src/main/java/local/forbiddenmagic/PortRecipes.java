package local.forbiddenmagic;

import java.util.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;

/** Actual native TC recipe/research objects; cross-mod processing recipes use the current mods' JSON codecs. */
public final class PortRecipes {
    public static final List<String> RESEARCH=new ArrayList<>(),RECIPES=new ArrayList<>();
    public static AspectList aspects(JsonObject data){
        var out=new AspectList();data.entrySet().forEach(e->{var aspect=Aspect.getAspect(e.getKey());if(aspect==null)throw new IllegalArgumentException(e.getKey());out.add(aspect,e.getValue().getAsInt());});return out;
    }
    public static TcStack stack(String id){return TcStack.of(id.contains(":")?id:"forbiddenmagic:"+id);}
    private static TcIngredient ingredient(String id){return id.startsWith("#")?TcIngredient.tag(id.substring(1)):TcIngredient.item(id.contains(":")?id:"forbiddenmagic:"+id);}
    private static boolean enabled(JsonObject data){
        if(data.has("research")&&!LegacyConfig.research(data.get("research").getAsString()))return false;
        if(data.has("key")&&!LegacyConfig.research(data.get("key").getAsString()))return false;
        if(data.has("requires")&&!LegacyConfig.research(data.get("requires").getAsString()))return false;
        if(data.has("mods"))for(var mod:data.getAsJsonArray("mods"))if(!LegacyConfig.integration(mod.getAsString()))return false;
        if(data.has("any_mods")){boolean found=false;for(var mod:data.getAsJsonArray("any_mods"))found|=LegacyConfig.integration(mod.getAsString());if(!found)return false;}return true;
    }
    private static String[] enabledResearch(String[] keys){return Arrays.stream(keys).filter(k->LegacyConfig.research(k)).toArray(String[]::new);}
    private static String[] strings(JsonObject r,String field){var values=new ArrayList<String>();if(r.has(field))r.getAsJsonArray(field).forEach(e->values.add(e.getAsString()));return values.toArray(String[]::new);}
    private static boolean flag(JsonObject r,String field){return r.has(field)&&r.get(field).getAsBoolean();}
    private static ResearchPage recipePage(String key){
        var actual=TcRecipes.get(key);
        if(actual instanceof InfusionRecipe)return ResearchPage.infusion(key);
        if(actual instanceof InfusionEnchantmentRecipe)return ResearchPage.infusionEnchantment(key);
        if(actual instanceof CrucibleRecipe)return ResearchPage.crucible(key);
        if(actual instanceof ArcaneRecipe)return ResearchPage.arcane(key);
        throw new IllegalStateException("Missing research recipe "+key);
    }
    private static ResearchPage page(JsonObject data){
        return switch(data.get("type").getAsString()){
            case "text"->{String key=data.get("key").getAsString();if(LegacyConfig.bool("noLust")&&key.equals("forbidden.research_page.NETHERSHARDS.2"))key="forbidden.research_page.NETHERSHARDS.2b";if(LegacyConfig.integer("researchDifficulty")==-1&&key.equals("forbidden.research_page.CRYSTALWELL.1"))key="forbidden.research_page.CRYSTALWELL.1b";yield ResearchPage.text(key);}
            case "concealed"->ResearchPage.concealed(data.get("requires").getAsString(),data.get("key").getAsString());
            case "recipe"->recipePage(data.get("recipe").getAsString());
            case "crafting"->ResearchPage.crafting(data.get("recipe").getAsString());
            case "smelting"->ResearchPage.smelting(stack(data.get("input").getAsString()),stack(data.get("output").getAsString()));
            default->throw new IllegalArgumentException("Unknown book page "+data);
        };
    }
    public static void init(){
        try(var reader=new InputStreamReader(Objects.requireNonNull(PortRecipes.class.getResourceAsStream("/forbiddenmagic/port-data.json")),StandardCharsets.UTF_8)){
            var data=JsonParser.parseReader(reader).getAsJsonObject();
            for(var element:data.getAsJsonArray("recipes")){
                var r=element.getAsJsonObject();if(!enabled(r))continue;
                if(LegacyConfig.integer("gluttony")==2){
                    r=r.deepCopy();String id=r.get("key").getAsString();
                    if(id.equals("FM:CAKE")){r.getAsJsonArray("inputs").set(1,new JsonPrimitive("forbiddenmagic:nether_star_block"));r.getAsJsonArray("inputs").set(2,new JsonPrimitive("forbiddenmagic:nether_star_block"));}
                    if(id.equals("FM:RING"))r.getAsJsonObject("symbols").addProperty("D","forbiddenmagic:nether_star_block");
                }
                String key=r.get("key").getAsString(),research=r.get("research").getAsString(),type=r.get("type").getAsString();
                var result=TcStack.of(r.get("output").getAsString(),r.has("count")?r.get("count").getAsInt():1);var a=aspects(r.getAsJsonObject("aspects"));
                var parts=new ArrayList<TcIngredient>();if(r.has("inputs"))for(var item:r.getAsJsonArray("inputs")){
                    var ingredient=ingredient(item.getAsString());
                    if(key.equals("FM:ELDRITCHORB")&&item.getAsString().equals("thaumcraft:primordial_pearl"))ingredient=new TcIngredient.Reusable(ingredient);
                    parts.add(ingredient);
                }
                switch(type){
                    case "enchantment"->TcRecipes.register(new InfusionEnchantmentRecipe(key,research,ForbiddenMagic.id(r.get("enchantment").getAsString()),r.get("instability").getAsInt(),a,parts));
                    case "infusion"->TcRecipes.register(new InfusionRecipe(key,research,result,r.get("instability").getAsInt(),a,ingredient(r.get("center").getAsString()),parts));
                    case "shapeless"->TcRecipes.register(new ShapelessArcaneRecipe(key,research,result,a,parts));
                    case "shaped"->{
                        var grid=new ArrayList<TcIngredient>();int width=r.getAsJsonArray("pattern").get(0).getAsString().length();
                        for(var row:r.getAsJsonArray("pattern"))for(char c:row.getAsString().toCharArray())grid.add(c==' '?TcIngredient.EMPTY:ingredient(r.getAsJsonObject("symbols").get(String.valueOf(c)).getAsString()));
                        TcRecipes.register(new ShapedArcaneRecipe(key,research,result,a,width,r.getAsJsonArray("pattern").size(),grid));
                    }
                    case "crucible"->TcRecipes.register(new CrucibleRecipe(key,research,result,ingredient(r.get("center").getAsString()),a));
                    default->throw new IllegalArgumentException(type);
                }
                RECIPES.add(key);
            }
            TcRecipes.register(new CraftingRecipeRef("forbiddenmagic:black_dye","FM_ROSES",ForbiddenMagic.id("black_dye"),TcStack.of("minecraft:black_dye",2)));
            ResearchCategories.registerCategory("FORBIDDEN",ForbiddenMagic.id("textures/misc/forbidden.png"),ForbiddenMagic.id("textures/misc/runecircle.png"));
            for(var element:data.getAsJsonArray("research")){
                var r=element.getAsJsonObject();if(!enabled(r))continue;
                String key=r.get("key").getAsString();var a=aspects(r.getAsJsonObject("aspects"));int x=r.get("x").getAsInt(),y=r.get("y").getAsInt(),complexity=r.get("complexity").getAsInt();
                var entry=flag(r,"virtual")?new Entry(key):r.has("icon_texture")?new Entry(key,a,x,y,complexity,ResourceLocation.parse(r.get("icon_texture").getAsString())):new Entry(key,a,x,y,complexity,stack(r.get("icon").getAsString()));
                entry.setParents(enabledResearch(strings(r,"parents"))).setParentsHidden(enabledResearch(strings(r,"parents_hidden"))).setSiblings(enabledResearch(strings(r,"siblings")));
                if(flag(r,"auto"))entry.setAutoUnlock();if(flag(r,"round"))entry.setRound();if(flag(r,"stub"))entry.setStub();if(flag(r,"concealed"))entry.setConcealed();if(flag(r,"hidden"))entry.setHidden();if(flag(r,"lost"))entry.setLost();if(flag(r,"secondary"))entry.setSecondary();if(flag(r,"special"))entry.setSpecial();
                if(r.has("warp"))entry.setWarp(r.get("warp").getAsInt());
                if(r.has("triggers")){var triggers=new ArrayList<Aspect>();for(var t:r.getAsJsonArray("triggers"))triggers.add(Aspect.getAspect(t.getAsString()));entry.setAspectTriggers(triggers.toArray(Aspect[]::new));}
                var pages=new ArrayList<ResearchPage>();
                if(r.has("pages")){for(var value:r.getAsJsonArray("pages")){var p=value.getAsJsonObject();if(enabled(p))pages.add(page(p));}}
                else{
                    pages.add(ResearchPage.text("fm.page."+key));
                    if(r.has("recipes"))for(var value:r.getAsJsonArray("recipes"))pages.add(recipePage(value.getAsString()));
                }
                entry.setPages(pages.toArray(ResearchPage[]::new)).registerResearchItem();RESEARCH.add(key);
            }
        }catch(Exception e){throw new IllegalStateException("Forbidden Magic recipe/research load",e);}
    }
    private static final class Entry extends ResearchItem{
        Entry(String key){super(key,"FORBIDDEN");}
        Entry(String key,AspectList a,int x,int y,int difficulty,TcStack icon){super(key,"FORBIDDEN",a,x,y,difficulty,icon);}
        Entry(String key,AspectList a,int x,int y,int difficulty,ResourceLocation icon){super(key,"FORBIDDEN",a,x,y,difficulty,icon);}
        @Override public String nameKey(){return "fm.name."+key();}
        @Override public String textKey(){return (LegacyConfig.bool("tagResearch")?"fm.tagged.":"fm.tag.")+key();}
    }
}
