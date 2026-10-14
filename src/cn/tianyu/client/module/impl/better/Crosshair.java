package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;

/**
 * @Author: CN_tianyu
 */
public class Crosshair extends Module {
    public Crosshair() {
        super("Crosshair","", Category.better);
    }

    private final BooleanValue dynamic = new BooleanValue("Dynamic",false);
    private final NumberValue gap = new NumberValue("Crosshair Gap",5,15,0.25,0.25);
    private final NumberValue width = new NumberValue("Crosshair Width",5,10,1,0.25);
    private final NumberValue size = new NumberValue("Crosshair Size/Length",5,15,1,0.25);
}
