package local.forbiddenmagic.tests;

import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import local.forbiddenmagic.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import thaumcraft.crafting.*;
import thaumcraft.research.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_tests") @PrefixGameTestTemplate(false)
public final class Recipe121Tests {
    @GameTest(batch="recipe121",template="test/platform",timeoutTicks=300)
    public static void exactImportedRecipes(GameTestHelper h)throws Exception{
        var p=HeadlessPlayer.create(h.getLevel(),"recipes121");var rows=new ArrayList<Map<String,Object>>();
        try(var reader=new InputStreamReader(Objects.requireNonNull(Recipe121Tests.class.getResourceAsStream("/forbiddenmagic_tests/recipe-imports-121.json")),StandardCharsets.UTF_8)){
            for(var value:JsonParser.parseReader(reader).getAsJsonArray()){
                var data=value.getAsJsonObject();String key=data.get("key").getAsString();var recipe=(InfusionRecipe)TcRecipes.get(key);check(recipe!=null,"Missing imported recipe "+key);
                Knowledge.get(p).addResearch(recipe.research());var expected=new ArrayList<String>();for(var slot:data.getAsJsonArray("inputs"))expected.add(slot.getAsJsonObject().getAsJsonObject("stack").get("item").getAsString());
                var actual=recipe.components().stream().map(i->BuiltInRegistries.ITEM.getKey(i.displayStacks().getFirst().getItem()).toString()).toList();check(actual.equals(expected),"Imported ingredient count/order changed "+key);
                check(recipe.instability()==data.get("instability").getAsInt()&&recipe.aspects().equals(PortRecipes.aspects(data.getAsJsonObject("aspects"))),"Imported costs changed "+key);
                check(BuiltInRegistries.ITEM.getKey(recipe.central().displayStacks().getFirst().getItem()).toString().equals(data.getAsJsonObject("central").getAsJsonObject("stack").get("item").getAsString()),"Central changed "+key);
                var plan=InfusionCrafting.findRecipe(recipe.components().stream().map(i->i.displayStacks().getFirst()).toList(),recipe.central().displayStacks().getFirst(),p);check(plan!=null&&plan.key().equals(key),"Native infusion matcher rejected "+key);
                var positions=InfusionDiagram.coordinates(key,expected.size());check(positions.size()==expected.size(),"Diagram count changed "+key);var unique=new HashSet<String>();
                for(var point:positions){check(unique.add(Arrays.toString(point)),"Overlapping diagram points "+key);double radius=Math.hypot(point[0],point[1]);check(radius>=39&&radius<=41,"Ingredient outside common ring "+key);}
                rows.add(Map.of("key",key,"inputs",actual,"positions",positions,"native_match",true,"instability",recipe.instability()));
            }
        }
        check(rows.size()==4,"Import coverage");var focus=Content.stack("blink_focus");var item=(BlinkFocus)focus.getItem();check(item.getFocusColor(focus)==9474192,"Original Blink colour differs");
        check(item.getFocusTexture(focus).toString().equals("forbiddenmagic:item/blink_focus")&&item.getOrnamentTexture(focus).toString().equals("forbiddenmagic:item/blink_focus_ornament"),"Mounted focus not using atlas sprite IDs");
        Files.writeString(Path.of("recipe-121-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","recipes",rows,"original_blink_colour",9474192,"sprite_contract_ids",true)));p.discard();h.succeed();
    }
}
