package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.gui.music.MusicManager;
import cn.tianyu.client.gui.music.impl.ui.MusicPlayerGui;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.client.Minecraft;

/**
 * @Author: CN_tianyu
 */
public class musicManager extends Module {
    public musicManager() {
        super("musicManager", "音乐播放器", Category.client);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Minecraft.getMinecraft().displayGuiScreen(new MusicPlayerGui(new MusicManager()));
        this.setEnable(false);
    }
}