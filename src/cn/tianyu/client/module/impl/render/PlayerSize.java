package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * @Author: CN_tianyu
 */
public class PlayerSize extends Module {
    public PlayerSize() {
        super("PlayerSize","玩家大小", Category.render);
        getInstance = this;
    }

    public static final NumberValue sizeValue = new NumberValue("Size",
            1.0, 0.0, 10.0, 0.1);
    public static PlayerSize getInstance;

    public static void setSize(Entity entityIn) {
        float size = sizeValue.getValue().floatValue();
        if (size == 1.0f) {
            return;
        }
        if (entityIn == mc.thePlayer
                && getInstance.isEnabled()) {
            GlStateManager.scale(size, size, size);
        }
    }
}
