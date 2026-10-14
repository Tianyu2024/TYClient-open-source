package cn.tianyu.client.util.impl.client;

/**
 * @Author: CN_tianyu
 */
public class MouseUtils {
    public static boolean isHovered2(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
