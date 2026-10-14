package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.value.impl.ColorValue;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */

public class HurtColor extends Module {

    public static ColorValue color = new ColorValue("Color",new Color(255, 255, 255, 163));

    public HurtColor(){
        super("HurtColor","HurtColor", Category.useful);
    }

}
