package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.util.ChatComponentText;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;

public class VlFix extends Module {
    private BooleanValue matrixDisabler = new BooleanValue("Matrix", true);
    private BooleanValue ncpDisabler = new BooleanValue("NCP", true);
    private BooleanValue debugChat = new BooleanValue("Debug", true);


    public VlFix() {
        super("FixVl", "FixVl", Category.combat);
    }

    @Override
    public void onEnable(){
        NotificationManager.post(NotificationType.SUCCESS,"TYClient","Fixed.");
        toggle();
    }


    @Override
    public void onUpdate() {
//        super.onUpdate();
//        if (mc.thePlayer == null || mc.theWorld == null) return;
//        if (matrixDisabler.get()) {
//            runMatrixBypass();
//        }
//        if (ncpDisabler.get()) {
//            runNcpBypass();
//        }
    }

    private void runMatrixBypass() {
        try {
            mc.timer.timerSpeed = 1.0F;
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.motionX *= 0.999D;
                mc.thePlayer.motionZ *= 0.999D;
            }
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.thePlayer.setSprinting(mc.thePlayer.isSprinting());
            }
        } catch (Exception e) {
            sendDebug("§cMatrix Bypass Error");
        }
    }

    private void runNcpBypass() {
        try {
            if (Mouse.isGrabbed() && Display.isActive()) {
                Mouse.poll();
            }
        } catch (Exception e) {
            sendDebug("§cNCP Bypass Error");
        }
    }

    private void sendDebug(String message) {
        if (debugChat.get() && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    @EventTarget
    public void ingame(EventUpdate e) {
//        StringBuilder info = new StringBuilder();
//        if (matrixDisabler.get()) info.append("M");
//        if (ncpDisabler.get()) info.append("N");
//        setSuffix(info.length() == 0 ? "OFF" : info.toString());
    }
}