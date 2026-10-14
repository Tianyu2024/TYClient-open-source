package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.potion.Potion;

/**
 * @Author: CN_tianyu
 */
public class AntiDebuff extends Module {
    public AntiDebuff() {
        super("AntiDebuff", "防止负面buff影响视野", Category.useful);
    }

    public BooleanValue removeBlindness = new BooleanValue("Remove blindness", true);
    public BooleanValue removeNausea = new BooleanValue("Remove nausea", true);
//    public BooleanValue removeSideEffects = new BooleanValue("Remove side effects", false);

}
