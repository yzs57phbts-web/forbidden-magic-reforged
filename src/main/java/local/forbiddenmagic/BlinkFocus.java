package local.forbiddenmagic;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.aspect.*;
import thaumcraft.wand.*;
import thaumcraft.wand.focus.*;

public final class BlinkFocus extends FocusItem {
    public static FocusUpgradeType HELLFIRE,PANDEMONIUM;
    public BlinkFocus(){super(FocusItem.properties(new Properties()));}
    public static void upgrades(){int id=32;while(FocusUpgradeType.byId(id)!=null)id++;HELLFIRE=new FocusUpgradeType(id,"fm_hellfire",ForbiddenMagic.id("textures/foci/hellfire.png"),new AspectList().add(Aspect.INFERNUS,1));do{id++;}while(FocusUpgradeType.byId(id)!=null);PANDEMONIUM=new FocusUpgradeType(id,"fm_pandemonium",ForbiddenMagic.id("textures/foci/pandemonium.png"),new AspectList().add(Aspect.DARKNESS,1));}
    @Override public int getFocusColor(ItemStack s){return 0x909090;}
    // The mounted wand renderer requests BLOCK_ATLAS sprites, not standalone PNG paths.
    @Override public ResourceLocation getFocusTexture(ItemStack s){return ForbiddenMagic.id("item/blink_focus");}
    @Override public ResourceLocation getOrnamentTexture(ItemStack s){return ForbiddenMagic.id("item/blink_focus_ornament");}
    @Override public AspectList getVisCost(ItemStack s){var a=new AspectList().add(Aspect.ENTROPY,300);if(isUpgradedWith(s,HELLFIRE))a.add(Aspect.FIRE,100);if(isUpgradedWith(s,PANDEMONIUM))a.add(Aspect.ORDER,100);return a;}
    @Override public int getActivationCooldown(ItemStack s){return 0;}
    @Override public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack s,int rank){
        if(rank==2)return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL,HELLFIRE,PANDEMONIUM};
        if(rank>=3&&isUpgradedWith(s,HELLFIRE))return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL,FocusUpgradeType.POTENCY,FocusUpgradeType.ENLARGE};
        if(rank>=3&&isUpgradedWith(s,PANDEMONIUM))return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL,FocusUpgradeType.ENLARGE};return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
    }
    public static boolean safe(Level level,LivingEntity entity,Vec3 dest){var box=entity.getBoundingBox().move(dest.subtract(entity.position()));return level.hasChunkAt(BlockPos.containing(dest))&&!level.isOutsideBuildHeight(BlockPos.containing(dest))&&level.getWorldBorder().isWithinBounds(box)&&level.noCollision(entity,box)&&!level.containsAnyLiquid(box);}
    @Override public InteractionResult onFocusRightClick(ItemStack wand,Level level,Player player,HitResult ignored){
        var hit=FocusUtils.getTargetBlock(level,player,false,128);if(hit==null||!(wand.getItem() instanceof WandItem item))return InteractionResult.PASS;
        var p=hit.getBlockPos();var face=hit.getDirection();Vec3 dest=hit.getLocation();
        dest=face==Direction.DOWN?dest.add(0,-2,0):face==Direction.UP?dest:dest.add(face.getStepX()*0.5,0,face.getStepZ()*0.5);
        // Validate all destination chunks before charging or moving anything.
        if(!safe(level,player,dest))return InteractionResult.FAIL;
        ItemStack focus=Wands.getFocusItem(wand);if(!item.consumeAllVis(wand,player,getVisCost(focus),false,false))return InteractionResult.FAIL;
        if(level instanceof ServerLevel server&&player instanceof ServerPlayer sp){
            Vec3 origin=player.position();double radius=3+1.5*Wands.getFocusEnlarge(wand);var targets=server.getEntitiesOfClass(LivingEntity.class,new AABB(dest,dest).inflate(radius),e->e!=player);
            if(!item.consumeAllVis(wand,player,getVisCost(focus),true,false))return InteractionResult.FAIL;
            for(var e:targets){if(isUpgradedWith(focus,HELLFIRE)){float damage=3+3*Wands.getFocusPotency(wand);e.hurt(LegacyMechanics.hellfire(player),damage);e.igniteForSeconds(damage);}
                if(isUpgradedWith(focus,PANDEMONIUM)&&e instanceof Enemy&&safe(level,e,origin))e.teleportTo(origin.x,origin.y,origin.z);}
            sp.teleportTo(dest.x,dest.y,dest.z);sp.resetFallDistance();server.sendParticles(ParticleTypes.PORTAL,origin.x,origin.y+1,origin.z,30,0.4,0.8,0.4,0.05);server.sendParticles(ParticleTypes.PORTAL,dest.x,dest.y+1,dest.z,30,0.4,0.8,0.4,0.05);
            level.playSound(null,p,net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,net.minecraft.sounds.SoundSource.PLAYERS,1,1);
        }return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
