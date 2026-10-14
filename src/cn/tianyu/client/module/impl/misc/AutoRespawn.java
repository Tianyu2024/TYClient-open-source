package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import net.minecraft.network.play.client.C16PacketClientStatus;

/**
 * @Author: CN_tianyu
 */
public class AutoRespawn extends Module {
    public AutoRespawn(){
        super("AutoRespawn","AutoRespawn", Category.misc);
    }
    @EventTarget
    public void onTick(EventTick event) {
        if (mc.thePlayer != null && mc.thePlayer.isDead) {
            PacketUtils.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
        }
    }
}
