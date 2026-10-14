package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class NoGuiClose extends Module {
    public NoGuiClose() {
        super("NoGuiClose","防止服务器强制关闭gui", Category.misc);
    }

    public final BooleanValue chatOnly = new BooleanValue("ChatOnly", false);

    @EventTarget
    public void onPacketReceive(final EventPacket event) {
        if(event.getState() != EventPacket.EventState.RECEIVE){
            return;
        }
        if (event.getPacket() instanceof S2EPacketCloseWindow && (mc.currentScreen instanceof GuiChat || !chatOnly.get()))
            event.setCancelled(true);
    }
}
