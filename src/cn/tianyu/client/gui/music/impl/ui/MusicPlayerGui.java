package cn.tianyu.client.gui.music.impl.ui;

import cn.tianyu.client.gui.music.MusicManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * @Author: CN_tianyu
 */
public class MusicPlayerGui extends GuiScreen {

    private final MusicManager musicManager;

    // 材质资源路径（你需要准备对应的图片放入你的资源包中）
    private final ResourceLocation coverTexture = new ResourceLocation("tianyu/music/cover_default.png");
    private final ResourceLocation iconsTexture = new ResourceLocation("tianyu/music/icons.png");

    public MusicPlayerGui(MusicManager manager) {
        this.musicManager = manager;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        // 计算居中坐标
        int xMid = this.width / 2;
        int yMid = this.height / 2;

        // 添加按钮（示例：暂停/播放、倍速、音量）
        // 参数：id, x, y, width, height, text
        this.buttonList.add(new GuiButton(0, xMid - 50, yMid + 40, 40, 20, "播放/暂停"));
        this.buttonList.add(new GuiButton(1, xMid, yMid + 40, 50, 20, "倍速: " + musicManager.getPlaySpeed() + "x"));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 1. 绘制唯美的半透明背景（现代感黑巧/白巧风格）
        int xStart = this.width / 2 - 120;
        int yStart = this.height / 2 - 100;
        int panelWidth = 240;
        int panelHeight = 180;

        // 绘制带有一点高阶感的深色卡片背景
        drawRect(xStart, yStart, xStart + panelWidth, yStart + panelHeight, 0xCE1E1E1E);

        // 2. 绘制歌曲封面图片（核心点：点击可以进去）
        int coverX = xStart + 20;
        int coverY = yStart + 20;
        int coverSize = 60;
        this.mc.getTextureManager().bindTexture(coverTexture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // 绘制图片
        drawModalRectWithCustomSizedTexture(coverX, coverY, 0, 0, coverSize, coverSize, coverSize, coverSize);

        // 3. 绘制歌曲信息（歌名、歌手、歌词一行提示）
        this.fontRendererObj.drawStringWithShadow("天宇至臻音乐", coverX + 70, coverY + 5, 0xFFFFFF);
        this.fontRendererObj.drawString("正在播放...", coverX + 70, coverY + 20, 0x888888);

        // 动态展示当前获取的一行歌词
        String currentLyric = "歌词动态：让这旋律在客户端回荡...";
        this.fontRendererObj.drawString(currentLyric, xStart + 20, yStart + 100, 0x00FFAD);

        // 4. 绘制进度条
        int barX = xStart + 20;
        int barY = yStart + 120;
        int barWidth = panelWidth - 40;
        drawRect(barX, barY, barX + barWidth, barY + 4, 0x55FFFFFF); // 底条

        float progressPct = (float) musicManager.getCurrentProgress() / musicManager.getTotalDuration();
        drawRect(barX, barY, barX + (int)(barWidth * progressPct), barY + 4, 0xFF00A9FF); // 进度点亮条

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // 切换 播放 / 暂停
            if (musicManager.getCurrentState() == MusicManager.PlayState.PLAYING) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
        } else if (button.id == 1) {
            // 切换倍速逻辑（1.0 -> 1.5 -> 2.0 -> 1.0）
            float currentSpeed = musicManager.getPlaySpeed();
            if (currentSpeed == 1.0f) musicManager.setPlaySpeed(1.5f);
            else if (currentSpeed == 1.5f) musicManager.setPlaySpeed(2.0f);
            else musicManager.setPlaySpeed(1.0f);
            button.displayString = "倍速: " + musicManager.getPlaySpeed() + "x";
        }
        super.actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int xStart = this.width / 2 - 120;
        int yStart = this.height / 2 - 100;
        int coverX = xStart + 20;
        int coverY = yStart + 20;
        int coverSize = 60;

        // 检测是否点击到了图片区域（"有图片可以点进去"的需求）
        if (mouseButton == 0) { // 左键点击
            if (mouseX >= coverX && mouseX <= coverX + coverSize && mouseY >= coverY && mouseY <= coverY + coverSize) {
                // 点击了图片，触发逻辑（例如：弹出歌曲详情、打开大图、或者切换歌曲）
                this.mc.thePlayer.playSound("random.click", 1.0F, 1.0F);
                // 这里可以执行跳转或展开
                System.out.println("点击了音乐封面图！");
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // 打开音乐播放器时不暂停游戏
    }
}