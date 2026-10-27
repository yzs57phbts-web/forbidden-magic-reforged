package local.forbiddenmagic.tests;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Synchronous native drop emissions; avoids EntityLookup's deferred end-of-tick visibility. */
final class DropCapture {
    private record Window(ServerLevel level,AABB bounds,List<ItemStack> items){}
    private static Window active;
    private record MobWindow(ServerLevel level,AABB bounds,List<Mob> mobs){}
    private static MobWindow mobWindow;
    static {NeoForge.EVENT_BUS.addListener(DropCapture::join);}
    private static void join(EntityJoinLevelEvent event){
        var mw=mobWindow;if(mw!=null&&event.getLevel()==mw.level&&event.getEntity() instanceof Mob mob&&mw.bounds.contains(mob.position()))mw.mobs.add(mob);
        var window=active;if(window!=null&&event.getLevel()==window.level&&event.getEntity() instanceof ItemEntity item&&window.bounds.contains(item.position())){
            window.items.add(item.getItem().copy());event.setCanceled(true);
        }
    }
    static List<Mob> mobs(ServerLevel level,BlockPos origin,Runnable action){
        if(mobWindow!=null)throw new IllegalStateException("Nested mob observer");var mobs=new ArrayList<Mob>();mobWindow=new MobWindow(level,new AABB(origin).inflate(15),mobs);
        try{action.run();return mobs;}finally{mobWindow=null;}
    }
    static List<ItemStack> take(ServerLevel level,BlockPos origin,Runnable action){
        if(active!=null)throw new IllegalStateException("Nested drop capture");var items=new ArrayList<ItemStack>();active=new Window(level,new AABB(origin).inflate(6),items);
        try{action.run();return items;}finally{active=null;}
    }
}
