package cn.tianyu.client.gui.clickgui.oldui;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.ClickGUI;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.tianyu.client.TYClient.mc;

/**
 * @author CN_tianyu
 */

public class CategoryPanel2 {
    public int x;
    public int y;
    private Category category;

    public final int width = 120;
    public final int height = 20;

    private int prevX;
    private int prevY;
    private boolean drag;
    private boolean hovered;

    private static final Map<Category, Boolean> displayCache = new HashMap<>();
    private boolean displayMod;

    private final List<ModPanel2> modPanels = new ArrayList<>();

    public CategoryPanel2(int x, int y, Category category) {
        this.x = x;
        this.y = y;
        this.category = category;
        this.displayMod = displayCache.getOrDefault(category, true);

        for (Module mod : TYClient.moduleManager.getByCategory(category)) {
            modPanels.add(new ModPanel2(mod));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        if (drag && hovered) {
            x = mouseX + prevX;
            y = mouseY + prevY;
        }

        Color background = new Color(0, 0, 0, 48);
        RoundedUtils.drawRound(x, y, width, height, 3.0f, background);
        boolean mcfont = ClickGUI.Mcfont.getValue();
        if (mcfont) {
            mc.fontRendererObj.drawString(category.name(), x + width / 2, y + height / 2 - 5, 0xFFFFFFFF);
        } else {
            FontManager.F22.drawCenteredString(category.name(), x + width / 2, y + height / 2 - 5, 0xFFFFFFFF);
        }

        int index = 0;
        if (displayMod) {
            for (ModPanel2 it : modPanels) {
                it.x = x;
                it.y = y + height + index;
                it.drawScreen(mouseX, mouseY, partialTicks);
                index += 20;
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (hovered) {
                drag = true;
                prevX = x - mouseX;
                prevY = y - mouseY;
            }
        }

        if (mouseButton == 1) {
            if (hovered) {
                displayMod = !displayMod;
                displayCache.put(category, displayMod);
            }
        }

        if (displayMod) {
            modPanels.forEach(it -> it.mouseClicked(mouseX, mouseY, mouseButton));
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        drag = false;
        if (displayMod) {
            modPanels.forEach(it -> it.mouseReleased(mouseX, mouseY, state));
        }
    }
}