package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.setting.EnableSetting;

/**
 * @author CN_tianyu
 */

@DefaultEnabled
public class CapeMod extends Module {
    private final EnableSetting setting = new EnableSetting("setting", true);
    public CapeMod() {
        super("Cape", "披风", Category.client);
        getSetting().add(setting);
    }
    @Override
    public void onDisable(){
        setEnable(true);
    }
}