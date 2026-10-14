package cn.tianyu.client.gui.clickgui.powerx;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.ClickGUI;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.Value;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Mouse;

import java.awt.*;
import java.util.Collections;
import java.util.List;


public class PXClickGui extends GuiScreen {
    public static Category currentCategory = Category.combat;
    public static Module currentModule = !TYClient.instance.getModuleManager().getByCategory(currentCategory).isEmpty()
            ? TYClient.instance.getModuleManager().getByCategory(currentCategory).get(0)
            : null;
    public static float startX = 100, startY = 100;
    public int moduleStart;
    public int valueStart;
    boolean previousmouse = true;
    boolean mouse;
    public float moveX, moveY;
    final RapeMasterFontManager font = FontManager.arial18;
    final RapeMasterFontManager typefont = FontManager.tenacityBoldFont24;

    @SuppressWarnings("unchecked")
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (isHovered(startX, startY - 25, startX + 400, startY + 25, mouseX, mouseY) && Mouse.isButtonDown(0)) {
            if (moveX == 0 && moveY == 0) {
                moveX = mouseX - startX;
                moveY = mouseY - startY;
            } else {
                startX = mouseX - moveX;
                startY = mouseY - moveY;
            }
            previousmouse = true;
        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }

        RenderUtil.drawRoundedRect(startX - 1, startY - 1, startX + 370 + 1, startY + 230 + 1, 3F, new Color(11, 84, 150, 150).getRGB());
        RenderUtil.drawRoundedRect(startX, startY, startX + 370, startY + 230, 2F, new Color(27, 27, 27).getRGB());
        RenderUtil.drawRect(startX, startY + 20.5, startX + 370, startY + 20, new Color(21, 21, 21, 140).getRGB());
        RenderUtil.drawRect(startX, startY + 23, startX + 370, startY + 20, new Color(23, 23, 23, 130).getRGB());

        RenderUtil.drawRect(startX + 80, startY + 20.3, startX + 180, startY + 230, new Color(27, 27, 27).getRGB());

        RenderUtil.drawRect(startX + 80, startY + 20.3, startX + 80.8, startY + 230, new Color(12, 12, 12, 200).getRGB());
        RenderUtil.drawRect(startX + 80.8, startY + 20.3, startX + 81.6, startY + 230, new Color(13, 13, 13, 200).getRGB());
        RenderUtil.drawRect(startX + 81.6, startY + 20.3, startX + 82.4, startY + 230, new Color(14, 14, 14, 200).getRGB());
        RenderUtil.drawRect(startX + 82.4, startY + 20.3, startX + 83.2, startY + 230, new Color(15, 15, 15, 200).getRGB());
        RenderUtil.drawRect(startX + 83.2, startY + 20.3, startX + 84, startY + 230, new Color(16, 16, 16, 200).getRGB());
        RenderUtil.drawRect(startX + 84, startY + 20.3, startX + 84.8, startY + 230, new Color(17, 17, 17, 200).getRGB());
        RenderUtil.drawRect(startX + 84.8, startY + 20.3, startX + 85.8, startY + 230, new Color(18, 18, 18, 190).getRGB());
        RenderUtil.drawRect(startX + 85.8, startY + 20.3, startX + 90, startY + 230, new Color(20, 20, 20, 110).getRGB());
        RenderUtil.drawRect(startX + 185, startY + 20.3, startX + 182, startY + 230, new Color(21, 21, 21, 110).getRGB());
        RenderUtil.drawRect(startX + 182, startY + 20.3, startX + 180, startY + 230, new Color(21, 21, 21, 100).getRGB());
        FontManager.arial22.drawString(TYClient.instance.NAME, startX + 5, startY + 5, (new Color(152, 152, 152)).getRGB());


