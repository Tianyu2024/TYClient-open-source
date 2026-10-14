package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.combat.VlFix;
import cn.tianyu.client.value.impl.BooleanValue;

/**
 * @Author: CN_tianyu
 */
public class Fly extends Module {
    private BooleanValue keephurt = new BooleanValue("Keep Hurt",true);
    public Fly() {
        super("Fly","飞行", Category.movement);
    }
    @Override
    public void onEnable() {
        if (mc.thePlayer==null)
            return;
        if(mc.thePlayer.onGround){
            mc.thePlayer.jump();
            mc.thePlayer.handleStatusUpdate((byte) 2);
        }
    }

    @Override
    public void onDisable() {
        TYClient.moduleManager.getModule(VlFix.class).setEnable(true);
        mc.timer.timerSpeed = 1.0F;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        mc.thePlayer.fallDistance -=1;
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.motionY = 0;
        if(keephurt.getValue()){
            mc.thePlayer.handleStatusUpdate((byte) 2);
        }
//        mc.thePlayer.fallDistance = 3;
//        mc.thePlayer.capabilities.isFlying = false;
//        mc.thePlayer.motionY = 0;
//        mc.thePlayer.motionX = 0;
//        mc.thePlayer.motionZ = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown())
            mc.thePlayer.motionY += 1;
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            mc.thePlayer.motionY -= 1;
//        MoveUtil.strafe(2);
    }
}
