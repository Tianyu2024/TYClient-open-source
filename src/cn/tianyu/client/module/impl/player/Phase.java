package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.player.EventJump;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.world.WorldSettings;

/**
 * @Author: CN_tianyu
 */
public class Phase extends Module {
    public Phase() {
        super("Phase","穿墙", Category.player);
    }
    private boolean enable = true;


    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        enable = true;
    }

    @EventTarget
    public void onPre(EventWorldLoad event) {
        enable = true;

    }

    @EventTarget
    public void onTick(EventTick event) {
        boolean canClip = mc.thePlayer.capabilities.allowFlying && mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE && enable;
        if (canClip) {
            enable = false;
            this.setEnable(false);
        }
        if (!canClip && !enable) {
            //if (mc.thePlayer.onGround) getModule(BalanceTimer.class).setState(false);
        }
    }

    @EventTarget
    public void onJump(final EventJump event) {
        event.setMotion(event.getMotion() * 4);
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 12, mc.thePlayer.posZ);
        mc.thePlayer.motionY = 4f;
        mc.thePlayer.jump();
        setEnable(false);
    }
}
