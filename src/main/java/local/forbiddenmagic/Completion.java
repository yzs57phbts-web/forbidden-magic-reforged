package local.forbiddenmagic;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class Completion {
    public static MobEffectInstance uncurable(net.minecraft.core.Holder<MobEffect> effect,int ticks){var instance=new MobEffectInstance(effect,ticks);instance.getCures().clear();return instance;}
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("forbiddenmagic");
    public static final Map<String,Supplier<? extends Block>> ALL_BLOCKS=new LinkedHashMap<>();
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"forbiddenmagic");
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<WrathCage.Cage>> CAGE_ENTITY=ENTITIES.register("wrath_cage",()->BlockEntityType.Builder.of(WrathCage.Cage::new,block("wrath_cage")).build(null));
    public static Block block(String id){return ALL_BLOCKS.get(id).get();}
    static void add(String id,Supplier<? extends Block> factory){var b=BLOCKS.register(id,factory);ALL_BLOCKS.put(id,b);Content.add(id,()->id.equals("wrath_cage")||id.equals("tainted_sapling")?new WarpedBlockItem(b.get(),id.equals("wrath_cage")?5:1):new BlockItem(b.get(),new Item.Properties()));}
    static final class WarpedBlockItem extends BlockItem implements thaumcraft.item.gear.WarpingGear {
        private final int warp;WarpedBlockItem(Block b,int warp){super(b,new Item.Properties());this.warp=warp;}
        @Override public int getWarp(ItemStack s,net.minecraft.world.entity.player.Player p){return warp;}
    }
    public static void init(IEventBus bus){
        Content.add("morph_pickaxe",DarkTools.Pick::new);Content.add("morph_axe",DarkTools.Axe::new);Content.add("morph_shovel",DarkTools.Shovel::new);Content.add("morph_sword",DarkTools.Sword::new);
        Content.add("taint_pickaxe",DarkTools.TaintPick::new);Content.add("taint_shovel",DarkTools.TaintShovel::new);Content.add("skull_axe",DarkTools.Skull::new);Content.add("diabolists_fork",DarkTools.Fork::new);Content.add("soul_crystal",WrathCage.SoulCrystal::new);
        Content.add("submissive_collar",Trinkets::collar);Content.add("ring_of_nutrition",Trinkets::ring);Content.add("blink_focus",BlinkFocus::new);
        for(String id:List.of("profane_rod","profaned_rod","neutronium_rod","neutronium_staff_core","orichalcum_cap","emerald_nugget"))Content.simple(id);
        Content.add("black_dye",()->new Item(new Item.Properties()){
            @Override public net.minecraft.world.InteractionResult interactLivingEntity(ItemStack stack,net.minecraft.world.entity.player.Player player,net.minecraft.world.entity.LivingEntity target,net.minecraft.world.InteractionHand hand){return Items.BLACK_DYE.interactLivingEntity(stack,player,target,hand);}
        });
        if(ForbiddenMagic.loaded("botania"))Content.simple("terrasteel_cap");
        Content.add("tainted_fruit",()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8F).effect(()->uncurable(MobEffects.HUNGER,600),1).build())));
        Content.add("hexcoal",()->new Item(new Item.Properties()){
            @Override public int getBurnTime(ItemStack stack,net.minecraft.world.item.crafting.RecipeType<?> type){return 1+net.minecraft.util.RandomSource.create().nextInt(2400);}
        });
        add("wrath_cage",WrathCage::new);add("tainted_log",()->new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
        add("tainted_planks",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));add("tainted_leaves",Ecology.Leaves::new);add("tainted_sapling",Ecology.Sapling::new);
        add("tainted_stone",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));add("tainted_bricks",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));
        add("nether_star_block",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK)));add("arcane_cake",Ecology.Cake::new);
        add("umbral_rose",Ecology.Rose::new);add("umbral_rose_bush",Ecology.RoseBush::new);
        Heretic.init(bus);BLOCKS.register(bus);ENTITIES.register(bus);
        if(net.neoforged.fml.loading.FMLEnvironment.dist==net.neoforged.api.distmarker.Dist.CLIENT)CompletionClient.init(bus);
    }
    public static void setup(){WrathCage.souls();BlinkFocus.upgrades();Heretic.setup();
        // The modern furnace queries the input, whereas the old API used the smelting output.
        for(var input:List.of(Items.EMERALD,Blocks.EMERALD_ORE.asItem(),Blocks.DEEPSLATE_EMERALD_ORE.asItem()))thaumcraft.device.SmeltingBonuses.register(()->input,()->Content.get("emerald_nugget"));
        thaumcraft.wand.WandTriggers.register(s->s.is(Blocks.OBSIDIAN)||s.is(Blocks.NETHERRACK),Completion::hellfire);
    }
    public static boolean hellfire(net.minecraft.world.level.Level level,ItemStack wand,net.minecraft.world.entity.player.Player player,net.minecraft.core.BlockPos pos,net.minecraft.core.Direction face){
        if(level.isClientSide()||face!=net.minecraft.core.Direction.UP||!thaumcraft.wand.Wands.getFocusItem(wand).isEmpty()||!thaumcraft.research.ResearchGate.isComplete(player,"FM_HELLFIRE")||!level.isEmptyBlock(pos.above())||!player.mayUseItemAt(pos.above(),face,wand)||!level.mayInteract(player,pos))return false;
        if(!(wand.getItem() instanceof thaumcraft.wand.WandItem item)||!item.consumeAllVis(wand,player,new thaumcraft.aspect.AspectList().add(thaumcraft.aspect.Aspect.FIRE,2),true,false))return false;
        level.setBlockAndUpdate(pos.above(),Blocks.FIRE.defaultBlockState());level.playSound(null,pos,net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,net.minecraft.sounds.SoundSource.BLOCKS,1,1);return true;
    }
}
