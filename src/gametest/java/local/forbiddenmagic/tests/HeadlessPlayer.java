package local.forbiddenmagic.tests;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.*;

/** Normal damage/teleport semantics, with outbound packets discarded in the headless fixture. */
final class HeadlessPlayer {
    static ServerPlayer create(ServerLevel level,String name){var player=new ServerPlayer(level.getServer(),level,new GameProfile(UUID.randomUUID(),name),ClientInformation.createDefault());player.connection=new NoPackets(level,player);player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);return player;}
    private static final class NoPackets extends ServerGamePacketListenerImpl {
        NoPackets(ServerLevel level,ServerPlayer p){super(level.getServer(),new Connection(PacketFlow.SERVERBOUND),p,CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(Packet<?> packet){}
        @Override public void send(Packet<?> packet,PacketSendListener callback){}
    }
}
