package local.forbiddenmagic;

import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.ModEffects;
import thaumcraft.research.*;
import wayoftime.bloodmagic.ritual.*;

/** BM discovers RitualRegister across all loaded mod files. No constructor is reached without BM. */
@RitualRegister("forbiddenmagic_delayed_insanity")
public final class SanityRitual extends Ritual {
    public SanityRitual(){super("forbiddenmagic_delayed_insanity",1,20000,"ritual.forbiddenmagic.delayed_insanity");}
    @Override public int getRefreshCost(){return 15000;}
    @Override public int getRefreshTime(){return 1;}
    @Override public Ritual getNewCopy(){return new SanityRitual();}
    @Override public boolean activateRitual(IMasterRitualStone stone,Player player,java.util.UUID owner){return LegacyConfig.integration("bloodmagic")&&super.activateRitual(stone,player,owner);}
    public static MobEffectInstance wardEffect(){var ward=new MobEffectInstance(ModEffects.WARP_WARD,602,0,true,false);ward.getCures().clear();return ward;}
    @Override public void performRitual(IMasterRitualStone stone){
        var level=stone.getWorldObj();if(!LegacyConfig.integration("bloodmagic")||level.isClientSide()||level.getGameTime()%600!=0)return;
        var players=level.getEntitiesOfClass(ServerPlayer.class,new AABB(stone.getMasterBlockPos()).inflate(15),p->p.isAlive()&&!p.isSpectator());
        var network=stone.getOwnerNetwork();
        if(network.getCurrentEssence()<getRefreshCost()*players.size()){network.causeNausea();return;}
        int refreshed=0;
        for(var player:players){
            var effect=player.getEffect(ModEffects.WARP_WARD);
            if(effect!=null&&effect.getDuration()>600)continue;
            var ward=wardEffect();
            if(player.addEffect(ward)){
                refreshed++;
                if(level.getGameTime()%2400==0&&level.random.nextInt(35)<=3)Warp.add(player,1+level.random.nextInt(3),WarpType.TEMPORARY);
            }
        }
        if(refreshed>0)network.syphon(stone.ticket(getRefreshCost()*refreshed));
    }
    @Override public void gatherComponents(Consumer<RitualComponent> consumer){
        // Exact 28 offsets from the original ritual. BM3 has no Crystallos reagent system.
        for(int sign:new int[]{-1,1}){
            for(boolean swap:new boolean[]{false,true}){
                int x=swap?0:4*sign,z=swap?4*sign:0;
                addRune(consumer,x,1,z,EnumRuneType.DUSK);addRune(consumer,x,2,z,EnumRuneType.EARTH);
                addRune(consumer,x,3,z,EnumRuneType.DUSK);addRune(consumer,x,4,z,EnumRuneType.AIR);
                for(int side:new int[]{-1,1})addRune(consumer,swap?side:x,4,swap?z:side,EnumRuneType.EARTH);
                addRune(consumer,swap?0:3*sign,4,swap?3*sign:0,EnumRuneType.FIRE);
            }
        }
    }
}
