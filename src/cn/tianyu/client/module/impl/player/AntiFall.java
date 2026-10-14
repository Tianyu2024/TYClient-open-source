package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.movement.LongJump;
import cn.tianyu.client.module.impl.movement.Scaffold;
import cn.tianyu.client.setting.BlinkComponent;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

/**
 * @Author: CN_tianyu
 */
public class AntiFall extends Module {
    public AntiFall() {
        super("AntiFall", "", Category.player);
    }

    public final ModeValue mode = new ModeValue("Mode", "Universal", "Universal");
    private double groundX = 0.0;
    private double groundY = 0.0;
    private double groundZ = 0.0;
    private boolean universalStarted = false;
    private boolean universalFlag = false;

    @Override
    public void onEnable() {
        universalStarted = false;
        universalFlag = false;
        BlinkComponent.blinking = false;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
        BlinkComponent.dispatch();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        setSuffix(mode.getValue());

        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (isEnabled(LongJump.class) || isEnabled(Scaffold.class))
            return;

        switch (mode.getValue()) {
            case "Universal":
                if (universalStarted) {
                    if (mc.thePlayer.onGround || mc.thePlayer.fallDistance > 8.0f) {
                        BlinkComponent.blinking = false;
                        BlinkComponent.dispatch();
                        universalStarted = false;
                        universalFlag = false;
                    } else if (mc.thePlayer.fallDistance > 5.0f && !universalFlag) {
                        universalFlag = true;
                        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(groundX, groundY + 0.5, groundZ, false));
                    }
                } else if (mc.thePlayer.fallDistance > 1.0f && !mc.thePlayer.onGround && mc.thePlayer.motionY < -0.1) {
                    if (isOverVoid()) {
                        universalStarted = true;
                        universalFlag = false;
                        BlinkComponent.blinking = true;
                        groundX = mc.thePlayer.posX;
                        groundY = mc.thePlayer.posY;
                        groundZ = mc.thePlayer.posZ;
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (mc.thePlayer == null) return;
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook s = (S08PacketPlayerPosLook) event.getPacket();
            if (Math.abs(s.getX() - groundX) < 0.1 && Math.abs(s.getY() - groundY) < 0.1 && Math.abs(s.getZ() - groundZ) < 0.1) {
                event.setCancelled(true);
                BlinkComponent.blinking = false;
                mc.thePlayer.setPosition(groundX, groundY, groundZ);
                universalStarted = false;
                universalFlag = false;
            }
        }
    }

    private boolean isOverVoid() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 10, mc.thePlayer.posZ),
                false, true, false) == null;
    }
}