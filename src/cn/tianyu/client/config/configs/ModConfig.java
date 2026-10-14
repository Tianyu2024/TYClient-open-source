package cn.tianyu.client.config.configs;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.config.Config;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.Value;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;

/**
 * @author CN_tianyu
 */

public class ModConfig extends Config {
    public ModConfig() {
        super("modules.json");
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = new JsonObject();
        for (Module module : TYClient.instance.moduleManager.getModuleMap().values()) {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("enable", Boolean.valueOf(module.getState()));
            moduleObject.addProperty("key", (Number)module.getKey());
            JsonObject valuesObject = new JsonObject();
            for (Value<?> value : module.getValues()) {
                if (value instanceof NumberValue) {
                    valuesObject.addProperty(value.getName(), (Number)((NumberValue)value).getValue());
                    continue;
                }
                if (value instanceof BooleanValue) {
                    valuesObject.addProperty(value.getName(), (Boolean)((BooleanValue)value).getValue());
                    continue;
                }
                if (value instanceof ModeValue) {
                    valuesObject.addProperty(value.getName(), ((ModeValue)value).getValue());
                    continue;
                }
                if (!(value instanceof ColorValue)) continue;
                valuesObject.addProperty(value.getName(), (Number)((ColorValue)value).getValue().getRGB());
            }
            moduleObject.add("values", (JsonElement)valuesObject);
            object.add(module.getName(), (JsonElement)moduleObject);
        }
        return object;
    }

    @Override
    public void loadConfig(JsonObject object) {
        for (Module module : TYClient.instance.moduleManager.getModuleMap().values()) {
            if (!object.has(module.getName())) continue;
            JsonObject moduleObject = object.get(module.getName()).getAsJsonObject();
            if (moduleObject.has("enable")) {
                module.setEnable(moduleObject.get("enable").getAsBoolean());
            }
            if (moduleObject.has("key")) {
                module.setKey(moduleObject.get("key").getAsInt());
            }
            if (!moduleObject.has("values")) continue;
            JsonObject valuesObject = moduleObject.get("values").getAsJsonObject();
            for (Value<?> value : module.getValues()) {
                if (!valuesObject.has(value.getName())) continue;
                JsonElement theValue = valuesObject.get(value.getName());
                if (value instanceof NumberValue) {
                    ((NumberValue)value).setValue(theValue.getAsNumber().doubleValue());
                    continue;
                }
                if (value instanceof BooleanValue) {
                    ((BooleanValue)value).setValue(theValue.getAsBoolean());
                    continue;
                }
                if (value instanceof ModeValue) {
                    ((ModeValue)value).setValue(theValue.getAsString());
                    continue;
                }
                if (!(value instanceof ColorValue)) continue;
                Color color = new Color(theValue.getAsInt());
                ((ColorValue)value).setValue(new Color(color.getRed(), color.getGreen(), color.getBlue()));
            }
        }
    }
}
