package local.forbiddenmagic;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.Wands;
import velrondevs.botania.api.block_entity.SpecialFlowerBlockEntity;
import velrondevs.botania.common.block.SpecialFlowerBlock;
import velrondevs.botania.common.block.FloatingSpecialFlowerBlock;
import velrondevs.botania.common.item.block.SpecialFlowerBlockItem;
import velrondevs.botania.api.mana.ManaItemHandler;

/** Loaded only when the actual Botania Revived port is present (velrondevs namespace). */
public final class BotanyContent {
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("forbiddenmagic");
    private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"forbiddenmagic");
    public static final Map<String,DeferredBlock<SpecialFlowerBlock>> FLOWERS=new LinkedHashMap<>();
    public static final Map<String,DeferredBlock<FloatingSpecialFlowerBlock>> FLOATERS=new LinkedHashMap<>();
    private static final Map<String,DeferredHolder<BlockEntityType<?>,BlockEntityType<?>>> FLOWER_TYPES=new LinkedHashMap<>();
    public static void init(IEventBus bus) {
        for(String name:List.of("euclidaisy","whisperweed","tainthistle","bloodthorn")) {
            if(name.equals("bloodthorn")&&!ForbiddenMagic.loaded("bloodmagic"))continue;
            var block=BLOCKS.register(name,()->new SpecialFlowerBlock(MobEffects.WEAKNESS,60,BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY),()->type(name)));
            FLOWERS.put(name,block);
            Content.add(name,()->new SpecialFlowerBlockItem(block.get(),new Item.Properties()));
            var floating=BLOCKS.register("floating_"+name,()->new FloatingSpecialFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY),()->type(name)));
            FLOATERS.put(name,floating);Content.add("floating_"+name,()->new SpecialFlowerBlockItem(floating.get(),new Item.Properties()));
            FLOWER_TYPES.put(name,TYPES.register(name,()->BlockEntityType.Builder.of((pos,state)->name.equals("tainthistle")?new Flowers.Tainthistle(pos,state):new Flowers.Functional(name,pos,state),block.get(),floating.get()).build(null)));
        }
        BLOCKS.register(bus);TYPES.register(bus);
    }
    @SuppressWarnings("unchecked") public static <T extends SpecialFlowerBlockEntity> BlockEntityType<T> type(String name) {
        return (BlockEntityType<T>)FLOWER_TYPES.get(name).get();
    }
    public static void recharge(ItemStack stack,Player player) {
        if(!LegacyConfig.integration("botania")||!LegacyConfig.bool("crossWand")||player.level().isClientSide()||player.tickCount%40!=0||!LegacyWands.hotbar(stack,player))return;
        String cap=Wands.getCap(stack).tag();
        int unit=Math.max(0,LegacyConfig.integer("manavis")-(cap.equals("fm_manasteel")||cap.equals("fm_elementium")?2:0));
        for(var aspect:Aspect.getPrimalAspects()) {
            int amount=Math.min(100,Wands.getMaxVis(stack)-Wands.getVis(stack,aspect));
            if(amount>0&&(unit==0||ManaItemHandler.instance().requestManaExact(stack,player,unit*amount,true)))Wands.addRealVis(stack,aspect,amount,true);
        }
    }
}
