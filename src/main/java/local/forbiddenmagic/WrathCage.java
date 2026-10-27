package local.forbiddenmagic;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import com.mojang.serialization.MapCodec;
import thaumcraft.aspect.*;
import thaumcraft.essentia.*;

/** Explicit ordinary vanilla/Thaumcraft/optional Twilight Forest souls; no captured NBT. */
public final class WrathCage extends BaseEntityBlock {
    public static final Map<ResourceLocation,Aspect> SOULS=Collections.synchronizedMap(new LinkedHashMap<>());
    public static List<ResourceLocation> soulIds(){synchronized(SOULS){return List.copyOf(SOULS.keySet());}}
    public static void souls(){
        CageRules.load();
    }
    private static void add(String name,Aspect aspect){SOULS.put(ResourceLocation.withDefaultNamespace(name),aspect);}
    public static String soul(ItemStack crystal){return crystal.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("fm_soul");}
    public static ItemStack crystal(String id){var s=Content.stack("soul_crystal");var tag=new CompoundTag();tag.putString("fm_soul",id);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;}
    public static boolean capture(Player player,EntityType<?> type){
        var id=BuiltInRegistries.ENTITY_TYPE.getKey(type);if(!CageRules.ensureSoul(player.level(),type))return false;
        for(var s:player.getInventory().items)if(s.is(Content.get("soul_crystal"))&&soul(s).isEmpty()){
            var tag=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();tag.putString("fm_soul",id.toString());s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return true;
        }return false;
    }
    public WrathCage(){super(Properties.ofFullCopy(Blocks.SPAWNER).strength(5,2000).noOcclusion());}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new WrathCage());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Cage(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:createTickerHelper(t,Completion.CAGE_ENTITY.get(),(a,b,c,d)->d.tick());}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(level.getBlockEntity(pos) instanceof Cage cage))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(stack.is(Content.get("soul_crystal"))&&!soul(stack).isEmpty()){
            var id=ResourceLocation.tryParse(soul(stack));if(id==null||!BuiltInRegistries.ENTITY_TYPE.containsKey(id)||!CageRules.ensureSoul(level,BuiltInRegistries.ENTITY_TYPE.get(id)))return ItemInteractionResult.FAIL;
            if(!level.isClientSide()){ItemStack old=cage.mob.isEmpty()?ItemStack.EMPTY:crystal(cage.mob);cage.mob=id.toString();cage.fuel=0;cage.delay=20;stack.shrink(1);if(stack.isEmpty())player.setItemInHand(hand,old);else if(!old.isEmpty()&&!player.addItem(old))player.drop(old,false);cage.sync();}
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if(stack.is(Content.get("diabolists_fork"))){if(!level.isClientSide()){cage.mode=(cage.mode+1)%3;cage.sync();player.displayClientMessage(Component.translatable("fm.cage.mode",cage.selected().getName(),cage.amount(cage.selected())),true);}return ItemInteractionResult.sidedSuccess(level.isClientSide());}
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&!level.isClientSide()&&level.getBlockEntity(pos) instanceof Cage c&&!c.mob.isEmpty())popResource(level,pos,crystal(c.mob));super.onRemove(state,level,pos,next,moving);
    }
    public static final class SoulCrystal extends Item {
        public SoulCrystal(){super(new Properties().stacksTo(1));}
        @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){var id=ResourceLocation.tryParse(soul(s));if(id!=null&&SOULS.containsKey(id))out.add(BuiltInRegistries.ENTITY_TYPE.get(id).getDescription());}
    }
    public static final class Cage extends BlockEntity implements EssentiaTransport,AspectContainer {
        public String mob="";public int mode=0,fuel=0,delay=20;public boolean slothful=false;private final Map<Aspect,Integer> buffer=new HashMap<>();
        public Cage(BlockPos p,BlockState s){super(Completion.CAGE_ENTITY.get(),p,s);}
        public Aspect species(){return SOULS.getOrDefault(ResourceLocation.tryParse(mob),Aspect.IRA);}
        public Aspect selected(){return mode==1?Aspect.IRA:mode==2?Aspect.DESIDIA:species();}
        public int amount(Aspect a){return buffer.getOrDefault(a,0);}
        public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
        public void tick(){
            if(!(level instanceof ServerLevel server)||!LegacyConfig.bool("wrathCage"))return;
            var id=ResourceLocation.tryParse(mob);if(id==null||!BuiltInRegistries.ENTITY_TYPE.containsKey(id))return;var type=BuiltInRegistries.ENTITY_TYPE.get(id);if(!CageRules.ensureSoul(server,type))return;
            int cost=LegacyConfig.integer("wrathCost"),efficiency=LegacyConfig.integer("wrathEff");
            if(!mob.isEmpty())for(Direction side:Direction.values()){
                var source=Essentia.getConnectable(level,worldPosition,side);if(cost>0&&source!=null&&source.canOutputTo(side.getOpposite())&&source.getSuctionAmount(side.getOpposite())<256&&source.getMinimumSuction()<=256){var a=source.getEssentiaType(side.getOpposite());if(a!=null&&amount(a)<64&&(a==species()||a==Aspect.IRA&&amount(species())<cost||a==Aspect.DESIDIA&&amount(species())<cost&&amount(Aspect.IRA)<cost)){int got=source.takeEssentia(a,1,side.getOpposite());if(got>0){addEssentia(a,got,side);break;}}}
            }
            if(mob.isEmpty()||level.hasNeighborSignal(worldPosition))return;
            if(delay>0){delay--;return;}
            if(cost==0)slothful=false;
            else if(fuel<=0){for(Aspect a:new Aspect[]{species(),Aspect.IRA,Aspect.DESIDIA})if(amount(a)>=cost){buffer.put(a,amount(a)-cost);fuel=efficiency;slothful=a==Aspect.DESIDIA;sync();break;}if(fuel<=0)return;}
            if(level.getEntitiesOfClass(Mob.class,LegacyMechanics.cageLimitBox(worldPosition),e->LegacyMechanics.sameCageFamily(type,e,level)).size()>=6){resetDelay();return;}
            boolean spawned=false;
            for(int n=0;n<3&&(cost==0||fuel>0);n++){
                if(level.getEntitiesOfClass(Mob.class,LegacyMechanics.cageLimitBox(worldPosition),e->LegacyMechanics.sameCageFamily(type,e,level)).size()>=6)break;
                double x=worldPosition.getX()+0.5+(level.random.nextDouble()-level.random.nextDouble())*4,y=worldPosition.getY()+level.random.nextInt(3)-1,z=worldPosition.getZ()+0.5+(level.random.nextDouble()-level.random.nextDouble())*4;
                if(!(type.create(level) instanceof Mob entity))continue;entity.moveTo(x,y,z,level.random.nextFloat()*360,0);
                if(!CageRules.position(server,entity))continue;
                entity.finalizeSpawn(server,level.getCurrentDifficultyAt(entity.blockPosition()),MobSpawnType.SPAWNER,null);
                if(!CageRules.position(server,entity))continue;
                if(server.tryAddFreshEntityWithPassengers(entity)){if(cost>0)fuel--;spawned=true;entity.spawnAnim();server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,x,y,z,12,0.5,0.5,0.5,0.01);}
            }if(spawned){resetDelay();sync();}
        }
        private void resetDelay(){delay=200+level.random.nextInt(100)+(slothful?200:0);setChanged();}
        @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);tag.putString("Soul",mob);tag.putInt("Mode",mode);tag.putInt("Fuel",fuel);tag.putInt("Delay",delay);tag.putBoolean("Slothful",slothful);var b=new CompoundTag();buffer.forEach((a,n)->b.putInt(a.getTag(),n));tag.put("Essentia",b);}
        @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);mob=tag.getString("Soul");mode=Math.floorMod(tag.getInt("Mode"),3);fuel=Math.clamp(tag.getInt("Fuel"),0,64);delay=Math.clamp(tag.getInt("Delay"),0,499);slothful=tag.getBoolean("Slothful");buffer.clear();var b=tag.getCompound("Essentia");for(String key:b.getAllKeys()){var a=Aspect.getAspect(key);if(a!=null)buffer.put(a,Math.clamp(b.getInt(key),0,64));}}
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
        @Override public boolean isConnectable(Direction d){return true;}
        @Override public boolean canInputFrom(Direction d){return true;}
        @Override public boolean canOutputTo(Direction d){return false;}
        @Override public void setSuction(Aspect a,int n){}
        @Override public Aspect getSuctionType(Direction d){return selected();}
        @Override public int getSuctionAmount(Direction d){return LegacyConfig.bool("wrathCage")&&LegacyConfig.integer("wrathCost")>0&&amount(selected())<64?256:0;}
        @Override public int takeEssentia(Aspect a,int n,Direction d){return 0;}
        @Override public int addEssentia(Aspect a,int n,Direction d){if(!doesContainerAccept(a)||n<=0)return 0;int got=Math.min(n,64-amount(a));buffer.put(a,amount(a)+got);if(got>0)sync();return got;}
        @Override public Aspect getEssentiaType(Direction d){return selected();}
        @Override public int getEssentiaAmount(Direction d){return amount(selected());}
        @Override public int getMinimumSuction(){return 0;}
        @Override public boolean renderExtendedTube(){return false;}
        @Override public AspectList getAspects(){return mob.isEmpty()||!LegacyConfig.bool("wrathCage")||LegacyConfig.integer("wrathCost")==0?new AspectList():new AspectList().add(selected(),amount(selected()));}
        @Override public void setAspects(AspectList a){}
        @Override public boolean doesContainerAccept(Aspect a){return LegacyConfig.bool("wrathCage")&&LegacyConfig.integer("wrathCost")>0&&a!=null&&(a==species()||a==Aspect.IRA||a==Aspect.DESIDIA);}
        @Override public int addToContainer(Aspect a,int n){return n-addEssentia(a,n,null);}
        @Override public boolean takeFromContainer(Aspect a,int n){return false;}
        @Override public boolean doesContainerContainAmount(Aspect a,int n){return doesContainerAccept(a)&&amount(a)>=n;}
        @Override public int containerContains(Aspect a){return amount(a);}
    }
}
