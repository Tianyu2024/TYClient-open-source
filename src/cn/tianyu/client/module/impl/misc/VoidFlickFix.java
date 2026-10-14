package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class VoidFlickFix extends Module {
    public static VoidFlickFix getInstance;

    public VoidFlickFix() {
        super("VoidFlickFix", "修复", Category.misc);
        getInstance = this;
    }
}
