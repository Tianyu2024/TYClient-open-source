package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import com.yumegod.obfuscation.Native;

/**
 * @Author: CN_tianyu
 */
@Native
public class CustomSky2 extends Module {
    public static boolean isCustomSky = false;
    public CustomSky2() {
        super("CustomSky2","自定义天空", Category.better);
    }


}