        for (int i = 0; i < Category.values().length; i++) {
            if (Category.values()[i] != currentCategory) {

                RenderUtil.circle(startX + 30, startY + 50 + i * 20, 6, new Color(255, 255, 255, 0).getRGB());
            }
            try {
                if (isCategoryHovered(startX + 15, startY + 40 + i * 20, startX + 60, startY + 50 + i * 20, mouseX,
                        mouseY) && Mouse.isButtonDown(0)) {
                    currentCategory = Category.values()[i];
                    currentModule = !TYClient.instance.getModuleManager().getByCategory(currentCategory).isEmpty()
                            ? TYClient.instance.getModuleManager().getByCategory(currentCategory).get(0)
                            : null;
                    moduleStart = 0;
                }
            } catch (Exception ignored) {

            }
        }
        int m = Mouse.getDWheel();

        if (isCategoryHovered(startX + 60, startY, startX + 200, startY + 320, mouseX, mouseY)) {
            if (m < 0 && moduleStart < TYClient.instance.getModuleManager().getByCategory(currentCategory).size() - 1) {
                moduleStart++;
            }
            if (m > 0 && moduleStart > 0) {
                moduleStart--;
            }
        }

        if (isCategoryHovered(startX + 200, startY, startX + 420, startY + 320, mouseX, mouseY)) {
            if (m < 0 && valueStart < currentModule.getValues().size() - 1) {
                valueStart++;
            }
            if (m > 0 && valueStart > 0) {
                valueStart--;
            }
        }
        FontManager.tenacityBoldFont20.drawString(
                currentCategory.toString(),
                startX + 85, startY + 5, new Color(152, 152, 152).getRGB());
        FontManager.tenacityBoldFont20.drawString(
                currentModule == null ? currentCategory.toString()
                        : "Module:" + currentModule.getName(),
                startX + 185, startY + 5, new Color(152, 152, 152).getRGB());
        if (currentModule != null) {
            float mY = startY + 16;
            for (int i = 0; i < TYClient.instance.getModuleManager().getByCategory(currentCategory).size(); i++) {
                Module module = TYClient.instance.getModuleManager().getByCategory(currentCategory).get(i);
                if (mY > startY + 200)
                    break;
                if (i < moduleStart) {
                    continue;
                }
                if (module.isEnabled()) {
                    RenderUtil.drawRoundedRect(startX + 88, mY + 8, startX + 175, mY + 25, 8F, new Color(58, 58, 58).getRGB());
                    RenderUtil.circle(startX + 95, mY + 16, 2, new Color(0, 100, 237).getRGB());
                } else {
                    RenderUtil.drawRoundedRect(startX + 88, mY + 8, startX + 175, mY + 25, 8F, new Color(39, 39, 39).getRGB());
                    RenderUtil.circle(startX + 95, mY + 16, 2, new Color(152, 152, 152).getRGB());
                }
                RenderUtil.startGlScissor((int) (startX), (int) (startY - 16), 165, 285);
                FontManager.tenacityBoldFont20.drawString(module.getName(), startX + 108 - 5, mY + 13, module.isEnabled() ? new Color(152, 152, 152).getRGB() : new Color(68, 68, 68).getRGB());
                RenderUtil.stopGlScissor();
                if (!module.getValues().isEmpty()) {
                    FontManager.font18.drawString("+", startX + 167, mY + 15, new Color(150, 150, 150).getRGB());
                }
                if (isSettingsButtonHovered(startX + 108, mY,
                        startX + 100 + (typefont.getStringWidth(module.getName())),
                        (float) (mY + 12 + typefont.getStringHeight()),
                        mouseX,
                        mouseY)) {
                    if (!previousmouse && Mouse.isButtonDown(0)) {
                        module.setEnable(!module.isEnabled());
                        previousmouse = true;
                    }
                    if (!previousmouse && Mouse.isButtonDown(1)) {
                        previousmouse = true;
                    }
                }

                if (!Mouse.isButtonDown(0)) {
                    previousmouse = false;
                }
                if (isSettingsButtonHovered(startX + 90, mY,
                        startX + 100 + (FontManager.tenacityBoldFont20.getStringWidth(module.getName())),
                        (float) (mY + 12 + FontManager.tenacityBoldFont20.getStringHeight()), mouseX, mouseY) && Mouse.isButtonDown(1)) {
                    currentModule = module;
                    valueStart = 0;
                }
                mY += 18;
            }
            mY = startY + 25;
            for (int i = 0; i < currentModule.getValues().size(); i++) {
                if (mY > startY + 215)
                    break;
                if (i < valueStart) {
                    continue;
                }
                Value value = currentModule.getValues().get(i);
                if (ClickGUI.Visitable.getValue()) {
                    if (!value.isVisible()) continue;
                }
                if (value instanceof NumberValue) {
                    float x = startX + 290;
                    double render = 68.0F
                            * (((Number) value.getValue()).floatValue() - ((NumberValue) value).getMin())
                            / (((NumberValue) value).getMax()
                            - ((NumberValue) value).getMin());
                    RenderUtil.drawRect(x - 6, mY + 2, (float) ((double) x + 74), mY + 3,
                            (new Color(200, 200, 200)).getRGB());
                    RenderUtil.drawRect(x - 6, mY + 2, (float) (x + render + 6.5D), mY + 3,
                            (new Color(0, 100, 237)).getRGB());
                    RenderUtil.circle((float) (x + render + 4), mY + 2, 2F, new Color(0, 100, 237).getRGB());
                    font.drawString(value.getName() + ": " + value.getValue(), startX + 190, mY, new Color(152, 152, 152).getRGB());
                    if (!Mouse.isButtonDown(0)) {
                        previousmouse = false;
                    }
                    if (isButtonHovered(x, mY - 2, x + 100, mY + 7, mouseX, mouseY)
                            && Mouse.isButtonDown(0)) {
                        if (!previousmouse && Mouse.isButtonDown(0)) {
                            render = ((NumberValue) value).getMin();
                            double max = ((NumberValue) value).getMax();
                            double inc = ((NumberValue) value).getInc();
                            double valAbs = mouseX - (x + 1.0D);
                            double perc = valAbs / 68.0D;
                            perc = Math.min(Math.max(0.0D, perc), 1.0D);
                            double valRel = (max - render) * perc;
                            double val = render + valRel;
                            val = Math.round(val * (1.0D / inc)) / (1.0D / inc);
                            value.setValue(val);
                        }
                        if (!Mouse.isButtonDown(0)) {
                            previousmouse = false;
                        }
                    }
                    mY += 20;
                }
                if (value instanceof BooleanValue) {
                    float x = startX + 230;
                    font.drawString(value.getName(), startX + 190, mY, new Color(152, 152, 152).getRGB());
                    if ((boolean) value.getValue()) {
                        RenderUtil.drawRoundedRect(x + 56, mY - 1, x + 76, mY + 9, 4F, new Color(58, 58, 58).getRGB());
                        RenderUtil.circle(x + 72, mY + 4, 4, new Color(0, 100, 237).getRGB());
                    } else {

                        RenderUtil.drawRoundedRect(x + 56, mY - 1, x + 76, mY + 9, 4F, new Color(58, 58, 58).getRGB());
                        RenderUtil.circle(x + 60, mY + 4, 4, new Color(152, 152, 152).getRGB());
                    }
                    if (isCheckBoxHovered(x + 56, mY, x + 76, mY + 9, mouseX, mouseY)) {
                        if (!previousmouse && Mouse.isButtonDown(0)) {
                            previousmouse = true;
                            mouse = true;
                        }

                        if (mouse) {
                            value.setValue(!(boolean) value.getValue());
                            mouse = false;
                        }
                    }
                    if (!Mouse.isButtonDown(0)) {
                        previousmouse = false;
                    }
                    mY += 20;
                }
                if (value instanceof ModeValue) {
                    ModeValue mo = (ModeValue) value;
                    float x = startX + 275;
                    font.drawStringWithShadow(value.getName(), startX + 190, mY, new Color(152, 152, 152).getRGB());
                    RenderUtil.drawRect(x + 20, mY, x + 65, mY + 10,
                            new Color(120, 120, 120).getRGB());

                    RenderUtil.drawRect(x + 5, mY, x + 15, mY + 10,
                            new Color(0, 100, 237).getRGB());
                    RenderUtil.drawRect(x + 70, mY, x + 80, mY + 10,
                            new Color(0, 100, 237).getRGB());
                    font.drawStringWithShadow("<", x + 8 - 1, mY + 3, new Color(152, 152, 152).getRGB());
                    font.drawStringWithShadow(">", x + 72 + 1, mY + 3, new Color(152, 152, 152).getRGB());
                    RenderUtil.startGlScissor((int) (x + 20), (int) mY, (int) (x + 65), (int) (mY + 10));
                    font.drawStringWithShadow(((ModeValue) value).getModeAsString(),
                            x + 40 - font.getStringWidth(((ModeValue) value).getModeAsString()) / 2, mY + 2.5f, -1);
                    RenderUtil.stopGlScissor();
                    if (isStringHovered(x, mY - 5, x + 100, mY + 15, mouseX, mouseY)) {
                        if (!previousmouse) {
                            if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1))) {
                                List<String> options = Collections.singletonList(mo.getModes().toString());
                                //noinspection SuspiciousMethodCalls
                                int index = options.indexOf(mo.getValue());
                                if (Mouse.isButtonDown(0)) {
                                    index++;
                                } else {
                                    index--;
                                }
                                if (index >= options.size()) {
                                    index = 0;
                                } else if (index < 0) {
                                    index = options.size() - 1;
                                }
                                //DebugUtil.log("step4");
                                mo.setValue(mo.getModes().get(index));
                                DebugUtil.log(mo.getModes().get(index));
                            }
                            previousmouse = true;
                        }
                        if (!Mouse.isButtonDown(0)) {
                            previousmouse = false;
                        }

                    }
                    mY += 25;
                }
            }

            RenderUtil.startGlScissor((int) (startX), (int) (startY - 16), 165 - 87, 285);
            float typeY = 0;
            Category category;
            for (int i = 0; i < Category.values().length; i++) {
                category = Category.values()[i];
                typefont.drawString(category.name(), startX + 25, startY + 40 + typeY,
                        category == currentCategory ? new Color(0, 100, 237).getRGB() : new Color(152, 152, 152).getRGB());
                typeY += 20;
            }
            FontManager.GuiICONS28.drawString("G", startX + 7, startY + 140,
                    currentCategory.name().equals("Misc") ? new Color(0, 100, 237).getRGB() : new Color(152, 152, 152).getRGB());
            FontManager.GuiICONS28.drawString("J", startX + 7, startY + 160,
                    currentCategory.name().equals("Globals") ? new Color(0, 100, 237).getRGB() : new Color(152, 152, 152).getRGB());
            FontManager.tenacityBoldFont25.drawString("G", startX + 7, startY + 180,
                    currentCategory.name().equals("Script") ? new Color(0, 100, 237).getRGB() : new Color(152, 152, 152).getRGB());
            final int typeWidth = -4;
            switch (currentCategory.name()) {
                case "combat": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/" + currentCategory + 2 + ".png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Render.png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Movement.png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Player.png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/World.png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
                case "render": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Combat.png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/" + currentCategory + 2 + ".png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Movement.png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Player.png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/World.png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
                case "movement": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Combat.png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Render.png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/" + currentCategory + 2 + ".png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Player.png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/World.png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
                case "player": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Combat.png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Render.png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Movement.png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/" + currentCategory + 2 + ".png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/World.png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
                case "world": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Combat.png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Render.png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Movement.png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Player.png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/" + currentCategory + 2 + ".png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
                case "misc": {
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Combat.png"), (int) startX + 5, (int) startY + 40 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Render.png"), (int) startX + 5, (int) startY + 80 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Movement.png"), (int) startX + 5, (int) startY + 60 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/Player.png"), (int) startX + 5, (int) startY + 100 + typeWidth, 14, 14);
                    RenderUtil.drawImage(new ResourceLocation("client/clickgui/Clickui/World.png"), (int) startX + 5, (int) startY + 120 + typeWidth, 14, 14);
                    break;
                }
            }
            RenderUtil.stopGlScissor();
        }

    }

    public static boolean isStringHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public static boolean isSettingsButtonHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public static boolean isButtonHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public static boolean isCheckBoxHovered(float f, float y, float g, float y2, int mouseX, int mouseY) {
        return mouseX >= f && mouseX <= g && mouseY >= y && mouseY <= y2;
    }

    public static boolean isCategoryHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    public static boolean isHovered(float x, float y, float x2, float y2, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
