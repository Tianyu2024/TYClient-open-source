package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class RawMouseInput extends Module {

    public RawMouseInput() {
        super("RawMouseInput", "原始鼠标输入", Category.misc);
    }
    @Override
    public void onEnable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
            }
        }
    }

    @Override
    public void onDisable(){
        if (Mouse.isCreated()) {
            if (GLFW.glfwRawMouseMotionSupported()) {
                GLFW.glfwSetInputMode(Display.getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            }
        }
    }
}