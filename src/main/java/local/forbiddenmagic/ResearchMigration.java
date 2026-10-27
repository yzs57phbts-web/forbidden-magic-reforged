package local.forbiddenmagic;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.research.*;

/** Preserve earned1.0 unlocks when splitting its combined food node. New players never earn the virtual old key. */
public final class ResearchMigration {
    public static void init(){NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event)->{if(event.getEntity() instanceof ServerPlayer p&&p.tickCount%20==0)migrate(p);});}
    public static boolean migrate(ServerPlayer player){
        var knowledge=Knowledge.get(player);boolean changed=false;
        if(knowledge.isResearchComplete("FM_GLUTTONY")){changed|=knowledge.addResearch("FM_RINGFOOD");changed|=knowledge.addResearch("FM_ARCANECAKE");}
        if(knowledge.isResearchComplete("ROD_fm_dreamwood_staff"))changed|=knowledge.addResearch("ROD_fm_dreamwood");
        if(changed)Knowledge.sync(player);return changed;
    }
}
