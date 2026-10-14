package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.client.Minecraft;

/**
 * @author CN_tianyu
 */

public class XRay extends Module {
    public XRay() {
        super("XRay", "透矿", Category.useful);
    }

    @Override
    public void onEnable() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }
}
