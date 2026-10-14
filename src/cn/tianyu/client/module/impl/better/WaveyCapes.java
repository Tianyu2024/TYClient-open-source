package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;

/**
 * @Author: CN_tianyu
 */
public class WaveyCapes extends Module {
    public WaveyCapes() {
        super("WaveyCapes", "物理披风", Category.better);
    }

    public ModeValue windMode = new ModeValue("Wind Mode", "NONE", "NONE","WAVES");
    public ModeValue capeStyle = new ModeValue("Cape Mode", "SMOOTH", "SMOOTH", "BLOCKY");
    public ModeValue capeMovement = new ModeValue("Cape Mode", "BASIC_SIMULATION", "BASIC_SIMULATION", "VANILLA");
    public NumberValue gravity = new NumberValue("Gravity", 25, 0, 40, 1);
    public NumberValue heightMultiplier = new NumberValue("Height Multiplier", 6, 0, 10, 1);
    public BooleanValue onlyLocalPlayer = new BooleanValue("Only Local Player", true);

}
