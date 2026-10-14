package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @Author: CN_tianyu
 */
public class Regen extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", "Vanilla", "Vifa");
    private final NumberValue health = new NumberValue("Health", 1, 20, 1, 1);
    private boolean teleported;

    public Regen(){
        super("Regen","Regen", Category.misc);
    }

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        switch (mode.getValue()) {
            case "Vanilla":
                if (mc.thePlayer.getFoodStats().getFoodLevel() > 3 && mc.thePlayer.getHealth() <= health.getValue().floatValue()) {
                    for (int i = 0; i < 5; i++) {
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer(true));
                    }
                }
                break;
            case "Vifa":
                if (mc.thePlayer.getFoodStats().getFoodLevel() > 3 && mc.thePlayer.getHealth() <= health.getValue().floatValue()) {
                    if (!teleported) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, -999, mc.thePlayer.posZ);
                        if (!mc.thePlayer.onGround) {
                            teleported = true;
                        }
                    }

                    if (teleported) {
                        for (int i = 0; i < 20; i++) {
                            PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ, false));
                        }
                        mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                    }
                } else {
                    teleported = false;
                }
                break;
        }
    }

    @Override
    public void onDisable() {
        teleported = false;
        super.onDisable();
    }
}
