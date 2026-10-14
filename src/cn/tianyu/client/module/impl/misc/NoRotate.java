package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

/**
 * @Author: CN_tianyu
 */
public class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate","防止服务器强制你转头", Category.misc);
    }

    private final ModeValue mode = new ModeValue("Mode", "Cancel", "test", "Cancel");
    private final BooleanValue fakeUpdate = new BooleanValue("Fake Update", false);

    @EventTarget
    public void onPacket(EventPacket e) {
        if (mc.thePlayer == null) return;
        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) e.getPacket();
            switch (mode.get()) {
                case "Cancel":
                    e.setCancelled(true);
                    break;
            }

            if (fakeUpdate.get()) {
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                        mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                        packet.getYaw(), packet.getPitch(), mc.thePlayer.onGround));
            }
        }
    }
}
