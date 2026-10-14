package net.minecraft.client.bilibili;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * @Author: CN_tianyu
 */
public class BilibiliHud
{
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void render()
    {
        if (!BilibiliConfig.enableHud || mc.thePlayer == null) return;

        FontRenderer fr = mc.fontRendererObj;
        int x = mc.displayWidth - 120;
        int y = 10;

        BilibiliLiveApi.LiveRoomInfo info = BilibiliLiveManager.currentInfo;

        // 绘制直播状态
        if (info == null)
        {
            fr.drawStringWithShadow("B站直播：未加载", x, y, 0xAAAAAA);
            y += 12;
        }
        else
        {
            String status = info.live_status == 1 ? "§a直播中" : "§7未开播";
            fr.drawStringWithShadow("B站：" + status, x, y, 0xFFFFFF);
            y += 12;
            fr.drawStringWithShadow("在线：" + info.online, x, y, 0xFFFF55);
            y += 12;
        }

        // 绘制滚动弹幕
        if (!BilibiliConfig.enableDanmakuRoll) return;
        for (String dm : BilibiliDanmaku.danmakuList)
        {
            fr.drawStringWithShadow(dm, x, y, 0xCCCCCC);
            y += 12;
            if (y > mc.displayHeight - 20) break;
        }
    }
}