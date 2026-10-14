package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

/**
 * @Author: CN_tianyu
 */

@SuppressWarnings("unused")
public class NoFall extends Module {
    public NoFall() {
        super("NoFall","无掉落伤害", Category.player);
    }

    private final ModeValue mode = new ModeValue("Mode", "Vanilla", "Vanilla", "Packet");

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        if (event.isPre()) {
            this.setSuffix(mode.get());
            if (mc.thePlayer.fallDistance > 3.0 && isBlockUnder()) {
                switch (mode.get()) {
                    case "Vanilla":
                        event.setGround(true);
                        break;
                    case "Packet":
                        PacketUtils.sendPacket(new C03PacketPlayer(true));
                        break;
                }
                mc.thePlayer.fallDistance = 0;
            }
        }
    }

    private boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0) return false;
        for (int offset = 0; offset < (int) mc.thePlayer.posY + 2; offset += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
