package net.minecraft.client.bilibili;

import cn.tianyu.client.util.impl.Workers;

/**
 * @Author: CN_tianyu
 */
public class BilibiliLiveManager
{
    public static BilibiliLiveApi.LiveRoomInfo currentInfo;

    public static void refresh()
    {
        Workers.IO.execute(new Runnable()
        {
            @Override
            public void run()
            {
                currentInfo = BilibiliLiveApi.getRoomInfo(BilibiliConfig.roomId);
            }
        });
    }

    public static void openBrowserLive()
    {
        long rid = BilibiliConfig.roomId;
        if (rid <= 0) return;
        try
        {
            String url = "https://live.bilibili.com/" + rid;
            if (System.getProperty("os.name").toLowerCase().contains("win"))
            {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        }
        catch (Exception ignored) {}
    }

    // 每帧Tick更新
    public static void onTick()
    {
        BilibiliDanmaku.tick();
    }
}