package local.forbiddenmagic;

import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import thaumcraft.platform.Baubles;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.VisStorage;
import thaumcraft.research.*;

public final class ToolEvents {
    private static final ThreadLocal<Boolean> AREA=ThreadLocal.withInitial(()->false);
    public static void init(){
        NeoForge.EVENT_BUS.addListener(ToolEvents::right);NeoForge.EVENT_BUS.addListener(ToolEvents::rightBlock);NeoForge.EVENT_BUS.addListener(ToolEvents::left);
        NeoForge.EVENT_BUS.addListener(ToolEvents::tick);NeoForge.EVENT_BUS.addListener(ToolEvents::drops);NeoForge.EVENT_BUS.addListener(ToolEvents::blocks);
        NeoForge.EVENT_BUS.addListener(ToolEvents::xp);NeoForge.EVENT_BUS.addListener(ToolEvents::food);NeoForge.EVENT_BUS.addListener(ToolEvents::damage);
        NeoForge.EVENT_BUS.addListener(ToolEvents::breakSpeed);
        NeoForge.EVENT_BUS.addListener(ToolEvents::crafted);
        NeoForge.EVENT_BUS.addListener(ToolEvents::tooltip);
    }
    public static int enchant(LivingEntity entity,ItemStack stack,String name){if(!LegacyConfig.enchantment(name))return 0;var h=entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ResourceKey.create(Registries.ENCHANTMENT,ForbiddenMagic.id(name)));return h.map(e->EnchantmentHelper.getItemEnchantmentLevel(e,stack)).orElse(0);}
    private static boolean creativeWand(Player p,ItemStack s){return !p.isCreative()&&thaumcraft.wand.Wands.isWand(s)&&(thaumcraft.wand.Wands.getRod(s).tag().startsWith("fm_neutronium")||thaumcraft.wand.Wands.getCap(s).tag().equals("fm_orichalcum"));}
    public static void right(PlayerInteractEvent.RightClickItem e){if(creativeWand(e.getEntity(),e.getItemStack())){e.setCanceled(true);return;}if(e.getEntity().isShiftKeyDown()&&DarkTools.cycle(e.getItemStack(),e.getEntity())){e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);}}
    public static void rightBlock(PlayerInteractEvent.RightClickBlock e){if(creativeWand(e.getEntity(),e.getItemStack())){e.setCanceled(true);return;}if(e.getEntity().isShiftKeyDown()&&DarkTools.cycle(e.getItemStack(),e.getEntity())){e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);}}
    public static void left(PlayerInteractEvent.LeftClickBlock e){if(e.getFace()!=null)e.getEntity().getPersistentData().putInt("fm_break_face",e.getFace().ordinal());}
    public static void crafted(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent e){if(e.getEntity().level().isClientSide()||!e.getCrafting().is(Content.get("primal_scribing_tools")))return;for(int i=0;i<Math.min(9,e.getInventory().getContainerSize());i++){var s=e.getInventory().getItem(i);if(s.is(thaumcraft.registry.EldritchContent.PRIMORDIAL_PEARL.get()))s.grow(1);}}
    public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){var s=e.getItemStack();if(!thaumcraft.wand.Wands.isWand(s))return;String rod=thaumcraft.wand.Wands.getRod(s).tag();if(rod.startsWith("fm_blood")&&ForbiddenMagic.loaded("bloodmagic"))BloodCompat.ownerTooltip(s,e.getToolTip());if(rod.startsWith("fm_neutronium")||thaumcraft.wand.Wands.getCap(s).tag().equals("fm_orichalcum"))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("fm.creative_only").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));else if(rod.startsWith("fm_profane")){var tag=s.getOrDefault(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();e.getToolTip().add(net.minecraft.network.chat.Component.translatable("fm.contract",tag.contains("fm_contract")?tag.getInt("fm_contract"):rod.equals("fm_profaned")?0:25000));}}
    public static void breakSpeed(net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed e){if(e.getEntity().getMainHandItem().is(Content.get("taint_pickaxe")))e.getPosition().ifPresent(pos->{float h=e.getState().getDestroySpeed(e.getEntity().level(),pos);e.setNewSpeed(h==0?0:h<5?0.1F:h<20?h/2:5+h);});}
    public static void tick(PlayerTickEvent.Post e){var p=e.getEntity();if(p.level().isClientSide()||p.tickCount%10!=0)return;for(var s:p.getInventory().items)if(s.getItem() instanceof DarkTools.Morph&&s.isDamaged()&&enchant(p,s,"voidtouched")>0)s.setDamageValue(s.getDamageValue()-1);}
    private static void add(LivingDropsEvent e,ItemStack s){e.getDrops().add(new ItemEntity(e.getEntity().level(),e.getEntity().getX(),e.getEntity().getY(),e.getEntity().getZ(),s));}
    public static void drops(LivingDropsEvent e){
        if(!(e.getSource().getEntity() instanceof Player p)||p.level().isClientSide()||!p.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
        var s=p.getMainHandItem();var mob=e.getEntity();var random=mob.getRandom();int looting=CoreEvents.enchant(p,s,Enchantments.LOOTING);
        if(LegacyConfig.bool("silverfishEmeralds")&&mob.getType()==EntityType.SILVERFISH&&e.isRecentlyHit()&&!(p instanceof net.neoforged.neoforge.common.util.FakePlayer)){
            var biome=mob.level().getBiome(mob.blockPosition());boolean mountains=biome.is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN)||biome.is(net.minecraft.resources.ResourceLocation.parse("minecraft:windswept_hills"))||biome.is(net.minecraft.resources.ResourceLocation.parse("minecraft:windswept_forest"))||biome.is(net.minecraft.resources.ResourceLocation.parse("minecraft:windswept_gravelly_hills"));
            if(random.nextInt(mountains?30:70)<=(mountains?2+looting*2:1+looting*3))add(e,Content.stack("emerald_nugget"));
        }
        if(s.is(Content.get("diabolists_fork")))WrathCage.capture(p,mob.getType());
        if(enchant(p,s,"greedy")>0&&looting==0){if(mob.getType()==EntityType.VILLAGER)add(e,new ItemStack(Items.EMERALD));else if(mob instanceof Enemy&&random.nextInt(35)<3)add(e,Content.stack("emerald_nugget"));}
        if(s.is(Content.get("skull_axe"))){ItemStack head=ItemStack.EMPTY;
            if((mob.getType()==EntityType.SKELETON||mob.getType()==EntityType.WITHER_SKELETON||mob.getType()==EntityType.STRAY||mob.getType()==EntityType.BOGGED)&&random.nextInt(26)<=3+looting)head=new ItemStack(mob.getType()==EntityType.WITHER_SKELETON?Items.WITHER_SKELETON_SKULL:Items.SKELETON_SKULL);
            else if((mob.getType()==EntityType.ZOMBIE||mob.getType()==EntityType.HUSK||mob.getType()==EntityType.DROWNED)&&random.nextInt(26)<=2+2*looting)head=new ItemStack(Items.ZOMBIE_HEAD);
            else if(mob.getType()==EntityType.PIGLIN&&random.nextInt(26)<=2+2*looting)head=new ItemStack(Items.PIGLIN_HEAD);
            else if(mob.getType()==EntityType.CREEPER&&random.nextInt(26)<=2+2*looting)head=new ItemStack(Items.CREEPER_HEAD);
            else if(mob instanceof Player victim&&random.nextInt(11)<=1+looting){head=new ItemStack(Items.PLAYER_HEAD);head.set(DataComponents.PROFILE,new ResolvableProfile(victim.getGameProfile()));}
            if(!head.isEmpty())add(e,head);
        }
    }
    public static void xp(LivingExperienceDropEvent e){
        var p=e.getAttackingPlayer();
        if(p!=null&&e.getEntity() instanceof net.minecraft.world.entity.Mob mob&&CoreEvents.enchant(p,p.getMainHandItem(),Enchantments.LOOTING)==0){
            int base=((local.forbiddenmagic.mixin.MobExperienceAccessor)mob).fmBaseExperience();
            e.setDroppedExperience(e.getDroppedExperience()+3*base*enchant(p,p.getMainHandItem(),"educational"));
        }
    }
    public static void blocks(BlockDropsEvent e){
        if(!(e.getBreaker() instanceof ServerPlayer p))return;var tool=e.getTool();int fortune=CoreEvents.enchant(p,tool,Enchantments.FORTUNE),silk=CoreEvents.enchant(p,tool,Enchantments.SILK_TOUCH);
        if(tool.is(Content.get("taint_shovel"))&&e.getState().is(thaumcraft.wand.focus.FocusUtils.TAINT)&&!e.getState().is(Completion.block("tainted_leaves"))&&e.getDrops().isEmpty()&&p.getRandom().nextInt(31)<=1+2*fortune+(silk>0?6:0))e.getDrops().add(new ItemEntity(p.level(),e.getPos().getX()+0.5,e.getPos().getY()+0.5,e.getPos().getZ()+0.5,Content.stack("taint_shard")));
        int cluster=enchant(p,tool,"cluster");if(cluster>0&&fortune==0)thaumcraft.item.tool.SpecialMining.replaceDrops(e.getState(),e.getDrops(),0.2F+0.075F*cluster,p.getRandom());
        if(enchant(p,tool,"consuming")>0&&silk==0)e.getDrops().removeIf(d->LegacyConfig.garbage(d.getItem()));
        if(enchant(p,tool,"corrupting")>0&&p.getRandom().nextInt(3)==1&&BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).getNamespace().equals("thaumcraft")){
            for(var drop:e.getDrops()){var id=BuiltInRegistries.ITEM.getKey(drop.getItem().getItem());if(id.getNamespace().equals("thaumcraft")&&java.util.List.of("air_shard","fire_shard","water_shard","earth_shard","order_shard","entropy_shard").contains(id.getPath())){
                // One independent sinful shard per original crystal, preserving stack count.
                int n=drop.getItem().getCount();drop.setItem(Content.stack(sin(p.getRandom())));drop.getItem().setCount(1);for(int i=1;i<n;i++)net.minecraft.world.level.block.Block.popResource(p.level(),e.getPos(),Content.stack(sin(p.getRandom())));
            }}
        }
        if(!AREA.get()&&enchant(p,tool,"impact")>0&&(tool.getItem() instanceof DarkTools.Pick||tool.getItem() instanceof DarkTools.Shovel)&&tool.isCorrectToolForDrops(e.getState())){
            Direction face=Direction.from3DDataValue(p.getPersistentData().getInt("fm_break_face"));AREA.set(true);try{
                for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){if(a==0&&b==0)continue;var pos=e.getPos().offset(face.getAxis()==Direction.Axis.X?0:a,face.getAxis()==Direction.Axis.Y?0:b,face.getAxis()==Direction.Axis.Z?0:face.getAxis()==Direction.Axis.X?a:b);
                    var state=p.level().getBlockState(pos);if(!tool.isEmpty()&&p.level().hasChunkAt(pos)&&state.getDestroySpeed(p.level(),pos)>=0&&tool.isCorrectToolForDrops(state)&&p.level().mayInteract(p,pos))p.gameMode.destroyBlock(pos);
                }
            }finally{AREA.set(false);}
        }
    }
    private static String sin(net.minecraft.util.RandomSource r){int i;do{i=r.nextInt(7);}while(LegacyConfig.bool("noLust")&&i==3);return new String[]{"wrath_shard","envy_shard","pride_shard","lust_shard","sloth_shard","greed_shard","gluttony_shard"}[i];}
    public static void food(LivingEntityUseItemEvent.Finish e){if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItem().has(DataComponents.FOOD))return;
        if(Baubles.isEquipped(p,Content.get("ring_of_nutrition")))p.getFoodData().eat(2,2);
        if(e.getItem().is(Content.get("tainted_fruit"))){Warp.add(p,1,WarpType.STICKY);p.addEffect(Completion.uncurable(thaumcraft.registry.ModEffects.TAINT_POISON,600));if(p.getRandom().nextFloat()<0.4F)p.addEffect(Completion.uncurable(thaumcraft.registry.ModEffects.THAUMARHIA,600));}
    }
    public static void damage(LivingIncomingDamageEvent e){
        if(e.getEntity().level().isClientSide())return;
        if(e.getSource().getDirectEntity() instanceof Player attacker)e.setAmount(e.getAmount()+1.25F*enchant(attacker,attacker.getMainHandItem(),"wrath"));
        if(!LegacyConfig.bool("noLust")&&e.getAmount()>0&&e.getEntity() instanceof Player p&&Baubles.isEquipped(p,Content.get("submissive_collar"))){var collar=Baubles.find(p,s->s.is(Content.get("submissive_collar")));int doses=3*(int)e.getAmount(),chance=0;if(e.getSource().getEntity() instanceof Player hitter){chance=1;if(hitter.getMainHandItem().is(Content.get("riding_crop"))){doses+=3;chance+=3;}}
            for(int i=0;i<doses;i++)VisStorage.addVis(collar,Aspect.getPrimalAspects().get(p.getRandom().nextInt(6)),1,true);
            if(p.level().dimension()==net.minecraft.world.level.Level.NETHER&&p.getRandom().nextInt(30)<chance)p.drop(Content.stack("lust_shard"),false);
        }
    }
}
