package cn.tianyu.client.script;

import net.minecraft.client.Minecraft;

/**
 * @Author: CN_tianyu
 */
public abstract class Script {
    protected final Minecraft mc = Minecraft.getMinecraft();
    public String name;
    public String desc;
    public boolean running;

    public Script(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public abstract void onTick();
    public abstract void start();
    public abstract void stop();
    public abstract void onDisable();
}