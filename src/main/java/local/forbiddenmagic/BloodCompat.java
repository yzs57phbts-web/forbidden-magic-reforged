package local.forbiddenmagic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.Wands;
import thaumcraft.research.Warp;
import wayoftime.bloodmagic.common.item.*;
import wayoftime.bloodmagic.core.data.*;
import wayoftime.bloodmagic.core.registry.OrbRegistry;
import wayoftime.bloodmagic.util.helper.*;
import wayoftime.bloodmagic.event.SoulNetworkEvent;

/** Direct adaptation to the installed Blood Magic 3.4.0 port; no old BM API shim. */
public final class BloodCompat {
    private static final java.util.Map<java.util.UUID,Integer> SEALED_LP=new java.util.HashMap<>();
    private static final net.minecraft.world.item.Tier RAPIER_TIER=thaumcraft.platform.Registration.tier(new thaumcraft.platform.TcToolMaterial(700,10,-3,18,thaumcraft.item.gear.TcMaterials.REPAIRS_VOID_METAL,net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL));
    private static boolean ourEffect(MobEffectInstance e){return e.is(Content.BLOOD_SEAL)||e.is(thaumcraft.registry.ModEffects.TAINT_POISON);}
    public static void syncFlask(ItemStack stack,java.util.Collection<MobEffectInstance> effects){
        var current=stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,net.minecraft.world.item.alchemy.PotionContents.EMPTY);
        if(effects.stream().noneMatch(BloodCompat::ourEffect)&&current.customEffects().stream().noneMatch(BloodCompat::ourEffect))return;
        stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(current.potion(),current.customColor(),java.util.List.copyOf(effects)));
    }
    public static void ownerTooltip(ItemStack stack,java.util.List<net.minecraft.network.chat.Component> lines){var binding=Binding.fromStack(stack);if(binding!=null)lines.add(net.minecraft.network.chat.Component.translatable("fm.collar.owner",binding.getOwnerName()));}
    private static final BloodOrb ORB=new BloodOrb(ForbiddenMagic.id("eldritch_blood_orb"),6,80_000_000,140);
    public static void setup(){
        if(LegacyConfig.integration("bloodmagic")&&OrbRegistry.getOrbsForTier(6).stream().noneMatch(s->s.is(Content.get("eldritch_blood_orb"))))OrbRegistry.tierMap.put(6,Content.stack("eldritch_blood_orb"));
        Warp.registerItem(ForbiddenMagic.id("eldritch_blood_orb"),5);
        Warp.registerItem(ForbiddenMagic.id("rapier_of_the_strix"),2);
        NeoForge.EVENT_BUS.addListener(BloodCompat::onFill);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event)->sealTick(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event)->{if(LegacyConfig.integration("bloodmagic")&&event.getEntity() instanceof Player player&&event.getEffectInstance().is(Content.BLOOD_SEAL)&&!player.level().isClientSide())SEALED_LP.put(player.getUUID(),NetworkHelper.getSoulNetwork(player).getCurrentEssence());});
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent event)->SEALED_LP.clear());
    }
    public static Item orbItem(){return new ItemBloodOrb(()->ORB);}
    public static Item rapier(){return new Rapier();}
    public static Binding bind(ItemStack stack,Player player){
        Binding binding=Binding.fromStack(stack);
        if(binding==null){
            binding=new Binding(player.getUUID(),player.getGameProfile().getName());
            var tag=NBTHelper.getOrCreateTag(stack).copy();tag.put("binding",binding.serializeNBT());NBTHelper.setTag(stack,tag);
        }
        return binding;
    }
    public static SoulNetwork network(ItemStack stack,Player player){return NetworkHelper.getSoulNetwork(bind(stack,player));}
    public static boolean pay(ItemStack stack,Player player,int amount){
        if(player.hasInfiniteMaterials())return true;
        if(amount<=0)return true;
        return network(stack,player).syphon(SoulTicket.item(stack,amount))==amount;
    }
    public static boolean payOrHurt(ItemStack stack,Player player,int amount){
        if(pay(stack,player,amount))return true;
        var result=network(stack,player).syphonAndDamage(player,SoulTicket.item(stack,amount));
        return result.isSuccess()&&player.isAlive();
    }
    public static void recharge(ItemStack stack,Player player){
        if(!LegacyConfig.integration("bloodmagic")||!LegacyConfig.bool("crossWand"))return;
        boolean staff=Wands.isStaff(stack);
        if(player.level().isClientSide()||player.tickCount%(staff?25:100)!=0||staff&&!LegacyWands.hotbar(stack,player))return;
        int unit=Math.max(0,LegacyConfig.integer("bloodvis")-(Wands.getCap(stack).tag().equals("fm_alchemical")?1:0));
        bind(stack,player);
        for(var aspect:Aspect.getPrimalAspects()){
            int amount=Math.min(100,Wands.getMaxVis(stack)-Wands.getVis(stack,aspect));
            if(amount<=0)continue;
            if(pay(stack,player,amount*unit)){Wands.addRealVis(stack,aspect,amount,true);continue;}
            if(!staff&&player.getHealth()<=6)return;
            if(player.getHealth()<=0)return;
            player.setHealth(Math.max(0,player.getHealth()-3));
            Wands.addRealVis(stack,aspect,amount,true);
            if(!player.isAlive())player.die(player.damageSources().magic());
            return;
        }
    }
    public static void refillInk(ItemStack stack,Player player){
        if(!LegacyConfig.integration("bloodmagic")||stack.getDamageValue()<=0)return;
        if(Binding.fromStack(stack)==null)return;
        if(player.hasInfiniteMaterials())((Scribes)stack.getItem()).setStoredDamage(stack,0);
        else if(pay(stack,player,25))((Scribes)stack.getItem()).setStoredDamage(stack,stack.getDamageValue()-1);
        else if(player.getHealth()>6){player.setHealth(player.getHealth()-2);((Scribes)stack.getItem()).setStoredDamage(stack,stack.getDamageValue()-1);}
    }
    public static boolean payBoundInk(ItemStack stack,int amount){
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null||!server.isSameThread())return false;
        var binding=Binding.fromStack(stack);return binding!=null&&NetworkHelper.getSoulNetwork(binding).syphon(SoulTicket.item(stack,amount))==amount;
    }
    private static void onFill(SoulNetworkEvent.Fill event){
        if(!LegacyConfig.integration("bloodmagic"))return;
        var player=event.getNetwork().getPlayer();
        if(player!=null&&player.hasEffect(Content.BLOOD_SEAL))event.setCanceled(true);
    }
    public static void sealTick(Player player){
        if(player.level().isClientSide())return;
        if(!LegacyConfig.integration("bloodmagic")||!player.hasEffect(Content.BLOOD_SEAL)){SEALED_LP.remove(player.getUUID());return;}
        var network=NetworkHelper.getSoulNetwork(player);int current=network.getCurrentEssence();Integer last=SEALED_LP.get(player.getUUID());
        if(last!=null&&current>last)network.setCurrentEssence(last);else SEALED_LP.put(player.getUUID(),current);
    }
    public static void strike(LivingEntity victim){
        if(!LegacyConfig.integration("bloodmagic"))return;
        if(victim instanceof Player player&&!player.level().isClientSide()){
            var network=NetworkHelper.getSoulNetwork(player);int current=network.getCurrentEssence();
            network.setCurrentEssence(Math.max(0,current-Math.max(4000,current/4)));
            player.addEffect(new MobEffectInstance(Content.BLOOD_SEAL,1200));
        }
    }
    public static final class Rapier extends SwordItem implements thaumcraft.item.gear.Repairable {
        public Rapier(){super(RAPIER_TIER,new Item.Properties().attributes(SwordItem.createAttributes(RAPIER_TIER,4,-2.4F)));}
        @Override public boolean hurtEnemy(ItemStack stack,LivingEntity victim,LivingEntity attacker){
            victim.setDeltaMovement(victim.getDeltaMovement().multiply(1,0.8,1));
            if(victim.invulnerableTime>18)victim.invulnerableTime-=5;
            strike(victim);return super.hurtEnemy(stack,victim,attacker);
        }
        @Override public void inventoryTick(ItemStack stack,Level level,Entity owner,int slot,boolean selected){
            if(!level.isClientSide()&&owner.tickCount%20==0&&stack.isDamaged())stack.setDamageValue(stack.getDamageValue()-1);
        }
    }
}
