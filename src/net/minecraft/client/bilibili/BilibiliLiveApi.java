package net.minecraft.client.bilibili;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author: CN_tianyu
 */
public class BilibiliLiveApi {
    private static final Gson GSON = new Gson();

    // 获取直播间基础状态
    public static LiveRoomInfo getRoomInfo(long roomId)
    {
        if (roomId <= 0) return null;
        try
        {
            String urlStr = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=" + roomId;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            br.close();
            conn.disconnect();

            Response resp = GSON.fromJson(sb.toString(), Response.class);
            return resp.code == 0 ? resp.data : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // 数据模型
    public static class Response
    {
        public int code;
        public LiveRoomInfo data;
    }

    public static class LiveRoomInfo
    {
        public int live_status;   // 1=直播中 0=未开播
        public String title;
        public int online;
        public String uname;
    }
}
