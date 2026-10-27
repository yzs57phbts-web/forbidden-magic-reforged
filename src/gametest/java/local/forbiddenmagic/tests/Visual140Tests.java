package local.forbiddenmagic.tests;
import local.forbiddenmagic.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
import java.util.*;
import static local.forbiddenmagic.tests.PortTests.check;

@GameTestHolder("forbiddenmagic_140tests") @PrefixGameTestTemplate(false)
public final class Visual140Tests {
    @GameTest(batch="visual140",template="test/platform",timeoutTicks=200)
    public static void ironAxeFork(GameTestHelper h)throws Exception{
        var level=h.getLevel();var p=HeadlessPlayer.create(level,"fork140");level.addNewPlayer(p);var at=h.absolutePos(new BlockPos(3,3,3));p.moveTo(at.getX(),at.getY(),at.getZ(),0,0);
        var iron=new ItemStack(Items.IRON_AXE);p.setItemInHand(InteractionHand.MAIN_HAND,iron);p.doTick();double damage=p.getAttributeValue(Attributes.ATTACK_DAMAGE),speed=p.getAttributeValue(Attributes.ATTACK_SPEED);
        var fork=Content.stack("diabolists_fork");p.setItemInHand(InteractionHand.MAIN_HAND,fork);p.doTick();check(p.getAttributeValue(Attributes.ATTACK_DAMAGE)==damage&&p.getAttributeValue(Attributes.ATTACK_SPEED)==speed,"Fork attributes differ from actual iron axe");
        check(fork.getMaxDamage()==iron.getMaxDamage()&&fork.getEnchantmentValue()==iron.getEnchantmentValue(),"Fork durability/enchantability not iron axe");check(fork.getItem() instanceof AxeItem&&!(fork.getItem() instanceof TridentItem),"Fork inherited throwable trident mechanics");
        check(fork.getItem().use(level,p,InteractionHand.MAIN_HAND).getResult()==InteractionResult.PASS&&!p.isUsingItem()&&fork.getUseDuration(p)==0,"Fork starts trident use");
        check(fork.getItem().isValidRepairItem(fork,new ItemStack(Items.IRON_INGOT))&&!fork.getItem().isValidRepairItem(fork,new ItemStack(Items.QUARTZ)),"Fork repair not iron");
        check(LegacyMechanics.wrathWeaponBonus(fork)==6,"Modern fork lost its original weapon route for Wrath");
        for(String id:List.of("profaned_rod","neutronium_rod","neutronium_staff_core","orichalcum_cap"))check(!LegacyConfig.itemVisible(id),"Invented component in creative tab: "+id);
        p.discard();Files.writeString(Path.of("visual-140-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","PASS","native_iron_axe_damage",damage,"native_iron_axe_speed",speed,"durability",fork.getMaxDamage(),"no_throw_or_use_charge",true,"hidden_saved_components",true)));h.succeed();
    }
}
