package cn.tianyu.client.gui.ui.modules;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventShader;
import cn.tianyu.client.gui.ui.UiModule;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.annotations.Direction;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.util.impl.render.ColorUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class ModuleList extends UiModule {
    public List<Module> modules;

    public ModuleList() {
        super("ModuleList", RenderUtil.width(), 0.0, 100.0, 0.0);
    }

    @EventTarget
    public void blur(EventShader event) {
        double yOffset = 0.0;
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module module : this.modules) {
            if (HUD.importantModules.getValue() && module.getCategory() == Category.render) continue;
            if (HUD.importantModules.getValue() && module.getCategory() == Category.better) continue;
            if (HUD.importantModules.getValue() && module.getCategory() == Category.useful) continue;

            Animation moduleAnimation = module.getAnimation();
            if (!module.getState() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
            String displayText = this.formatModule(module);
            double textWidth = FontManager.font16.getStringWidth(displayText);
            //double textWidth = mc.fontRendererObj.getStringWidth(displayText);
            double xValue = sr.getScaledWidth() - 10;
            boolean flip = xValue <= (double)((float)sr.getScaledWidth() / 2.0f);
            double x2 = flip ? xValue : (double)sr.getScaledWidth() - (textWidth + 3.0);
            double y2 = yOffset + 4.0;
            double heightVal = (Double)HUD.height.getValue() + 1.0;

            if (flip) {
                x2 -= Math.abs((moduleAnimation.getOutput() - 1.0) * ((double)sr.getScaledWidth() - (2.0 + textWidth)));
                break;
            }
            x2 += Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + textWidth));

            if (((Boolean)HUD.background.getValue()).booleanValue()) {
                Gui.drawRect3((float)(x2 - 2.0), (float)(y2 - 3.0), FontManager.font16.getStringWidth(displayText) + 5, (float)heightVal, new Color(0, 0, 0).getRGB());
            }

            yOffset += moduleAnimation.getOutput() * heightVal;
        }
    }

    private String formatModule(Module module) {
        String name = module.getName();
        name = name.replaceAll(" ", "");
        String formatText = "%s %s%s";
        String suffix = module.getSuffix();
        if (suffix == null || suffix.isEmpty()) {
            return name;
        }
        return String.format(formatText, new Object[]{name, EnumChatFormatting.GRAY, suffix});
    }

    private int getRainbowColor(int index, float offset) {
        float speed = 8000f;
        float hue = (System.currentTimeMillis() % (int)speed) + (index * 100) + offset * 50;
        while (hue > speed) hue -= speed;
        hue /= speed;
        return Color.HSBtoRGB(hue, 0.65f, 1.0f);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        ArrayList<Module> moduleList = new ArrayList<Module>();
        moduleList.addAll(TYClient.instance.moduleManager.getModules());
        if (this.modules == null) {
            this.modules = moduleList;
            this.modules.removeIf(module -> module.getCategory() == Category.render && (Boolean)HUD.importantModules.getValue() != false);
        }
        this.modules.sort(Comparator.<Module>comparingDouble(m -> {
            String name = m.getName() + (m.getSuffix() != "" ? " " + m.getSuffix() : "");
            return FontManager.font16.getStringWidth(name);
        }).reversed());
        double yOffset = 0.0;
        ScaledResolution sr = new ScaledResolution(mc);
        int count = 0;
        for (Module module2 : this.modules) {
            if (HUD.importantModules.getValue() && module2.getCategory() == Category.render) continue;
            if (HUD.importantModules.getValue() && module2.getCategory() == Category.better) continue;
            if (HUD.importantModules.getValue() && module2.getCategory() == Category.useful) continue;

            Animation moduleAnimation = module2.getAnimation();
            moduleAnimation.setDirection(module2.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!module2.getState() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
            String displayText = "| " + this.formatModule(module2);
            double textWidth = FontManager.font16.getStringWidth(displayText);
            double xValue = sr.getScaledWidth() - 10;
            boolean flip = xValue <= (double)((float)sr.getScaledWidth() / 2.0f);
            double x = flip ? xValue : (double)sr.getScaledWidth() - (textWidth + 3.0);
            float alphaAnimation = 1.0f;
            double y = yOffset + 4.0;
            double heightVal = (Double)HUD.height.getValue() + 1.0;

            if (flip) {
                x -= Math.abs((moduleAnimation.getOutput() - 1.0) * ((double)sr.getScaledWidth() - (2.0 - textWidth)));
                break;
            }
            x += Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + textWidth));

            if (((Boolean)HUD.background.getValue()).booleanValue()) {
                Gui.drawRect3((float)(x - 2.0), (float)(y - 3.0), FontManager.font16.getStringWidth(displayText) + 5, (float)heightVal, ColorUtil.applyOpacity(new Color(20, 20, 20), ((Double)HUD.backgroundAlpha.getValue()).floatValue() * alphaAnimation).getRGB());
            }
            if (((Boolean) HUD.hLine.getValue()).booleanValue()) {
                Gui.drawRect3((float)RenderUtil.width() - 1.0f, (float)(y - 3.0), 1.0, (float)heightVal, getRainbowColor(count, 0));
            }

            int textcolor = getRainbowColor(count, 20);
            FontManager.font16.drawStringWithShadow(displayText, (float)x, (float)(y - 1.0 + (double)FontManager.font16.getMiddleOfBox((float)heightVal)), ColorUtil.applyOpacity(textcolor, alphaAnimation));

            yOffset += moduleAnimation.getOutput() * heightVal;
            ++count;
        }
    }

    public static enum ANIM {
        MoveIn,
        ScaleIn;
    }
}