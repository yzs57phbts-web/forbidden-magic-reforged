package local.forbiddenmagic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class Ecology {
    public static final class Sapling extends BushBlock implements BonemealableBlock {
        public static final IntegerProperty STAGE=BlockStateProperties.STAGE;
        public Sapling(){super(Properties.ofFullCopy(Blocks.OAK_SAPLING));registerDefaultState(stateDefinition.any().setValue(STAGE,0));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(STAGE);}
        @Override protected MapCodec<? extends BushBlock> codec(){return simpleCodec(p->new Sapling());}
        @Override protected void randomTick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(l.getMaxLocalRawBrightness(p.above())>=9&&r.nextInt(7)==0)advance(s,l,p,r);}
        @Override public boolean isValidBonemealTarget(LevelReader l,BlockPos p,BlockState s){return true;}
        @Override public boolean isBonemealSuccess(Level l,RandomSource r,BlockPos p,BlockState s){return r.nextFloat()<0.45F;}
        @Override public void performBonemeal(ServerLevel l,RandomSource r,BlockPos p,BlockState s){advance(s,l,p,r);}
        public static void advance(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(state.getValue(STAGE)==0)level.setBlock(pos,state.setValue(STAGE,1),3);else grow(level,pos,random);}
    }
    /** Final 0.575's small oak geometry, with modern height/replaceability checks before mutation. */
    public static boolean grow(ServerLevel l,BlockPos p,RandomSource r){
        int h=4+r.nextInt(3);if(l.isOutsideBuildHeight(p)||l.isOutsideBuildHeight(p.above(h+1))||!l.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT))return false;
        for(int y=0;y<=h+1;y++){int rad=y==0?0:y>=h-1?2:1;for(int x=-rad;x<=rad;x++)for(int z=-rad;z<=rad;z++){
            var at=p.offset(x,y,z);var s=l.getBlockState(at);if(!l.hasChunkAt(at)||!(s.isAir()||s.canBeReplaced()||s.is(net.minecraft.tags.BlockTags.LEAVES)||s.is(Completion.block("tainted_sapling"))))return false;
        }}
        l.setBlock(p.below(),Blocks.DIRT.defaultBlockState(),3);
        for(int y=h-3;y<=h;y++){int dy=y-h,rad=1-dy/2;for(int x=-rad;x<=rad;x++)for(int z=-rad;z<=rad;z++)if(Math.abs(x)!=rad||Math.abs(z)!=rad||r.nextInt(2)!=0&&dy!=0){var at=p.offset(x,y,z);if(!l.getBlockState(at).isSolidRender(l,at))l.setBlock(at,Completion.block("tainted_leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,1),3);}}
        for(int y=0;y<h;y++)l.setBlock(p.above(y),Completion.block("tainted_log").defaultBlockState(),3);return true;
    }
    public static final class Leaves extends LeavesBlock {
        public Leaves(){super(Properties.ofFullCopy(Blocks.OAK_LEAVES));}
    }
    public static final class Cake extends Block {
        public static final IntegerProperty BITES=IntegerProperty.create("bites",0,11);
        public Cake(){super(Properties.ofFullCopy(Blocks.CAKE).randomTicks());registerDefaultState(stateDefinition.any().setValue(BITES,0));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(BITES);}
        @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return box(1+s.getValue(BITES),0,1,15,8,15);}
        @Override protected void randomTick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(s.getValue(BITES)>0)l.setBlock(p,s.setValue(BITES,s.getValue(BITES)-1),3);}
        @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return l.getBlockState(p.below()).isSolid();}
        @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos q){return !canSurvive(s,l,p)?Blocks.AIR.defaultBlockState():s;}
        @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
            if(!player.canEat(false))return InteractionResult.PASS;if(!l.isClientSide()){player.getFoodData().eat(2,1);player.awardStat(net.minecraft.stats.Stats.EAT_CAKE_SLICE);int bites=s.getValue(BITES);if(bites<11)l.setBlock(p,s.setValue(BITES,bites+1),3);else l.removeBlock(p,false);}return InteractionResult.sidedSuccess(l.isClientSide());
        }
    }
    public static final class Rose extends FlowerBlock {
        public Rose(){super(net.minecraft.world.effect.MobEffects.BLINDNESS,8,Properties.ofFullCopy(Blocks.POPPY));}
    }
    public static final class RoseBush extends DoublePlantBlock implements BonemealableBlock {
        public RoseBush(){super(Properties.ofFullCopy(Blocks.ROSE_BUSH).randomTicks());}
        @Override protected void randomTick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(r.nextInt(10)<=2)spread(l,p,r,8,0);}
        @Override public boolean isValidBonemealTarget(LevelReader l,BlockPos p,BlockState s){return true;}
        @Override public boolean isBonemealSuccess(Level l,RandomSource r,BlockPos p,BlockState s){return true;}
        @Override public void performBonemeal(ServerLevel l,RandomSource r,BlockPos p,BlockState s){spread(l,p,r,8,0);}
    }
    private static void spread(ServerLevel l,BlockPos p,RandomSource r,int steps,int cap){
        int count=0;if(cap>0)for(var at:BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(2,1,2)))if(l.getBlockState(at).is(Completion.block("umbral_rose"))&&++count>=cap)return;
        var flower=Completion.block("umbral_rose").defaultBlockState();BlockPos anchor=p,candidate=anchor.offset(r.nextInt(3)-1,r.nextInt(2)-r.nextInt(2),r.nextInt(3)-1);
        for(int i=0;i<steps;i++){
            if(l.hasChunkAt(candidate)&&l.isEmptyBlock(candidate)&&flower.canSurvive(l,candidate))anchor=candidate;
            candidate=anchor.offset(r.nextInt(3)-1,r.nextInt(2)-r.nextInt(2),r.nextInt(3)-1);
        }
        if(l.hasChunkAt(candidate)&&l.isEmptyBlock(candidate)&&flower.canSurvive(l,candidate))l.setBlock(candidate,flower,3);
    }
}
