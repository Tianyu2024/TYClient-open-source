package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.NumberValue;

/**
 * @Author: CN_tianyu
 */
public class TabListAnimations extends Module {
    public TabListAnimations() {
        super("TabListAnimations","Tab动画", Category.render);
        getInstance = this;
    }

    public final NumberValue maxDownY = new NumberValue("MaxDownY", 10.0, 10.0, 200.0, 10.0);
    public static TabListAnimations getInstance;
}
