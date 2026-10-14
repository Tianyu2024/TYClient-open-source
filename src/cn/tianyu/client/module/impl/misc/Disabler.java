package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @Author: CN_tianyu
 */
public class Disabler extends Module {
    public Disabler(){
        super("Disabler","Disabler", Category.misc);
    }

    private BooleanValue hypixelMode = new BooleanValue("Hypixel Mode", true);
    private BooleanValue hypixelMotionMode = new BooleanValue("Hypixel Motion Mode", true);
    private NumberValue activationDelay = new NumberValue("Activation delay", 0, 0, 4, 0.5);
    private BooleanValue resetDisabler = new BooleanValue("§cReset", false);

    int tickCounter = 0;
    boolean waitingForGround = false;
    boolean applyingMotion = false;
    int stateTickCounter = 0;
    boolean warningDisplayed = false;
    int sprintToggleTick = 0;
    boolean shouldRun = false;
    long lobbyTime = 0;
    long finished = 0;
    long activationDelayMillis;
    final long checkDisabledTime = 4000;
    private int color = new Color(0, 187, 255, 255).getRGB();
    private int dotAnimationCounter = 0;
    private long lastDotUpdate;
    private boolean hasPrintedRunningMessage = false;
    private float barWidth = 80;
    private float barHeight = 5;
    private float filledWidth;
    private float barX, barY;
    private final int borderColor = new Color(30, 30, 30, 200).getRGB();

    private boolean shouldRender;
    private double firstY;
    private boolean reset;
    private float savedYaw, savedPitch;
    private boolean worldJoin;
    private int wDelay;

    public boolean disablerLoaded, running;

    private int getDisablerTicks() {
        return hypixelMode.get() ? 130 : 140;
    }

    @Override
    public void onEnable() {
        if (!disablerLoaded && mc.thePlayer != null) {
            resetState();
        }

        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        barX = scaledResolution.getScaledWidth() / 2 - barWidth / 2;
        barY = scaledResolution.getScaledHeight() / 2 + 28; // 调整到文本下方
    }

    @Override
    public void onDisable() {
        shouldRun = false;
        running = false;
    }

    private void resetState() {
        savedYaw = mc.thePlayer.rotationYaw;
        savedPitch = mc.thePlayer.rotationPitch;
        shouldRun = true;
        tickCounter = 0;
        applyingMotion = false;
        waitingForGround = true;
        stateTickCounter = 0;
        warningDisplayed = false;
        running = false;
        sprintToggleTick = 0;
        lobbyTime = time();
        finished = 0;
        shouldRender = false;
        reset = false;
        worldJoin = false;
        activationDelayMillis = (long) (activationDelay.get() * 1000);
        hasPrintedRunningMessage = false; // 重置打印状态
    }

    public static long time() {
        return System.currentTimeMillis();
    }

    @EventTarget
    public void onWorldJoin(EventWorldLoad e) {
        if(mc.thePlayer!= null){
            resetState();
        }
    }

    @EventTarget
    public void onPreMotion(EventMotion e) {
        if (resetDisabler.get()) {
            DebugUtil.log("&7[&dTYClient&7] &cdisabler resetting...");
            resetState();
            disablerLoaded = false;
            resetDisabler.setValue(false);
        }
        if (checkCompass()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (finished != 0 && mc.thePlayer.onGround && now - finished > checkDisabledTime) {
            DebugUtil.log("&7[&dTYClient&7] &adisabler enabled!");
            finished = 0;
            filledWidth = 0;
            disablerLoaded = true;
        }
        if (!shouldRun) {
            return;
        }

        if ((now - lobbyTime) < activationDelayMillis) {
            return;
        }
        running = true;
        TYClient.instance.rotationManager.setRotation(new Vector2f(0, savedPitch),180,false);

        if (waitingForGround) {
            /*if (mc.thePlayer.ticksExisted <= 3) {
                waitingForGround = false;
                worldJoin = true;
                wDelay = 0;
            }
            else */
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42f;
                waitingForGround = false;
                worldJoin = false;
            }
            return;
        }

        if (MoveUtil.inAirTicks >= 10 || worldJoin && ++wDelay >= 3) {
            if (!applyingMotion) {
                applyingMotion = true;
                firstY = mc.thePlayer.posY;
            }

            if (tickCounter < getDisablerTicks()) {

                if (!hasPrintedRunningMessage) {
                    DebugUtil.log("&7[&dTYClient&7] running disabler...");
                    hasPrintedRunningMessage = true;
                }
                shouldRender = true;

                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionY = 0;
                mc.thePlayer.motionZ = 0;

                if (mc.thePlayer.posY != firstY) {
                    if (!reset) {
                        resetState();
                        activationDelayMillis = 2000;
                        reset = true;
                        DebugUtil.log("&7[&dTYClient&7] &adisabler reset, wait a few seconds...");
                    } else {
                        shouldRun = false;
                        applyingMotion = false;
                        running = false;
                        DebugUtil.log("&7[&dTYClient&7] &cfailed to reset disabler, plz try again!");
                    }
                }

                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    e.setZ(e.getZ() + 0.075);
                    e.setX(e.getX() + 0.075);
                }

                tickCounter++;
            } else if (!warningDisplayed) {
                double totalTimeSeconds = (now - lobbyTime) / 1000.0;
                warningDisplayed = true;
                finished = now;
                shouldRender = false;
                shouldRun = false;
                applyingMotion = false;
                running = false;
            }
        }

