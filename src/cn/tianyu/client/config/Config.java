package cn.tianyu.client.config;

import cn.tianyu.client.TYClient;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author CN_tianyu
 */

public abstract class Config {
    public Path getPath() {
        return Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath(), TYClient.instance.NAME, "config", name + ".json");
    }

    private final String name;

    public Config(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract JsonObject saveConfig();

    public abstract void loadConfig(JsonObject var1);
}
