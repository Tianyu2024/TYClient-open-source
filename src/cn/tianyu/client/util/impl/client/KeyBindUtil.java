package cn.tianyu.client.util.impl.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;

/**
 * @Author: CN_tianyu
 */
public class KeyBindUtil {
    public static String getKeyName(int keyCode) {
        return keyCode < 0 ? Mouse.getButtonName(keyCode + 100) : Keyboard.getKeyName(keyCode);
    }

    public static boolean isKeyDown(int keyCode) {
        return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
    }

    public static void updateKeyState(int keyCode) {
        KeyBindUtil.setKeyBindState(keyCode, keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        KeyBinding.setKeyBindState(keyCode, pressed);
    }

    public static void pressKeyOnce(int keyCode) {
        KeyBinding.onTick(keyCode);
    }
}
