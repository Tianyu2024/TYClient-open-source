package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;

import java.awt.*;

/**
 * @author CN_tianyu
 */

@DefaultEnabled
public class KeyBoard extends Module {

    public KeyBoard() {
        super("KeyBoard", "按键显示", Category.render);
    }

    @Override
    public void draw() {
        // 可调整baseX/baseY改变整体显示位置
        drawKey(100, 100);
    }

    public void drawKey(int baseX, int baseY) {
        // 颜色定义
        int backgroundColor = new Color(0, 0, 0, 150).getRGB(); // 半透明黑
        int pressedColor = new Color(255, 255, 255, 190).getRGB(); // 半透明白（按下效果）
        int textColor = 0xFFFFFFFF; // 白色文字

        Minecraft mc = Minecraft.getMinecraft();
        GameSettings settings = mc.gameSettings;
        FontRenderer fr = mc.fontRendererObj;

        // 按钮尺寸和间距
        int btnSize = 28;
        int gap = 2;
        // 计算整行总宽度（和空格键宽度一致：3个按键+2个间距）
        int rowTotalWidth = (btnSize * 3) + (gap * 2);
        // 单个大按钮宽度 = (总宽度 - 间距) / 2
        int bigBtnSize = (rowTotalWidth - gap) / 2;

        // ========== 第一行：W键 ==========
        int wX = baseX + btnSize + gap;
        int wY = baseY;
        drawKeyButton(wX, wY, btnSize, settings.keyBindForward.isKeyDown(), "W", textColor, pressedColor, backgroundColor);

        // ========== 第二行：A S D 键 ==========
        // A键
        int aX = baseX;
        int aY = baseY + btnSize + gap;
        drawKeyButton(aX, aY, btnSize, settings.keyBindLeft.isKeyDown(), "A", textColor, pressedColor, backgroundColor);
        // S键
        int sX = baseX + btnSize + gap;
        int sY = baseY + btnSize + gap;
        drawKeyButton(sX, sY, btnSize, settings.keyBindBack.isKeyDown(), "S", textColor, pressedColor, backgroundColor);
        // D键
        int dX = baseX + (btnSize + gap) * 2;
        int dY = baseY + btnSize + gap;
        drawKeyButton(dX, dY, btnSize, settings.keyBindRight.isKeyDown(), "D", textColor, pressedColor, backgroundColor);

        // ========== 第三行：空格键（文字改为———） ==========
        int spaceX = baseX;
        int spaceY = baseY + (btnSize + gap) * 2;
        int spaceWidth = rowTotalWidth; // 宽度覆盖整行
        int spaceHeight = 24;

        // 绘制空格键背景
        Gui.drawRect(spaceX, spaceY, spaceX + spaceWidth, spaceY + spaceHeight,
                settings.keyBindJump.isKeyDown() ? pressedColor : backgroundColor);
        // 绘制空格键文字（改为———）
        String spaceText = "Space";
        int spaceTextX = spaceX + (spaceWidth - fr.getStringWidth(spaceText)) / 2;
        int spaceTextY = spaceY + (spaceHeight - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(spaceText, spaceTextX, spaceTextY, textColor);

        // ========== 第四行：LMB RMB 键（占满整行，中间留gap） ==========
        // LMB（左键）- 占左半部分
        int lmbX = baseX;
        int lmbY = baseY + (btnSize + gap) * 2 + spaceHeight + gap;
        drawKeyButton2(lmbX, lmbY, bigBtnSize, settings.keyBindAttack.isKeyDown(), "LMB", textColor, pressedColor, backgroundColor);

        // RMB（右键）- 占右半部分，中间留gap
        int rmbX = baseX + bigBtnSize + gap;
        int rmbY = baseY + (btnSize + gap) * 2 + spaceHeight + gap;
        drawKeyButton2(rmbX, rmbY, bigBtnSize, settings.keyBindUseItem.isKeyDown(), "RMB", textColor, pressedColor, backgroundColor);
    }

    /**
     * 通用按键绘制方法（原版Gui.drawRect）
     */
    private void drawKeyButton(int x, int y, int size, boolean isPressed, String label, int textColor, int pressedColor, int normalColor) {
        // 原版矩形绘制
        Gui.drawRect(x, y, x + size, y + size, isPressed ? pressedColor : normalColor);
        // 文字居中（带阴影）
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int textX = x + (size - fr.getStringWidth(label)) / 2;
        int textY = y + (size - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(label, textX, textY, textColor);
    }
    private void drawKeyButton2(int x, int y, int size, boolean isPressed, String label, int textColor, int pressedColor, int normalColor) {
        // 原版矩形绘制
        Gui.drawRect(x, y, x + size, y + 26, isPressed ? pressedColor : normalColor);
        // 文字居中（带阴影）
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        int textX = x + (size - fr.getStringWidth(label)) / 2;
        int textY = y + (26 - fr.FONT_HEIGHT) / 2;
        fr.drawStringWithShadow(label, textX, textY, textColor);
    }
}