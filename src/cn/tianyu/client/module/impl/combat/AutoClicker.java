package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventLeftClickMouse;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.KeyBindUtil;
import cn.tianyu.client.util.impl.client.RandomUtils;
import cn.tianyu.client.util.impl.player.ItemUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings;
import org.apache.logging.log4j.core.net.Priority;
import org.lwjglx.input.Mouse;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author: CN_tianyu
 */

public class AutoClicker extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final NumberValue minCPS = new NumberValue("min-cps", 8, 1, 20,1);
    public final NumberValue maxCPS = new NumberValue("max-cps", 12, 1, 20,1);
    //public final BooleanValue blockHit = new BooleanValue("block-hit", false);
    //public final NumberValue blockHitTicks = new NumberValue("block-hit-ticks", 1.5F, 1.0F, 20.0F, 0.5f,this.blockHit::getValue);
    public final BooleanValue weaponsOnly = new BooleanValue("weapons-only", true);
    public final BooleanValue allowTools = new BooleanValue("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanValue breakBlocks = new BooleanValue("break-blocks", true);
    //public final BooleanValue invClick = new BooleanValue("inv-click", false);
    //public final NumberValue invCps = new NumberValue("inv-cps", 1, 1, 20, 1,this.invClick::getValue);
    private boolean clickPending = false;
    private long clickDelay = 0L;
    private boolean blockHitPending = false;
    private long blockHitDelay = 0L;
    private int ticks = 0;

    public AutoClicker() {
        super("AutoClicker", "连点器", Category.combat);
    }

    private long getNextClickDelay() {
        return 1000L / RandomUtils.nextLong(this.minCPS.getValue().longValue(), this.maxCPS.getValue().longValue());
    }

//    private long getBlockHitDelay() {
//        return (long) (50.0F * this.blockHitTicks.getValue());
//    }

    private boolean isBreakingBlock() {
        return mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    public boolean canClick() {
        if (!this.weaponsOnly.getValue()
                || ItemUtil.hasRawUnbreakingEnchant()
                || this.allowTools.getValue() && ItemUtil.isHoldingTool()) {
            if (this.breakBlocks.getValue() && this.isBreakingBlock()) {
                WorldSettings.GameType gameType12 = mc.playerController.getCurrentGameType();
                return gameType12 != WorldSettings.GameType.SURVIVAL && gameType12 != WorldSettings.GameType.CREATIVE;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @EventTarget
    public void onMotion(EventMotion event){
//        if (this.isEnabled() && mc.thePlayer != null && event.isPre() && this.invClick.getValue()) {
//            if (mc.currentScreen instanceof GuiContainer) {
//                GuiContainer screen = (GuiContainer) mc.currentScreen;
//                int mouseX = Mouse.getEventX() * screen.width / mc.displayWidth;
//                int mouseY = screen.height - Mouse.getEventY() * screen.height / mc.displayHeight - 1;
//                if (Mouse.isButtonDown(0)) {
//                    ticks++;
//                    if (ticks > invCps.getValue()) {
//        ((IAccessorGuiScreen) screen).callMouseClicked(mouseX, mouseY, 0);
//                    }
//                } else {
//                    ticks = 0;
//                }
//            }
//        }
        if (event.getEventState() == EventMotion.EventState.PRE) {
            if (this.clickDelay > 0L) {
                this.clickDelay -= 50L;
            }
            if (this.blockHitDelay > 0L) {
                this.blockHitDelay -= 50L;
            }
            if (mc.currentScreen != null) {
                this.clickPending = false;
                this.blockHitPending = false;
            } else {
                if (this.clickPending) {
                    this.clickPending = false;
                    KeyBindUtil.updateKeyState(mc.gameSettings.keyBindAttack.getKeyCode());
                }
                if (this.blockHitPending) {
                    this.blockHitPending = false;
                    KeyBindUtil.updateKeyState(mc.gameSettings.keyBindUseItem.getKeyCode());
                }
                if (this.isEnabled() && this.canClick() && mc.gameSettings.keyBindAttack.isKeyDown()) {
                    if (!mc.thePlayer.isUsingItem()) {
                        while (this.clickDelay <= 0L) {
                            this.clickPending = true;
                            this.clickDelay = this.clickDelay + this.getNextClickDelay();
                            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                            KeyBindUtil.pressKeyOnce(mc.gameSettings.keyBindAttack.getKeyCode());
                        }
                    }
//                    if (this.blockHit.getValue()
//                            && this.blockHitDelay <= 0L
//                            && mc.gameSettings.keyBindUseItem.isKeyDown()
//                            && ItemUtil.isHoldingSword()) {
//                        this.blockHitPending = true;
//                        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
//                        if (!mc.thePlayer.isUsingItem()) {
//                            this.blockHitDelay = this.blockHitDelay + this.getBlockHitDelay();
//                            KeyBindUtil.pressKeyOnce(mc.gameSettings.keyBindUseItem.getKeyCode());
//                        }
//                    }
                }
            }
        }
    }

    @EventPriority(4)
    @EventTarget
    public void onCLick(EventLeftClickMouse event) {
        if (this.isEnabled() && !event.isCancelled()) {
            if (!this.clickPending) {
                this.clickDelay = this.clickDelay + this.getNextClickDelay();
            }
        }
    }

    @Override
    public void onEnable() {
        this.clickDelay = 0L;
        this.blockHitDelay = 0L;
    }

    @EventTarget
    public void verifyValue(EventMotion e) {
        if(this.minCPS.getValue() > this.maxCPS.getValue()) {
            this.maxCPS.setValue(this.minCPS.getValue());
        }
    }

}