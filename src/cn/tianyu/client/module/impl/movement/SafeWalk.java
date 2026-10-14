package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.lwjglx.input.Keyboard;

/**
 * @Author: CN_tianyu
 */
public class SafeWalk extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeValue mode = new ModeValue("Mode", "Sneak", "Sneak", "CancelInput");
    public final BooleanValue autoShift = new BooleanValue("auto-shift", true);
    public final BooleanValue blocksOnly = new BooleanValue("blocks-only", true);
    public final BooleanValue shiftOnJump = new BooleanValue("shift-on-jump", false);
    public final BooleanValue holdShift = new BooleanValue("hold-shift", false);
    public final BooleanValue onlyDown = new BooleanValue("only-down", true);
    public final NumberValue minPitch = new NumberValue("min-pitch", 70F, 0F, 90F, 1F);
    public final NumberValue maxPitch = new NumberValue("max-pitch", 90F, 0F, 90F, 1F);

    private boolean shouldShift;

    public SafeWalk() {
        super("SafeWalk", "安全桥", Category.player);
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mode.getValue().equals("CancelInput")) {
            if (shouldShift && mc.thePlayer.onGround) {
                if (event.getForward() > 0) {
                    event.setForward(0);
                }
                if (event.getStrafe() != 0) {
                    event.setStrafe(0);
                }
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.getEventState() != EventMotion.EventState.PRE) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean shouldActivate = checkConditions();

        if (!shouldActivate) {
            if (mode.getValue().equals("Sneak")) {
                if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                    setShift(false);
                }
            }
            shouldShift = false;
            return;
        }

        boolean overVoid = isOverVoid();
        if (overVoid) {
            shouldShift = true;

            if (mode.getValue().equals("Sneak")) {
                if (mc.thePlayer.onGround) {
                    setShift(true);
                } else if (shiftOnJump.getValue()) {
                    setShift(true);
                }
            }
        } else {
            shouldShift = false;
            if (mode.getValue().equals("Sneak")) {
                if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                    setShift(false);
                }
            }
        }
    }

    private boolean checkConditions() {
        if (!autoShift.getValue()) return false;

        if (onlyDown.getValue()) {
            float pitch = mc.thePlayer.rotationPitch;
            if (pitch < minPitch.getValue().floatValue() || pitch > maxPitch.getValue().floatValue()) {
                if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                    setShift(false);
                }
                return false;
            }
        }

        if (holdShift.getValue()) {
            if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                setShift(false);
                return false;
            }
        }

        if (blocksOnly.getValue()) {
            ItemStack stack = mc.thePlayer.getHeldItem();
            if (stack == null || !(stack.getItem() instanceof ItemBlock)) {
                setShift(false);
                return false;
            }
        }

        if (mc.thePlayer.capabilities.isFlying) {
            setShift(false);
            return false;
        }

        return true;
    }

    private boolean isOverVoid() {
        BlockPos pos = new BlockPos(
                mc.thePlayer.posX,
                mc.thePlayer.posY - 1,
                mc.thePlayer.posZ
        );
        return mc.theWorld.isAirBlock(pos);
    }

    private void setShift(boolean state) {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindSneak.getKeyCode(),
                state
        );
        mc.gameSettings.keyBindSneak.pressed = state;
    }

    @Override
    public void onDisable() {
        setShift(false);
        shouldShift = false;
    }

    @Override
    public void onEnable() {
        shouldShift = false;
        super.onEnable();
    }
}