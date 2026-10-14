package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class BetterChat extends Module {
    public BetterChat() {
        super("BetterChat","更好的聊天栏", Category.better);
        getInstance = this;
    }

    public final BooleanValue enableFont = new BooleanValue("HDFont", false);
    public final ModeValue animations = new ModeValue("Animations",
            "Height", "Height", "Off");
    public final ModeValue backgroundShadow = new ModeValue("Shadow",
            "Visuals", "Normal", "Visuals", "Off");
    public static BetterChat getInstance;
}

