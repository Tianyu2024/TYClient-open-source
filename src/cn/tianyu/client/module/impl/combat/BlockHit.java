package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventLeftClickMouse;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.PlayerUtil;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

/**
 * @Author: CN_tianyu
 */
public class BlockHit extends Module {
    private Robot robot;

    public BlockHit() {
        super("BlockHit", "自动格挡", Category.combat);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private final ModeValue mode = new ModeValue("ModeValue", "Legit", "Legit", "Auto");
    private final ModeValue abmode = new ModeValue("Abmode", "RobotClick", "RobotClick", "packet");

    @EventTarget
    public void onTick(EventTick e) {
        if(mc.thePlayer != null && mc.theWorld !=null) {
            setSuffix(mode.getValue());
            if(Objects.equals(mode.getValue(), "Auto")
                    && mc.thePlayer.hurtTime > 0) {
                this.block();
            }
        }
    }

    @EventTarget
    public void onLeftClick(EventLeftClickMouse e) {
        if(Objects.equals(mode.getValue(), "Legit") && mc.thePlayer.hurtTime > 0) {
            this.block();
        }
    }

    private void block(){
        switch (abmode.getValue()) {
            case "RobotClick":
                robotclick();
                return;
            case "packet":
                KillAura.blocking = true;
                sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                KillAura.blocking = false;
                return;
        }
    }

    private void robotclick(){
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }
}
