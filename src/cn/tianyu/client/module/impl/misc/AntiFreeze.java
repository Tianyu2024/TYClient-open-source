package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

/**
 * @Author: CN_tianyu
 */
public class AntiFreeze extends Module {
    public AntiFreeze() {
        super("AntiFreeze", "prevents server plugins from freezing you", Category.misc);
    }

    private final ModeValue mode = new ModeValue("Mode", "Normal", "Normal", "Teleport");

    @EventTarget
    public void onPacketReceiveEvent(EventPacket event) {
        if(event.getState() != EventPacket.EventState.RECEIVE){
            return;
        }
        if (event.getPacket() instanceof S2DPacketOpenWindow
                && ((S2DPacketOpenWindow) event.getPacket()).getWindowTitle().getUnformattedText().contains("frozen")) {
            event.setCancelled(true);
        } else if (event.getPacket() instanceof S02PacketChat
                && ((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText().contains("frozen")) {
            if (mode.is("Teleport")) {
                mc.thePlayer.posY = -999;
            }
            event.setCancelled(true);
        }
    }
}
