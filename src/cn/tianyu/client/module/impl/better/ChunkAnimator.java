package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.annotations.AnimationHandler;

/**
 * @Author: CN_tianyu
 */
public class ChunkAnimator extends Module {
    public ChunkAnimator() {
        super("ChunkAnimator","区块加载动画", Category.better);
        getInstance = this;
    }

    public AnimationHandler animation = new AnimationHandler();
    public static ChunkAnimator getInstance;
}
