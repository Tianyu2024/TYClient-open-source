package net.minecraft.client.bilibili;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjglx.input.Keyboard;

import java.io.IOException;

/**
 * @Author: CN_tianyu
 */
public class GuiBilibiliLive extends GuiScreen
{
    private GuiTextField roomIdField;
    private GuiButton btnRefresh;
    private GuiButton btnOpenLive;

    private GuiCheckBox checkHud;
    private GuiCheckBox checkDanmaku;
    private GuiCheckBox checkAutoMsg;

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        int centerX = this.width / 2;

        this.roomIdField = new GuiTextField(0, this.fontRendererObj, centerX - 100, 40, 200, 20);
        this.roomIdField.setText(String.valueOf(BilibiliConfig.roomId));

        this.btnRefresh = new GuiButton(1, centerX - 100, 70, 95, 20, "刷新状态");
        this.btnOpenLive = new GuiButton(2, centerX + 5, 70, 95, 20, "打开直播间");

        this.checkHud = new GuiCheckBox(3, centerX - 100, 100, "显示HUD直播面板", BilibiliConfig.enableHud);
        this.checkDanmaku = new GuiCheckBox(4, centerX - 100, 125, "开启弹幕滚动", BilibiliConfig.enableDanmakuRoll);
        this.checkAutoMsg = new GuiCheckBox(5, centerX - 100, 150, "死亡自动直播间提示", BilibiliConfig.autoSendOnKillDeath);

        this.buttonList.add(btnRefresh);
        this.buttonList.add(btnOpenLive);
        this.buttonList.add(checkHud);
        this.buttonList.add(checkDanmaku);
        this.buttonList.add(checkAutoMsg);

        BilibiliLiveManager.refresh();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 1)
        {
            try
            {
                BilibiliConfig.roomId = Long.parseLong(roomIdField.getText());
            }
            catch (NumberFormatException ignored) {}
            BilibiliLiveManager.refresh();
        }
        else if (button.id == 2)
        {
            BilibiliLiveManager.openBrowserLive();
        }
        // 保存开关
        BilibiliConfig.enableHud = checkHud.isChecked();
        BilibiliConfig.enableDanmakuRoll = checkDanmaku.isChecked();
        BilibiliConfig.autoSendOnKillDeath = checkAutoMsg.isChecked();
        BilibiliConfig.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "B站直播设置", this.width / 2, 15, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "直播间ID：", this.width / 2 - 100, 45, 0xAAAAAA);
        this.roomIdField.drawTextBox();

        BilibiliLiveApi.LiveRoomInfo info = BilibiliLiveManager.currentInfo;
        String statusText = "未查询";
        if (info != null)
        {
            statusText = info.live_status == 1 ? "§a直播中" : "§7未开播";
            drawString(fontRendererObj, "标题：" + info.title, width / 2 - 100, 180, 0xffffff);
            drawString(fontRendererObj, "主播：" + info.uname, width / 2 - 100, 200, 0xffffff);
            drawString(fontRendererObj, "在线人数：" + info.online, width / 2 - 100, 220, 0xffffff);
        }
        drawCenteredString(fontRendererObj, statusText, width / 2, 240, 0xffffff);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.roomIdField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        this.roomIdField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        BilibiliConfig.save();
    }
}