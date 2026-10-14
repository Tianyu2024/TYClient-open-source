package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.player.EventSprint;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.util.vector.Vector2f;

/**
 * @author CN_tianyu
 */
@DefaultEnabled
public class Sprint extends Module {

    //private final BooleanValue rotate = new BooleanValue("Rotate", true);


    public Sprint() {
        super("Sprint", "自动疾跑", Category.movement);
    }

    @EventTarget
    public void onUpdate(EventTick e) {
        if (!checknull()) {
            if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
                EventSprint event = new EventSprint();
                TYClient.instance.eventManager.callEvent(event);
            }
        }
    }


//    @EventTarget
//    public void onMotion(EventMotion e) {
//        if (checknull()) return;
//
//        if (rotate.get()) {
//            Vector2f temp = new Vector2f(MoveUtil.getRawDirection(), mc.thePlayer.rotationPitch);
//            e.setYaw(temp.getX());
//            e.setPitch(temp.getY());
//        }
//
//        motionFix();
//    }
    private void motionFix() {
        EntityPlayerSP player = mc.thePlayer;

        if (player == null) return;
        if (!player.onGround) return;
        if (!player.isSprinting()) return;

        // 只修斜向疾跑
        if (player.moveForward <= 0F) return;
        if (player.moveStrafing == 0F) return;

        // 特殊情况不修
        if (player.isCollidedHorizontally) return;
        if (player.capabilities.isFlying) return;
        if (player.isInWater()) return;
        if (player.isInLava()) return;
        if (player.isOnLadder()) return;

        // 消除 double 尾数误差
        player.motionX = (double) ((float) player.motionX);
        player.motionZ = (double) ((float) player.motionZ);
    }
    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
    }
}