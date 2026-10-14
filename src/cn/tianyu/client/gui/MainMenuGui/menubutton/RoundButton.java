package cn.tianyu.client.gui.MainMenuGui.menubutton;

import cn.tianyu.client.util.impl.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class RoundButton extends GuiButton {
    public RoundButton(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, width, height, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRendererObj;
            // 设置按钮颜色
            int color = 0x88000000; // 半透明黑色背景

            // 鼠标悬停时改变颜色
            if (this.hovered) {
                color = 0xaa000000;
            }

            // 绘制圆角矩形背景
            RoundedUtils.drawRound(this.xPosition, this.yPosition, this.width, this.height, 4, new Color(color));

            // 绘制按钮文字
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, 0xFFFFFF);
        }
    }
}
