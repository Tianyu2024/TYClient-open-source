package cn.tianyu.client.gui.clickgui.oldui;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.ClickGUI;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.Value;

import java.awt.*;

import static cn.tianyu.client.TYClient.mc;

public class ModPanel2 {
    private final Module mod;
    public int x;
    public int y;

    private final int width = 120;
    private final int height = 20;

    private boolean hovered;
    private boolean enabled;
    private boolean drawoption;

    private int settingsHeight;
    private boolean isDraggingNumber;

    public ModPanel2(Module mod) {
        this.mod = mod;
        this.enabled = mod.isEnable();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        // 绘制模块背景
        Color background = mod.isEnable() ? new Color(0, 174, 248, 87) : new Color(0, 0, 0, 131);
        DrawUtil.drawRect(x, y, width, height,background.getRGB());
        Boolean mcfont = ClickGUI.Mcfont.getValue();
        if(mcfont){
            mc.fontRendererObj.drawString(mod.getName(), x + (width / 2), y + (height / 2 - 5), 0xFFFFFFFF);
        }else {
            FontManager.arial22.drawCenteredString(mod.getName(), x + (width / 2), y + (height / 2 - 5), 0xFFFFFFFF);
        }
        // 如果右键打开设置
        if (drawoption) {
            // 计算设置面板高度
            settingsHeight = 5 + mod.getValues().size() * 25;

            // 绘制设置面板背景
            RoundedUtils.drawRound(x + width + 5, y, width, settingsHeight,3.0f,new Color(30, 30, 30, 200));

            // 绘制所有设置项
            int settingY = y + 5;
            for (Value<?> value : mod.getValues()) {
                if (value.isVisible()) {
                    value.setPosition(x + width + 10, settingY);
                    value.draw(value.getX(), value.getY(), mouseX, mouseY);
                    settingY += 25;
                }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && hovered) {
            mod.setEnable(!mod.isEnable());
            enabled = !enabled;
        } else if (mouseButton == 1 && hovered) {
            drawoption = !drawoption;
        }

        // 处理设置项点击
        if (drawoption) {
            for (Value<?> value : mod.getValues()) {
                if (value.isVisible() && mouseX >= value.getX() && mouseX <= value.getX() + width - 10 &&
                        mouseY >= value.getY() && mouseY <= value.getY() + 20) {
                    value.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        // 处理设置项释放
        if (drawoption) {
            for (Value<?> value : mod.getValues()) {
                value.mouseReleased(mouseX, mouseY);
            }
        }
        isDraggingNumber = false;
    }
}