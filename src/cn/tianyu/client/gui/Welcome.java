package cn.tianyu.client.gui;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.Login.ShitLoginScreen;
import cn.tianyu.client.gui.MainMenuGui.MainMenuGui;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.LayeringAnimation;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class Welcome extends GuiScreen {
    private static final ResourceLocation HACKER_IMAGE = new ResourceLocation("client/ui/1.png");
    private static final ResourceLocation LEGIT_IMAGE = new ResourceLocation("client/ui/2.png"); // 假设合法模式的图片

    private int titleY = 30;
    private boolean hasSelectedMode = false;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        // 绘制背景
        RenderUtil.drawImage(new ResourceLocation("client/bg/1.jpg"), 0, 0, sr.getScaledWidth(), sr.getScaledHeight());

        // 绘制标题
        FontManager.F48.drawCenteredString(TYClient.NAME, width / 2, titleY + 41, new Color(255, 255, 255, 255).getRGB());

        if (!hasSelectedMode) {
            // 绘制选择模式界面
            drawModeSelection(mouseX, mouseY);
        } else {
            // 绘制欢迎信息
            String connectionMessage1 = "Welcome! User " + ShitLoginScreen.User + " ! ";
            FontManager.F22.drawCenteredString(connectionMessage1, width / 2 - 11, height / 2 - 20, new Color(0, 0, 0, 255).getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawModeSelection(int mouseX, int mouseY) {
        int boxWidth = 200;
        int boxHeight = 250;
        int gap = 50;
        int totalWidth = boxWidth * 2 + gap;
        int startX = (width - totalWidth) / 2;
        int startY = height / 2 - boxHeight / 2;

        // 绘制黑客模式选择框
        boolean hackerHovered = isHovered(startX, startY, boxWidth, boxHeight, mouseX, mouseY);
        Color hackerColor = hackerHovered ? new Color(255, 50, 50, 180) : new Color(255, 50, 50, 150);
        RoundedUtils.drawRound(startX, startY, boxWidth, boxHeight, 10, hackerColor);

        // 黑客模式文字
        FontManager.tenacityBoldFont20.drawCenteredString("Hacker", startX + boxWidth / 2, startY + 30, Color.WHITE.getRGB());

        // 黑客模式图片
        int imageSize = 150;
        RenderUtil.drawImage(HACKER_IMAGE,
                startX + (boxWidth - imageSize) / 2,
                startY + 70,
                imageSize,
                imageSize);

        // 绘制合法模式选择框
        boolean legitHovered = isHovered(startX + boxWidth + gap, startY, boxWidth, boxHeight, mouseX, mouseY);
        Color legitColor = legitHovered ? new Color(50, 150, 255, 180) : new Color(50, 150, 255, 150);
        RoundedUtils.drawRound(startX + boxWidth + gap, startY, boxWidth, boxHeight, 10, legitColor);

        // 合法模式文字
        FontManager.tenacityBoldFont20.drawCenteredString("Legit", startX + boxWidth + gap + boxWidth / 2, startY + 30, Color.WHITE.getRGB());

        // 合法模式图片
        RenderUtil.drawImage(LEGIT_IMAGE,
                startX + boxWidth + gap + (boxWidth - imageSize) / 2,
                startY + 70,
                imageSize,
                imageSize);
    }

    private boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // 保持原有功能
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (hasSelectedMode) {
            if (mouseButton == 0) {
                if(TYClient.instance.HackerMode){
                    LayeringAnimation.play(new MainMenuGui());
                    TYClient.instance.moduleManager.load();
                    showNotification2();
                    return;
                } else {
                    LayeringAnimation.play(new MainMenuGui());
                    TYClient.instance.moduleManager.load();
                    //TYClient.instance.moduleManager.loadLegit();
                    showNotification();
                    return;
                }
            }
            return;
        }

        // 模式选择逻辑
        int boxWidth = 200;
        int boxHeight = 250;
        int gap = 50;
        int totalWidth = boxWidth * 2 + gap;
        int startX = (width - totalWidth) / 2;
        int startY = height / 2 - boxHeight / 2;

        // 检查黑客模式点击
        if (mouseButton == 0 && isHovered(startX, startY, boxWidth, boxHeight, mouseX, mouseY)) {
            TYClient.instance.HackerMode = true;
            hasSelectedMode = true;
            return;
        }

        // 检查合法模式点击
        if (mouseButton == 0 && isHovered(startX + boxWidth + gap, startY, boxWidth, boxHeight, mouseX, mouseY)) {
            TYClient.instance.HackerMode = false;
            hasSelectedMode = true;
            return;
        }
    }

    private void showNotification() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tooltip");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage(TYClient.NAME + " " + TYClient.VERSION,
                    "Hello～(∠・ω< )⌒☆",
                    TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNotification2() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tooltip");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage(TYClient.NAME + " " + TYClient.VERSION,
                    "Why u be Hacker?",
                    TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}