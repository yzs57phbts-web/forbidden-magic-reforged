package local.forbiddenmagic;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.aspect.*;
import thaumcraft.alchemy.FluxBlock;
import thaumcraft.item.AspectTooltips;
import thaumcraft.registry.ModItems;
import thaumcraft.research.*;
import velrondevs.botania.api.block_entity.*;

/** Ports of FM 0.575 flower algorithms. See NOTICE for original FM/Botania attribution. */
public final class Flowers {
    public static final class Functional extends FunctionalFlowerBlockEntity {
        public final String kind;
        public Functional(String kind,BlockPos pos,BlockState state){super(BotanyContent.type(kind),pos,state);this.kind=kind;}
        @Override public boolean acceptsRedstone(){return true;}
        @Override public int getColor(){return switch(kind){case "euclidaisy"->0xFF8CFF;case "whisperweed"->0x745380;default->0x7D0600;};}
        @Override public int getMaxMana(){return switch(kind){case "euclidaisy"->9000;case "whisperweed"->6000;default->360;};}
        @Override public RadiusDescriptor getRadius(){return RadiusDescriptor.Rectangle.square(getEffectivePos(),kind.equals("bloodthorn")?6:3);}
        @Override public void tickFlower(){
            super.tickFlower();if(!LegacyConfig.integration("botania")||kind.equals("bloodthorn")&&!LegacyConfig.integration("bloodmagic")||level==null||level.isClientSide()||redstoneSignal>0)return;
            if(kind.equals("euclidaisy"))euclid();else if(kind.equals("whisperweed"))whisper();else bloodthorn();
        }
        private void euclid(){
            if(getMana()<9000||ticksExisted%400!=0)return;
            Aspect aspect=Aspect.AURA;
            if(level.random.nextInt(10)>=4){var all=Aspect.getEnabledAspects();aspect=all[level.random.nextInt(all.length)];}
            var essence=new net.minecraft.world.item.ItemStack(ModItems.WISP_ESSENCE.get());
            AspectTooltips.setSingle(essence,aspect,2);
            var pos=getEffectivePos();var entity=new ItemEntity(level,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,essence);entity.setPickUpDelay(10);
            if(level.addFreshEntity(entity)){addMana(-9000);sync();}
        }
        private void whisper(){
            if(getMana()<6000||ticksExisted%300!=0)return;
            var pos=getEffectivePos();var players=level.getEntitiesOfClass(ServerPlayer.class,new AABB(Vec3.atLowerCornerOf(pos.offset(-3,-3,-3)),Vec3.atLowerCornerOf(pos.offset(4,4,4))),p->p.isAlive()&&!p.isSpectator());
            if(players.isEmpty())return;
            var player=players.get(level.random.nextInt(players.size()));int amount=1+level.random.nextInt(3);
            if(level.random.nextInt(10)<2){amount+=1+level.random.nextInt(3);Warp.add(player,1+level.random.nextInt(5),WarpType.TEMPORARY);}
            for(int n=0;n<amount;n++){
                var list=level.random.nextInt(25)>2?Aspect.getPrimalAspects():Aspect.getCompoundAspects();
                var aspect=list.get(level.random.nextInt(list.size()));
                // The original grants direct knowledge, including an occasional undiscovered compound.
                Knowledge.get(player).addAspectPool(aspect,1);
            }
            Knowledge.sync(player);addMana(-6000);sync();
        }
        private void bloodthorn(){
            var pos=getEffectivePos();boolean changed=false;
            for(var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(Vec3.atLowerCornerOf(pos.offset(-6,-6,-6)),Vec3.atLowerCornerOf(pos.offset(6,6,6))),e->e.isAlive()&&!(e instanceof Player))){
                if(getMana()<40)break;
                if(!entity.hasEffect(MobEffects.WEAKNESS)&&entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,2))){addMana(-40);changed=true;}
            }
            if(changed)sync();
        }
    }
    public static final class Tainthistle extends GeneratingFlowerBlockEntity {
        private int burnTime;
        public Tainthistle(BlockPos pos,BlockState state){super(BotanyContent.type("tainthistle"),pos,state);}
        public int burnTime(){return burnTime;}
        @Override public int getColor(){return 0x4D00FF;}
        @Override public int getMaxMana(){return 150;}
        @Override public RadiusDescriptor getRadius(){return RadiusDescriptor.Rectangle.square(getEffectivePos(),4);}
        @Override public void tickFlower(){
            super.tickFlower();if(!LegacyConfig.integration("botania")||level==null||level.isClientSide())return;
            boolean changed=false;
            if(ticksExisted%80==0)for(var pos:BlockPos.betweenClosed(getEffectivePos().offset(-4,-4,-4),getEffectivePos().offset(4,4,4))){
                if(!level.hasChunkAt(pos))continue;
                var state=level.getBlockState(pos);
                if(state.getBlock() instanceof FluxBlock){
                    // TC Reforged uses quanta 1..8 instead of legacy metadata. Consume actual quanta.
                    int quanta=state.getValue(FluxBlock.LEVEL);
                    if(level.removeBlock(pos,false)){burnTime=Math.min(4000,burnTime+quanta*20);changed=true;}
                }
            }
            if(burnTime>0){if(ticksExisted%2==0)addMana(Math.min(3,getMaxMana()-getMana()));burnTime--;setChanged();}
            if(changed)sync();
        }
        @Override public void writeToPacketNBT(CompoundTag tag,HolderLookup.Provider provider){super.writeToPacketNBT(tag,provider);tag.putInt("burnTime",burnTime);}
        @Override public void readFromPacketNBT(CompoundTag tag,HolderLookup.Provider provider){super.readFromPacketNBT(tag,provider);burnTime=Math.max(0,Math.min(4000,tag.getInt("burnTime")));}
    }
}
