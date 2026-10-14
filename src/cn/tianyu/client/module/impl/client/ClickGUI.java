package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.client.Minecraft;
import org.lwjglx.input.Keyboard;

import java.awt.*;

/**
 * @author CN_tianyu
 */

public class ClickGUI extends Module {
    public static final BooleanValue Visitable = new BooleanValue("ValueVisitable", true);
    public static final ColorValue maincolor = new ColorValue("GuiBackColor", new Color(18, 237, 237));
    public static final BooleanValue Mcfont = new BooleanValue("Minecraft Font",false);
    public final ModeValue mode = new ModeValue("ui styles", "old", "NeverLose", "new", "old");

    private final Animation anim = new DecelerateAnimation(250, 1);

    public ClickGUI() {
        super("ClickGUI", "就是你现在看到的界面", Category.client);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        switch (mode.getValue()){
            case "NeverLose":
                Minecraft.getMinecraft().displayGuiScreen(TYClient.instance.NeverLoseClickGui);
                break;
            case "old":
                Minecraft.getMinecraft().displayGuiScreen(TYClient.oldclickGui);
                break;
            case "new":
                Minecraft.getMinecraft().displayGuiScreen(TYClient.instance.PXClickGui);
                break;
        }
        setEnable(false);
    }
}
