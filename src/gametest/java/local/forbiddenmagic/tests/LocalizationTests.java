package local.forbiddenmagic.tests;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;
import local.forbiddenmagic.*;
import thaumcraft.research.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class LocalizationTests {
    static JsonObject read(String name)throws Exception{try(var in=PortRecipes.class.getResourceAsStream(name)){return JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(in,name),StandardCharsets.UTF_8)).getAsJsonObject();}}
    static boolean enabled(JsonObject r){if(r.has("mods"))for(var mod:r.getAsJsonArray("mods"))if(!ForbiddenMagic.loaded(mod.getAsString()))return false;return true;}
    @GameTest(batch="localization",template="test/platform",timeoutTicks=400)
    public static void originalBookText(GameTestHelper h)throws Exception{
        var baseline=read("/forbiddenmagic/localization-0575-baseline.json");var ru=read("/assets/forbiddenmagic/lang/ru_ru.json");var en=read("/assets/forbiddenmagic/lang/en_us.json");
        check(baseline.get("original_russian").getAsInt()==131&&baseline.get("translated_russian").getAsInt()==20,"Historical Russian coverage");
        for(String k:baseline.getAsJsonObject("research").keySet()){
            check(ru.has(k)&&en.has(k),"Untranslated original key "+k);
            check(ru.get(k).getAsString().matches("(?s).*[А-Яа-яЁё].*"),"English fallback in Russian "+k);
            check(!en.get(k).getAsString().matches("(?s).*[А-Яа-яЁё].*"),"Russian fallback in English "+k);
        }
        int text=0,recipes=0,concealed=0,smelting=0;
        for(var entry:read("/forbiddenmagic/port-data.json").getAsJsonArray("research")){
            var r=entry.getAsJsonObject();if(!r.has("pages")||!enabled(r)||ResearchCategories.getResearch(r.get("key").getAsString())==null)continue;
            var actual=ResearchCategories.getResearch(r.get("key").getAsString());var expected=r.getAsJsonArray("pages").asList().stream().map(JsonElement::getAsJsonObject).filter(LocalizationTests::enabled).toList();check(actual.pages().size()==expected.size(),"Missing original pages "+actual.key());
            for(int n=0;n<expected.size();n++){
                var p=expected.get(n);var page=actual.pages().get(n);String type=p.get("type").getAsString();
                if(type.equals("text")){check(page instanceof ResearchPage.Text t&&t.text().equals(p.get("key").getAsString()),"Text page order "+actual.key());text++;}
                else if(type.equals("concealed")){check(page instanceof ResearchPage.ConcealedText c&&c.research().equals("FM_CAGE")&&c.text().equals("forbidden.research_page.FORK.wc"),"Fork conditional page");concealed++;}
                else if(type.equals("crafting")){check(page instanceof ResearchPage.Crafting,"Crafting page "+actual.key());check(h.getLevel().getRecipeManager().byKey(ResourceLocation.parse(p.get("recipe").getAsString())).isPresent(),"Missing actual crafting recipe "+p);recipes++;}
                else if(type.equals("recipe")){check(!(page instanceof ResearchPage.Text),"Recipe replaced by prose");recipes++;}
                else if(type.equals("smelting")){check(page instanceof ResearchPage.Smelting s&&!s.input().create().isEmpty()&&!s.output().create().isEmpty(),"Tree smelting page");smelting++;}
                if(p.has("key")){String k=p.get("key").getAsString();check(ru.has(k)&&en.has(k),"Missing page translation "+k);}
            }
        }
        check(concealed==1&&smelting==1,"Lost special original pages");
        check(ru.get("fm.name.FM_MORPH").getAsString().equals("Хамелеоновые инструменты")&&ru.get("enchantment.forbiddenmagic.greedy").getAsString().equals("Капиталист"),"Original names replaced");
        Files.writeString(Path.of("localization-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","original_russian_preserved",131,"new_research_translations",20,"ordered_text_pages",text,"recipe_pages",recipes,"conditional_fork_page",true,"native_tree_smelting_page",true,"russian_english_fallbacks",0)));
        h.succeed();
    }
}
