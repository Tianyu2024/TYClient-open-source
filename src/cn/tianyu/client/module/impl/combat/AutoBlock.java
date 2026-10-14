package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.CoolDown;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import org.lwjglx.input.Mouse;

import java.awt.*;
import java.awt.event.InputEvent;

import static cn.tianyu.client.module.impl.player.ScaffoldAim.isPlayerInGame;

/**
 * @Author: CN_tianyu
 */
public class AutoBlock extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final NumberValue duration = new NumberValue("Block Duration", 100, 1, 500, 1);
    public final NumberValue distance = new NumberValue("Distance", 3, 0, 6, 0.01);
    public final NumberValue chance = new NumberValue("Chance", 100.0F, 0.0F, 100.0F, 1.0F);
    public final BooleanValue useRobot = new BooleanValue("Use Robot", true);
    public final ModeValue autoClickerMode = new ModeValue("AutoClicker Mode", "Normal", "Normal", "Sync", "Smart");

    private boolean engaged;
    private final CoolDown engagedTime = new CoolDown(0);
    private Robot robot;
    private boolean isAutoClickerEnabled;
    private long lastClickTime;
    private int clicksInSecond;
    private double currentCPS;

    public AutoBlock() {
        super("AutoBlock", "自动格挡", Category.combat);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        setSuffix(getDisplayInfo());
        if (!isPlayerInGame() || !isPlayerHoldingSword()) {
            if (engaged) {
                engaged = false;
                release();
            }
            return;
        }

        checkAutoClickerStatus();

        if (autoClickerMode.is("Sync") || autoClickerMode.is("Smart")) {
            handleAutoClickerMode();
        } else {
            handleNormalMode();
        }
    }

    private void handleNormalMode() {
        if (engaged) {
            if ((engagedTime.hasFinished() || !Mouse.isButtonDown(0))
                    && duration.getMin() <= engagedTime.getElapsedTime()) {
                engaged = false;
                release();
            }
            return;
        }

        if (Mouse.isButtonDown(0)
                && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit != null
                && mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit) >= distance.getMin()
                && mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit) <= distance.getMax()
                && (chance.get() == 100 || Math.random() <= chance.get() / 100)) {
            engaged = true;
            engagedTime.setCooldown((long) duration.getMax());
            engagedTime.start();
            press();
        }
    }

    private void handleAutoClickerMode() {
        if (!isAutoClickerEnabled) {
            handleNormalMode();
            return;
        }

        updateCPS();

        if (autoClickerMode.is("Sync")) {
            if (Mouse.isButtonDown(0)) {
                if (!engaged) {
                    engaged = true;
                    engagedTime.setCooldown((long) duration.getMax());
                    engagedTime.start();
                    press();
                }
            } else {
                if (engaged) {
                    engaged = false;
                    release();
                }
            }
        } else if (autoClickerMode.is("Smart")) {
            handleSmartMode();
        }
    }

    private void handleSmartMode() {
        if (!Mouse.isButtonDown(0)) {
            if (engaged) {
                engaged = false;
                release();
            }
            return;
        }

        double targetInterval = 1000.0 / Math.max(currentCPS, 1.0);
        long blockDuration = (long) Math.min(duration.getMax(), targetInterval * 0.6);

        if (!engaged) {
            if (currentCPS >= 6.0) {
                engaged = true;
                engagedTime.setCooldown(blockDuration);
                engagedTime.start();
                press();
            } else if (currentCPS >= 3.0 && Math.random() < 0.5) {
                engaged = true;
                engagedTime.setCooldown(blockDuration);
                engagedTime.start();
                press();
            }
        } else if (engagedTime.hasFinished()) {
            engaged = false;
            release();

            if (currentCPS >= 8.0 && Math.random() < 0.7) {
                engaged = true;
                engagedTime.setCooldown((long) (blockDuration * 0.8));
                engagedTime.start();
                press();
            }
        }
    }

    private void checkAutoClickerStatus() {
        if (Mouse.isButtonDown(0)) {
            long now = System.currentTimeMillis();
            if (now - lastClickTime < 100) {
                isAutoClickerEnabled = true;
            }
            lastClickTime = now;
        }
    }

    private void updateCPS() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 1000) {
            clicksInSecond++;
        } else {
            currentCPS = clicksInSecond / ((now - lastClickTime) / 1000.0);
            clicksInSecond = 0;
            lastClickTime = now;
        }
    }

    private void press() {
        if (useRobot.getValue() && robot != null) {
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        } else {
            mc.gameSettings.keyBindUseItem.pressed = true;
        }
    }

    private void release() {
        if (useRobot.getValue() && robot != null) {
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        } else {
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    }

    public static boolean isPlayerHoldingSword() {
        return mc.thePlayer != null
                && mc.thePlayer.getCurrentEquippedItem() != null
                && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    @Override
    public void onDisable() {
        if (engaged) {
            engaged = false;
            release();
        }
        isAutoClickerEnabled = false;
        clicksInSecond = 0;
        currentCPS = 0;
        lastClickTime = 0;
    }

    public String getDisplayInfo() {
        String mode = autoClickerMode.getValue();
        if (isAutoClickerEnabled && (autoClickerMode.is("Sync") || autoClickerMode.is("Smart"))) {
            return mode + " | CPS: " + String.format("%.1f", currentCPS);
        }
        return mode + " | " + (engaged ? "Blocking" : "Ready");
    }
}