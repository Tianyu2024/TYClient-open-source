package cn.tianyu.client.setting;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.TimerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class BlinkComponent {

    public static final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    public static boolean blinking, dispatch;
    public static ArrayList<Class<?>> exemptedPackets = new ArrayList<>();
    public static TimerUtil exemptionWatch = new TimerUtil();

    public static void setExempt(Class<?>... packets) {
        exemptedPackets = new ArrayList<>(Arrays.asList(packets));
        exemptionWatch.reset();
    }

    @EventTarget
    @EventPriority(-1)
    public void onPacketSend(EventPacket event){
        if (mc.thePlayer == null) {
            packets.clear();
            exemptedPackets.clear();
            return;
        }

        if(event.getState() == EventPacket.EventState.SEND) {
            if (mc.thePlayer.isDead || mc.isSingleplayer() || !mc.getNetHandler().doneLoadingTerrain) {
                packets.forEach(PacketUtils::sendPacketNoEvent);
                packets.clear();
                blinking = false;
                exemptedPackets.clear();
                return;
            }

            final Packet<?> packet = event.getPacket();

            if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                    packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                    packet instanceof C01PacketEncryptionResponse) {
                return;
            }

            if (blinking && !dispatch) {
                if (exemptionWatch.hasTimeElapsed(100)) {
                    exemptionWatch.reset();
                    exemptedPackets.clear();
                }

                if (!event.isCancelled() && exemptedPackets.stream().noneMatch(packetClass ->
                        packetClass == packet.getClass())) {
                    packets.add(packet);
                    event.setCancelled(true);
                }
            } else if (packet instanceof C03PacketPlayer) {
                packets.forEach(PacketUtils::sendPacketNoEvent);
                packets.clear();
                dispatch = false;
            }
        }
    }

    public static void dispatch() {
        dispatch = true;
    }

    @EventTarget
    @EventPriority(-1)
    public void onWorld(EventWorldLoad event) {
        packets.clear();
        BlinkComponent.blinking = false;
    }

}
