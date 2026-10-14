package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;

/**
 * @Author: CN_tianyu
 */
public class AnimatedView extends Module {
    public AnimatedView() {
        super("AnimatedView", "视野动画", Category.better);
        getInstance = this;
    }

    public static AnimatedView getInstance;
}
