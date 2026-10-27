package local.forbiddenmagic;

import net.minecraft.core.registries.*;
import net.minecraft.world.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.Wands;
import thaumcraft.effect.EffectHooks;

public final class CoreEvents {
    public static void init(){
        NeoForge.EVENT_BUS.addListener(CoreEvents::drops);NeoForge.EVENT_BUS.addListener(CoreEvents::food);
        NeoForge.EVENT_BUS.addListener(CoreEvents::blocks);NeoForge.EVENT_BUS.addListener(CoreEvents::join);
        NeoForge.EVENT_BUS.addListener(CoreEvents::damage);
    }
    static int enchant(LivingEntity entity,ItemStack stack,net.minecraft.resources.ResourceKey<Enchantment> id){
        if(id.location().getNamespace().equals("forbiddenmagic")&&!LegacyConfig.enchantment(id.location().getPath()))return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(id),stack);
    }
    private static void add(LivingDropsEvent event,String id,int count){
        var mob=event.getEntity();var drop=new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),Content.stack(id));drop.getItem().setCount(count);drop.setPickUpDelay(10);event.getDrops().add(drop);
    }
    public static void drops(LivingDropsEvent event){
        var mob=event.getEntity();if(mob.level().isClientSide()||!mob.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))return;
        var killer=event.getSource().getEntity();boolean playerKill=killer instanceof Player&&!(killer instanceof FakePlayer)&&event.isRecentlyHit();
        if(playerKill&&EffectHooks.isTainted(mob)&&mob.hasEffect(MobEffects.WEAKNESS)&&mob.getEffect(MobEffects.WEAKNESS).getAmplifier()>=2&&mob.getRandom().nextBoolean())add(event,"tainted_blood_shard",1);
        if(mob.level().dimension()!=Level.NETHER)return;
        if(!playerKill){if(mob.getRandom().nextInt(30)<4)add(event,"sloth_shard",1+mob.getRandom().nextInt(3));return;}
        var player=(Player)killer;var weapon=player.getMainHandItem();int looting=enchant(player,weapon,Enchantments.LOOTING);
        if(mob instanceof Enemy){
            int wrath=2+LegacyMechanics.wrathWeaponBonus(weapon);
            wrath+=enchant(player,weapon,Enchantments.SHARPNESS);
            wrath+=enchant(player,weapon,Enchantments.POWER);
            if(!mob.fireImmune())wrath+=enchant(player,weapon,Enchantments.FIRE_ASPECT)+enchant(player,weapon,Enchantments.FLAME);
            if(mob.getType().is(EntityTypeTags.UNDEAD))wrath+=enchant(player,weapon,Enchantments.SMITE);
            if(mob.getType().is(TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("minecraft:arthropod"))))wrath+=enchant(player,weapon,Enchantments.BANE_OF_ARTHROPODS);
            if(Wands.isWand(weapon))wrath+=Wands.getFocusPotency(weapon);
            if(mob.getRandom().nextInt(61)<=wrath)add(event,"wrath_shard",1);
            if(mob.getType()==EntityType.WITHER||mob.getType()==EntityType.ENDER_DRAGON||mob.getType().is(TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("c:bosses"))))add(event,"pride_shard",2+mob.getRandom().nextInt(1+looting));
            int greed=looting+(Wands.isWand(weapon)?Wands.getFocusTreasure(weapon):0);
            if(greed>0&&mob.getRandom().nextInt(20)<=greed)add(event,"greed_shard",1);
        }
        if(mob instanceof ZombifiedPiglin&&mob.getMainHandItem().is(Content.get("envy_shard")))add(event,"envy_shard",1);
    }
    public static void food(LivingEntityUseItemEvent.Finish event){
        if(event.getEntity() instanceof Player player&&!player.level().isClientSide()&&player.level().dimension()==Level.NETHER
            &&event.getItem().has(DataComponents.FOOD)&&!event.getItem().is(Content.get("gluttony_shard"))&&player.getRandom().nextInt(10)<2)player.drop(Content.stack("gluttony_shard"),false);
    }
    public static void blocks(BlockDropsEvent event){
        if(!(event.getBreaker() instanceof Player player)||player.level().dimension()!=Level.NETHER||!event.getState().is(TagKey.create(Registries.BLOCK,ForbiddenMagic.id("greed_ores"))))return;
        // One roll per actual resource harvest, never for Silk Touch/creative/no-drop breaks.
        if(enchant(player,event.getTool(),Enchantments.SILK_TOUCH)>0||event.getDrops().stream().noneMatch(d->!d.getItem().isEmpty()&&(d.getItem().is(Items.QUARTZ)||d.getItem().is(Items.GOLD_NUGGET))))return;
        int fortune=enchant(player,event.getTool(),Enchantments.FORTUNE);
        if(fortune>0&&player.getRandom().nextInt(60)<=fortune)event.getDrops().add(new ItemEntity(player.level(),event.getPos().getX()+0.5,event.getPos().getY()+0.5,event.getPos().getZ()+0.5,Content.stack("greed_shard")));
    }
    public static void join(EntityJoinLevelEvent event){
        if(!event.getLevel().isClientSide()&&!event.loadedFromDisk()&&event.getEntity() instanceof ZombifiedPiglin pig&&event.getLevel().dimension()==Level.NETHER&&pig.getRandom().nextInt(175)==1){
            pig.setItemSlot(EquipmentSlot.MAINHAND,Content.stack("envy_shard"));pig.setDropChance(EquipmentSlot.MAINHAND,0);
        }
    }
    public static void damage(LivingIncomingDamageEvent event){
        if(event.getEntity().level().isClientSide())return;
        if(event.getEntity() instanceof Player player&&Wands.isWand(player.getMainHandItem())){
            var wand=player.getMainHandItem();String rod=Wands.getRod(wand).tag();
            if(rod.equals("fm_infernal")&&(event.getSource().is(DamageTypeTags.IS_FIRE)||event.getSource().is(DamageTypes.WITHER)))event.setCanceled(true);
            if(rod.equals("fm_tainted")&&event.getSource().getMsgId().equals("taint"))for(int n=0;n<3;n++)Wands.addVis(wand,Aspect.getPrimalAspects().get(player.getRandom().nextInt(6)),1,true);
        }
        if(event.getSource().getEntity() instanceof Player player&&Wands.isWand(player.getMainHandItem())){
            var wand=player.getMainHandItem();if(Wands.getRod(wand).tag().startsWith("fm_blood")&&Wands.getCap(wand).tag().equals("fm_alchemical"))event.getEntity().addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,2));
        }
    }
    public static final class Crop extends SwordItem {
        public Crop(){super(Tiers.WOOD,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.WOOD,3,-2.4F)));}
        @Override public float getDestroySpeed(ItemStack s,net.minecraft.world.level.block.state.BlockState b){return 1;}
        @Override public boolean isValidRepairItem(ItemStack s,ItemStack r){return r.is(Items.LEATHER)||super.isValidRepairItem(s,r);}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){if(player.getVehicle() instanceof LivingEntity mount){if(!level.isClientSide()){player.getItemInHand(hand).hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);mount.hurt(level.damageSources().magic(),1);mount.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,5));}return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide());}return InteractionResultHolder.pass(player.getItemInHand(hand));}
        @Override public boolean hurtEnemy(ItemStack stack,LivingEntity victim,LivingEntity attacker){
            if(!attacker.level().isClientSide()){
                if(victim instanceof AbstractHorse||victim instanceof Pig)victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,5));
                else if(victim instanceof Player||victim instanceof AbstractGolem){victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,1));victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,1));victim.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,1));}
                if(!LegacyConfig.bool("noLust")&&attacker.level().dimension()==Level.NETHER&&attacker.getRandom().nextInt(15)==1)victim.spawnAtLocation(Content.stack("lust_shard"));
            }
            return super.hurtEnemy(stack,victim,attacker);
        }
    }
}
