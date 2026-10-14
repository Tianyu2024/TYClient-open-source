package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;

/**
 * @Author: CN_tianyu
 */
public class ASyncScreenShot extends Module {
    public static ASyncScreenShot getInstance;


    public ASyncScreenShot() {
        super("ASyncScreenShot", "异步截图无卡顿", Category.better);
        getInstance = this;
    }
}
