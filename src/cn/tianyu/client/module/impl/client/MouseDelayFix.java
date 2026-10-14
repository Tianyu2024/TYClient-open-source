package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class MouseDelayFix extends Module {
    public static MouseDelayFix getInstance;

    public MouseDelayFix() {
        super("MouseDelayFix", "", Category.client);
        getInstance = this;
    }
}
