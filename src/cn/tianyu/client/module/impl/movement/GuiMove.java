package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.gui.MainMenuGui.MainMenuGui;
import cn.tianyu.client.gui.clickgui.oldui.ClickGui2;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.input.Keyboard;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class GuiMove extends Module {
    public GuiMove(){
        super("GuiMove","背包行走", Category.movement);
    }

    private static final List<KeyBinding> keys = Arrays.asList(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump);

    public static void updateStates() {
        if (mc.currentScreen != null) {
            for (KeyBinding k : keys) {
                k.setPressed(GameSettings.isKeyDown(k));
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    if (mc.thePlayer.rotationPitch > -90) {
                        mc.thePlayer.rotationPitch -= 5;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    if (mc.thePlayer.rotationPitch < 90) {
                        mc.thePlayer.rotationPitch += 5;
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.thePlayer.rotationYaw -= 5;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.thePlayer.rotationYaw += 5;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(final EventUpdate event) {
        if (mc.currentScreen instanceof GuiContainer || (mc.currentScreen instanceof ClickGui2) || (mc.currentScreen instanceof MainMenuGui)) {
            updateStates();
        }
    }
}
