package net.minecraft.client.bilibili;

import cn.tianyu.client.TYClient;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 * @Author: CN_tianyu
 */

public class BilibiliConfig
{
    private static final File CONFIG_FILE = new File(TYClient.mc.mcDataDir, "config/bilibili-live.cfg");

    // 基础配置
    public static long roomId = 0L;
    // 功能开关
    public static boolean enableHud = true;
    public static boolean enableDanmakuRoll = true;
    public static boolean autoSendOnKillDeath = true;

    public static void load()
    {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader fr = new FileReader(CONFIG_FILE))
        {
            Properties prop = new Properties();
            prop.load(fr);
            roomId = Long.parseLong(prop.getProperty("roomId", "0"));
            enableHud = Boolean.parseBoolean(prop.getProperty("enableHud", "true"));
            enableDanmakuRoll = Boolean.parseBoolean(prop.getProperty("enableDanmakuRoll", "true"));
            autoSendOnKillDeath = Boolean.parseBoolean(prop.getProperty("autoSendOnKillDeath", "true"));
        }
        catch (Exception ignored) {}
    }

    public static void save()
    {
        try
        {
            if (!CONFIG_FILE.getParentFile().exists()) CONFIG_FILE.getParentFile().mkdirs();
            Properties prop = new Properties();
            prop.setProperty("roomId", String.valueOf(roomId));
            prop.setProperty("enableHud", String.valueOf(enableHud));
            prop.setProperty("enableDanmakuRoll", String.valueOf(enableDanmakuRoll));
            prop.setProperty("autoSendOnKillDeath", String.valueOf(autoSendOnKillDeath));
            try (FileWriter fw = new FileWriter(CONFIG_FILE))
            {
                prop.store(fw, "Bilibili Live Client Config");
            }
        }
        catch (Exception ignored) {}
    }
}