        filledWidth = (float) (barWidth * tickCounter / getDisablerTicks()); // 修改这里
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        int[] disp = {scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), scaledResolution.getScaleFactor()};
        barX = scaledResolution.getScaledWidth() / 2 - barWidth / 2;
        barY = scaledResolution.getScaledHeight() / 2 + 28;
    }

    @EventTarget
    public void onMoveInput(EventMoveInput e) {
        if (!running) {
            return;
        }
        e.setForward(0);
        e.setStrafe(0);
        mc.thePlayer.movementInput.jump = false;
    }

    @EventTarget
    public void onRenderTick(EventRender2D event) {
        if (mc.currentScreen != null || !shouldRun || !shouldRender) {
            return;
        }

        // 更新动画计时
        if (System.currentTimeMillis() - lastDotUpdate > 333) {
            dotAnimationCounter = (dotAnimationCounter + 1) % 4;
            lastDotUpdate = System.currentTimeMillis();
        }

        String baseText = "Watchdog Sleeping";

        // 生成动态省略号
        String dots = "";
        switch (dotAnimationCounter) {
            case 1:
                dots = ".";
                break;
            case 2:
                dots = "..";
                break;
            case 3:
                dots = "...";
                break;
            case 4:
                dots = "....";
                break;
            default:
                dots = "";
                break;
        }

        String fullText = baseText + dots;
        int textWidth = mc.fontRendererObj.getStringWidth(fullText);
        ScaledResolution sr = new ScaledResolution(mc);

        // 绘制文字
        int xPos = (sr.getScaledWidth() - textWidth) / 2;
        int yPos = sr.getScaledHeight() / 2 + 8;
        mc.fontRendererObj.drawStringWithShadow(fullText, xPos, yPos, color);

        // 绘制进度条
        if (tickCounter > 0) {
            // 背景阴影
            drawRoundedRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 3, 0xAA000000);

            // 背景条
            drawRoundedRect(barX, barY, barX + barWidth, barY + barHeight, 2, 0xAA222222);

            // 渐变填充条
            drawGradientRect(
                    barX + 0.5f,
                    barY + 0.5f,
                    barX + filledWidth - 0.5f,
                    barY + barHeight - 0.5f,
                    color,
                    adjustColor(color, -50)
            );

            // 高光边框
            drawRoundedRect(barX, barY, barX + barWidth, barY + barHeight, 2, 0x44FFFFFF);
        }
    }

    // 新增绘图方法
    private void drawRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        glColor4f(red, green, blue, alpha);

        // 绘制圆角矩形（简化实现）
        glBegin(GL_POLYGON);
        for (int i = 0; i <= 90; i += 30) {
            float rad = (float) Math.toRadians(i);
            glVertex2f(x + radius + (float) Math.cos(rad) * radius,
                    y + radius + (float) Math.sin(rad) * radius);
        }
        // 类似处理其他三个角...
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    private void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);

        glBegin(GL_QUADS);
        // 起始颜色
        glColor4f(
                (startColor >> 16 & 0xFF) / 255.0F,
                (startColor >> 8 & 0xFF) / 255.0F,
                (startColor & 0xFF) / 255.0F,
                (startColor >> 24 & 0xFF) / 255.0F
        );
        glVertex2f(left, top);
        glVertex2f(left, bottom);

        // 结束颜色
        glColor4f(
                (endColor >> 16 & 0xFF) / 255.0F,
                (endColor >> 8 & 0xFF) / 255.0F,
                (endColor & 0xFF) / 255.0F,
                (endColor >> 24 & 0xFF) / 255.0F
        );
        glVertex2f(right, bottom);
        glVertex2f(right, top);
        glEnd();

        glShadeModel(GL_FLAT);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    private int adjustColor(int original, int amount) {
        int r = Math.min(255, Math.max(0, (original >> 16 & 0xFF) + amount));
        int g = Math.min(255, Math.max(0, (original >> 8 & 0xFF) + amount));
        int b = Math.min(255, Math.max(0, (original & 0xFF) + amount));
        return (original & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public boolean checkCompass(){
        boolean compass = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);

            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("compass")) {
                compass = true;
            }
        }
        return compass;
    }
}
