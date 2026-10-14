package net.minecraft.client.bilibili;

import com.google.gson.Gson;
import cn.tianyu.client.util.impl.Workers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: CN_tianyu
 */

public class BilibiliDanmaku
{
    private static final Gson GSON = new Gson();
    // 滚动弹幕列表
    public static final CopyOnWriteArrayList<String> danmakuList = new CopyOnWriteArrayList<String>();
    private static long lastFetchTime = 0;
    private static final long FETCH_INTERVAL = 5000;

    // 定时拉取弹幕
    public static void tick()
    {
        long now = System.currentTimeMillis();
        if (now - lastFetchTime < FETCH_INTERVAL) return;
        lastFetchTime = now;

        if (!BilibiliConfig.enableDanmakuRoll || BilibiliConfig.roomId <= 0)
            return;

        Workers.IO.execute(new Runnable()
        {
            @Override
            public void run()
            {
                fetchDanmaku(BilibiliConfig.roomId);
            }
        });
    }

    private static void fetchDanmaku(long roomId)
    {
        try
        {
            String urlStr = "https://api.live.bilibili.com/xlive/web-room/v1/dM/gethistory?roomid=" + roomId;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();

            DanmakuResp resp = GSON.fromJson(sb.toString(), DanmakuResp.class);
            if (resp.code == 0 && resp.data != null && resp.data.room != null)
            {
                danmakuList.clear();
                for (DanmakuItem item : resp.data.room)
                {
                    danmakuList.add(item.nickname + "：" + item.text);
                    if (danmakuList.size() > 15) break;
                }
            }
        }
        catch (Exception e)
        {
            // 静默失败，不抛异常刷屏
        }
    }

    // 发普通直播间弹幕（击杀/死亡自动发送）
    public static void sendLiveMsg(final String msg)
    {
        if (!BilibiliConfig.autoSendOnKillDeath || BilibiliConfig.roomId <= 0) return;
        // 说明：官方弹幕接口需要登录Cookie，客户端无登录
        // 我给你做「替代方案」：只在本地HUD提示，预留接口以后填Cookie就能直接发
        danmakuList.add("系统：" + msg);
    }

    // JSON模型
    public static class DanmakuResp {
        public int code;
        public DanmakuData data;
    }
    public static class DanmakuData {
        public List<DanmakuItem> room;
    }
    public static class DanmakuItem {
        public String nickname;
        public String text;
    }
}