package cn.tianyu.client.config.configs;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.config.Config;
import cn.tianyu.client.gui.ui.UiModule;
import cn.tianyu.client.module.impl.client.Debug;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.DebugUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
* @Author: CN_tianyu
*/
public class HudConfig extends Config {
    public HudConfig() {
        super("hud.json");
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (UiModule hud : TYClient.instance.uiManager.getModules()) {
            JsonObject hudObject = new JsonObject();
            hudObject.addProperty("x", hud.getPosX());
            hudObject.addProperty("y", hud.getPosY());
            object.add(hud.getName(), (JsonElement)hudObject);
        }
        return object;
    }

    @Override
    public void loadConfig(JsonObject object) {
        for (UiModule hud : TYClient.instance.uiManager.getModules()) {
            if (!object.has(hud.getName())) continue;
            JsonObject hudObject = object.get(hud.getName()).getAsJsonObject();
            hud.setPosX(hudObject.get("x").getAsDouble() * (double)RenderUtil.width());
            hud.setPosY(hudObject.get("y").getAsDouble() * (double)RenderUtil.height());
        }
    }
}
