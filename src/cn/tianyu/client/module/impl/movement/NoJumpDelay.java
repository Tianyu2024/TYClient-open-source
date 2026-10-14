package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class NoJumpDelay extends Module {
    public NoJumpDelay(){
        super("NoJumpDelay","NoJumpDelay", Category.movement);
    }
}
