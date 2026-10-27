package local.forbiddenmagic.tests;
import java.util.*;
import local.forbiddenmagic.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.wand.*;
import thaumcraft.aspect.Aspect;
import velrondevs.botania.api.BotaniaCapabilities;
import static local.forbiddenmagic.tests.PortTests.check;
public final class LegacyBotany130Tests {
    public static Map<String,Object> configuredCosts(GameTestHelper h,ServerPlayer p){
        p.getInventory().clearContent();var tablet=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("botania:mana_tablet")));var mana=tablet.getCapability(BotaniaCapabilities.MANA_ITEM);check(mana!=null,"No native mana tablet");mana.addMana(50000);p.getInventory().setItem(10,tablet);
        var wand=Wands.create(WandRegistry.cap("fm_manasteel"),WandRegistry.rod("fm_livingwood"),false);Wands.fill(wand);int max=Wands.getMaxVis(wand);Wands.storeVis(wand,Aspect.AIR,max-35);p.getInventory().setItem(0,wand);p.tickCount=40;int before=mana.getMana();BotanyContent.recharge(wand,p);int expected=35*Math.max(0,LegacyConfig.integer("manavis")-2);check(before-mana.getMana()==expected&&Wands.getVis(wand,Aspect.AIR)==max,"Configured mana price ignored");return Map.of("manasteel_partial_35_centivis",expected);
    }
}
