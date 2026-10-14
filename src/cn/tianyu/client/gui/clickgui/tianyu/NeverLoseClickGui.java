package cn.tianyu.client.gui.clickgui.tianyu;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.AnimationUtil;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.HSBData;
import cn.tianyu.client.util.impl.render.*;
import cn.tianyu.client.util.impl.render.Rectangle;
import cn.tianyu.client.value.Value;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NeverLoseClickGui extends GuiScreen {
    public static NeverLoseClickGui INSTANCE = new NeverLoseClickGui();
    private short[] date;
    public float x = 4, y = 20;
    public float width = 520;
    public final float height = 420;
    public float search = 300;
    public float visibleAnimation;
    private boolean quitting = false;

    private boolean dragging;
    private float dragX, dragY;
    public int wheel = Mouse.hasWheel() ? Mouse.getDWheel() * 2 : 0;

    private List<Module> leftModules = new CopyOnWriteArrayList<>();
    private List<Module> rightModules = new CopyOnWriteArrayList<>();

    private List[] lists = new List[]{};

    public NumberFormat nf = new DecimalFormat("0000");
    private Category.Pages current = Category.Pages.COMBAT;
    private NumberValue currentSliding = null;

    private Value<?> dropdownItem;
    private Rectangle protectArea;

    private final InputField searchTextField = new InputField(FontManager.font16);
    private boolean searching = false;

    private final float[] moduleWheel = {0f, 0f};

    private float alphaAnimate = 10;

    private String tooltip = null;
    private float offsetY = 0;
    private boolean mouseDown = false;

    private final ArrayList<ItemStack> itemStacks = new ArrayList<>();

    // 记录每个模块的展开状态（默认展开）
    private final Map<Module, Boolean> moduleExpandState = new HashMap<>();
    // 平滑展开/收起的动画值
    private final Map<Module, Float> moduleExpandAnimation = new HashMap<>();

    public NeverLoseClickGui() {
        INSTANCE = this;
        dropdownItem = null;
        protectArea = null;

        init();
    }

    public void init() {
        font = FontManager.museo18;
        for (Item item : Item.itemRegistry) {
            itemStacks.add(new ItemStack(item));
        }
    }

    private float scrollAni;
    private float CscrollAni;

    private static RapeMasterFontManager font;


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tooltip = null;

        visibleAnimation = AnimationUtil.animateSmooth(visibleAnimation, quitting ? 0 : 100, .2F);
        if (quitting) {
            if (Display.isActive() && !mc.inGameHasFocus) {
                mc.inGameHasFocus = true;
                mc.mouseHelper.grabMouseCursor();
            }

            if (Math.round(visibleAnimation) <= 2) mc.displayGuiScreen(null);
        }

        if (!Mouse.isButtonDown(0) && dragging) dragging = false;
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
        if (!quitting) {
            ShaderElement.addBlurTask(() -> {
                RoundedUtils.drawRound(x, y, width, height, 6, true, new Color(0, 0, 0));
            });
            RoundedUtils.drawRound(x, y, width, height, 6, false, new Color(0, 0, 0, 200));
        }
        float width = Math.max(FontManager.bold38.getStringWidth(TYClient.NAME.toUpperCase()), FontManager.bold38.getStringWidth("NOVOLINE"));
        if (width > FontManager.bold38.getStringWidth("NOVOLINE")) {
            this.width = AnimationUtil.animateSmooth(this.width, 520 + width - FontManager.bold38.getStringWidth("NOVOLINE"), 0.5f);
        } else {
            this.width = AnimationUtil.animateSmooth(this.width, 520, 0.5f);
        }
        FontManager.bold38.drawCenteredString(TYClient.NAME.toUpperCase(), (float) (x + 7.7 + width / 2), y + 12, new Color(255, 255, 255).getRGB());
        float pageY = 44;
        MaskUtil.defineMask();
        RenderUtil.drawRectWH(x, y + pageY - 4, width + 10, 344, new Color(0, 0, 0).getRGB());
        MaskUtil.finishDefineMask();
        MaskUtil.drawOnMask();
        float sb = 0;
        for (Category pageManager : Category.values()) {
            sb += 12;
            for (Category.Pages ignored : pageManager.getSubPages()) {
                sb += 26;
            }
            sb += 4;
        }
        CscrollY = Math.max(CscrollY, -sb + 344);
        if (RenderUtil.isHovering(x, y + pageY, width + 10, 340, mouseX, mouseY)) {
            CscrollAni = AnimationUtil.animateSmooth(CscrollAni, CscrollY, 0.3f);
        } else {
            CscrollY = CscrollAni;
        }
        pageY += CscrollAni;

        pageY += 12;
        for (Category.Pages cate : Category.Pages.values()) {
            String head = StringUtils.left(cate.name(), 1);
            String display = cate.name().substring(1);

            if (cate == current) {
                RoundedUtils.drawRound(x + 8, y + pageY, width, 16, 4, true, new Color(50, 50, 50));
            }

            float finalPageY = pageY;
            cate.animation.draw(() -> RoundedUtils.drawRound(x + 8, y + finalPageY, width, 16, 4, true, getColor(7, 50, 74, 230)));
            try {
                RenderUtil.drawImage(new ResourceLocation("xylitol/images/" + cate.name().toLowerCase() + ".png"), x + 10, y + pageY + 2, 12, 12, getColor(3, 168, 245).getRGB());
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            String text = head + display.toLowerCase();
            text = text.replaceAll("_", " ");
            if (cate.gradient) {
                float finalPageY1 = pageY;
                String finalText = text;
                GlStateManager.pushMatrix();
                GradientUtil.applyGradientHorizontal(x + 26, y + finalPageY1 + 4.5f, font.getStringWidth(text), font.getHeight(), Math.round((visibleAnimation / 100F)), getColor(HUD.color(1)), getColor(HUD.color(89)), () -> {
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
                    font.drawString(finalText, x + 26, y + finalPageY1 + 4.5f, new Color(255, 255, 255).getRGB());
                });
                GlStateManager.popMatrix();
            } else {
                font.drawString(text, x + 26, y + pageY + 5f, new Color(255, 255, 255).getRGB());
            }
            pageY += 26;
        }

        MaskUtil.resetMask();

        StencilUtil.initStencilToWrite();
        RoundedUtils.drawRound(x, y, this.width, height, 6, true, new Color(0, 0, 0));
        StencilUtil.readStencilBuffer(1);
        ShaderElement.addBlurTask(() -> RenderUtil.drawRectWH(x + width + 8 + 8, y, this.width - width - 16, height, new Color(0,0,0).getRGB()));
        RenderUtil.drawRectWH(x + width + 8 + 8, y, this.width - width, height, new Color(0, 0, 0,200).getRGB());
        Gui.drawRect(x + width + 8 + 8, y + 40, this.x + this.width, y + 41, new Color(100, 100, 100).getRGB());
        StencilUtil.endStencilBuffer();

        if (current == Category.Pages.CONFIGS) {
            boolean hovered = RenderUtil.isHovering(x + width + 16 + 4, y + 10, 89, 20, mouseX, mouseY);
            RoundedUtils.drawRoundOutline(x + width + 16 + 4, y + 10, 89, 20, 4, 0.1f, new Color(100, 100, 100), hovered ? getColor(new Color(0xFF00A7F2)) : new Color(100, 100, 100));
            font.drawCenteredString("Create Config", x + width + 16 + 4 + (89 / 2f), y + 17, new Color(255,255,255).getRGB());
            boolean hovered2 = RenderUtil.isHovering(x + width + 16 + 96, y + 10, 89, 20, mouseX, mouseY);
            RoundedUtils.drawRoundOutline(x + width + 16 + 96, y + 10, 89, 20, 4, 0.1f, new Color(100, 100, 100), hovered2 ? getColor(new Color(0xFF00A7F2)) : new Color(100, 100, 100));
            font.drawCenteredString("Open directory", x + width + 16 + 96 + (89 / 2f), y + 17, new Color(255,255,255).getRGB());
        } else {
            RoundedUtils.drawRoundOutline(x + width + 16 + 10, y + 10, search, 20, 4, 0.1f, new Color(80, 80, 80), new Color(100, 100, 100));

            if (!searching)
                RenderUtil.drawImage(new ResourceLocation("xylitol/images/search.png"), x + width + 16 + 10 + 3, y + 13, 14, 14, new Color(150, 150, 150, 100).getRGB());

            searchTextField.setBackgroundText("Click this to search with in gui..");
            searchTextField.setDrawingLine(false);
            searchTextField.setxPosition(x + width + 16 + 10 + 3 + (searching ? 0 : 16));
            searchTextField.setyPosition(y + 13);
            searchTextField.setWidth(search);
            searchTextField.setHeight(20);
            searchTextField.setDrawingBackground(false);
            searchTextField.drawTextBox(mouseX, mouseY);
        }

        StencilUtil.initStencilToWrite();
        RenderUtil.drawRectWH(x, y + 45, this.width, height - 46, new Color(0, 0, 0).getRGB());
        StencilUtil.readStencilBuffer(1);
        wheel = Mouse.hasWheel() ? Mouse.getDWheel() * 12 : 0;

        if (current.module) {
            // 双列渲染
            float leftHeight = render(leftModules, x + width + 24, mouseX, mouseY);
            float rightHeight = render(rightModules, x + width + 24 + 200, mouseX, mouseY);

            float maxHeight = Math.max(leftHeight, rightHeight);
            final float[] nextWheel = RenderUtil.getNextWheelPosition(wheel, moduleWheel, y + 10, y + 290, maxHeight, 0, RenderUtil.isHovering(x + 120, y + 40, this.width - 120, this.height - 40, mouseX, mouseY));
            moduleWheel[0] = nextWheel[0];
            moduleWheel[1] = maxHeight > this.height ? Math.max(nextWheel[1], -maxHeight + this.height - 60) : nextWheel[1];
        } else {
            if (current == Category.Pages.CONFIGS) {
                TYClient styles = TYClient.instance;
                float configY = y + 40 + 8 + 14 + scrollAni;
                FontManager.font16.drawString("- My Items", x + width + 16 + 6, configY - 14, new Color(255,255,255).getRGB());

                float naotan = styles.configManager.getConfigs().size() * 42;
                if (naotan > this.height - 60) {
                    scrollY = Math.max(scrollY, -naotan + this.height - 60);
                    if (RenderUtil.isHovering(x + width + 16, 0, 370, 420, mouseX, mouseY)) {
                        scrollAni = AnimationUtil.animateSmooth(scrollAni, scrollY*2, 0.2f);
                    } else {
                        scrollY = scrollAni;
                    }
                }

                for (String config : styles.configManager.getConfigs()) {
                    RoundedUtils.drawRoundOutline(x + width + 16 + 6, configY, 370, 36, 4, 0.1f, new Color(80, 80, 80), new Color(100, 100, 100));
                    FontManager.font18.drawString(config, x + width + 16 + 12, configY + 6, new Color(255,255,255).getRGB());
                    FontManager.font18.drawString(EnumChatFormatting.GRAY + "Modified: " + EnumChatFormatting.RESET + "2077/7/7" + " " + EnumChatFormatting.GRAY + "Author: " + EnumChatFormatting.RESET + "null", x + width + 16 + 12, configY + 8 + 12, new Color(3, 146, 214).getRGB());
                    boolean fuck = RenderUtil.isHovering(x + width + 16 + 6 + 290 - 6, configY + 8, 76, 20, mouseX, mouseY);
                    if ("modules.json".equals(config)) {
                        RoundedUtils.drawRoundOutline(x + width + 16 + 6 + 290 - 6, configY + 8, 76, 20, 4, 0.1f, new Color(100, 100, 100), fuck ? getColor(new Color(0xFF00A7F2)) : new Color(100, 100, 100));
                        RenderUtil.drawImage(new ResourceLocation("xylitol/images/save.png"), x + width + 16 + 6 + 290 + 10, configY + 8 + 2, 16, 16, new Color(201, 201, 201).getRGB());
                        FontManager.font16.drawString("Save", x + width + 16 + 6 + 290 + 30, configY + 8 + 8, new Color(255,255,255).getRGB());
                    } else {
                        RoundedUtils.drawRound(x + width + 16 + 6 + 290 - 6, configY + 8, 76, 20, 4, false, fuck ? getColor(new Color(0xFF00A7F2)) : new Color(0x1D82AF));
                        RenderUtil.drawImage(new ResourceLocation("xylitol/images/read.png"), x + width + 16 + 6 + 290 + 10, configY + 8 + 2, 16, 16, fuck ? -1 : new Color(201, 201, 201).getRGB());
                        FontManager.font16.drawString("Load", x + width + 16 + 6 + 290 + 30, configY + 8 + 8, fuck ? -1 : new Color(255,255,255).getRGB());
                    }
                    configY += 42;
                }
            }
        }

        StencilUtil.endStencilBuffer();

        RenderUtil.drawRectWH(x, y + height - 30, width + 16, 0.5f, new Color(100, 100, 100).getRGB());

        GlStateManager.disableTexture2D();
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.enableTexture2D();

        GlStateManager.disableTexture2D();
        GlStateManager.color(1f, 1f, 1f);
        RenderUtil.drawCircleCGUI(x + 16, y + height - 15, 26, new Color(0, 255, 255).getRGB());

        GlStateManager.disableTexture2D();
        font.drawString(DebugUtil.getUsername(), x + 34, y + height - 30 + 6, new Color(255,255,255).getRGB());
        font.drawString(EnumChatFormatting.GRAY+"TYClient's ", x + 34, y + height - 30 + 18, new Color(3, 168, 245).getRGB());
        font.drawString(DebugUtil.isDev() ? "Dev" : "User", x + 34 +font.getStringWidth("TYClient's "), y + height - 30 + 18, new Color(3, 168, 245).getRGB());

        alphaAnimate = AnimationUtil.animateSmooth(alphaAnimate, 180, 0.4f);

        if (dropdownItem != null && protectArea != null) {
            if (dropdownItem instanceof ModeValue) {
                final ModeValue property = (ModeValue) dropdownItem;
                property.animation = AnimationUtil.animateSmooth(property.animation, 255, 0.5f);
                RoundedUtils.drawRound(protectArea.getX(), protectArea.getY(), protectArea.getWidth(), protectArea.getHeight() + 1, 4, new Color(0, 0, 0, (int) property.animation));
                int buttonY = 0;
                for (String s : property.getModes()) {
                    font.drawString(s, protectArea.getX() + 3, protectArea.getY() + buttonY + 6, !property.is(s) ? new Color(200, 200, 200, (int) property.animation).getRGB() : new Color(255, 255, 255, (int) property.animation).getRGB());
                    buttonY += 14;
                }
            }
            if (dropdownItem instanceof ColorValue) {
                ColorValue cp = (ColorValue) dropdownItem;
                final Color valColor = cp.getColorC();
                HSBData hsbData = new HSBData(valColor);
                final float[] hsba = {
                        hsbData.getHue(),
                        hsbData.getSaturation(),
                        hsbData.getBrightness(),
                        hsbData.getAlpha(),
                };

                RoundedUtils.drawRoundOutline(protectArea.getX(), protectArea.getY(), protectArea.getWidth(), protectArea.getHeight() + 1, 2, 0.1F, new Color(5, 16, 26), new Color(100, 100, 100));
                RenderUtil.drawRectWH(protectArea.getX() + 3, protectArea.getY() + 3, 61, 61, new Color(0, 0, 0).getRGB());
                RenderUtil.drawRectWH(protectArea.getX() + 3.5, protectArea.getY() + 3.5, 60, 60, new Color(Color.getHSBColor(hsba[0], 1, 1).getRGB()).getRGB());
                RenderUtil.drawHGradientRect(protectArea.getX() + 3.5, protectArea.getY() + 3.5, 60, 60, new Color(0,0,0,0).getRGB(), 0x00F);
                RenderUtil.drawVGradientRect(protectArea.getX() + 3.5, protectArea.getY() + 3.5, 60, 60, 0x00F, new Color(Color.getHSBColor(hsba[0], 1, 0).getRGB()).getRGB());

                RenderUtil.drawRectWH(protectArea.getX() + 3.5 + hsba[1] * 60 - .5, protectArea.getY() + 3.5 + ((1 - hsba[2]) * 60) - .5, 1.5, 1.5, new Color(0, 0, 0).getRGB());
                RenderUtil.drawRectWH(protectArea.getX() + 3.5 + hsba[1] * 60, protectArea.getY() + 3.5 + ((1 - hsba[2]) * 60), .5, .5, new Color(valColor.getRGB()).getRGB());

                final boolean onSB = RenderUtil.isHovering(protectArea.getX() + 3, protectArea.getY() + 3, 61, 61, mouseX, mouseY);
                if (onSB && Mouse.isButtonDown(0)) {
                    hsbData.setSaturation(Math.min(Math.max((mouseX - protectArea.getX() - 3) / 60F, 0), 1));
                    hsbData.setBrightness(1 - Math.min(Math.max((mouseY - protectArea.getY() - 3) / 60F, 0), 1));
                    cp.setColor(hsbData.getAsColor().getRGB());
                }

                RenderUtil.drawRectWH(protectArea.getX() + 67, protectArea.getY() + 3, 10, 61, new Color(0, 0, 0).getRGB());
                for (float f = 0F; f < 5F; f += 1F) {
                    final Color lasCol = Color.getHSBColor(f / 5F, 1F, 1F);
                    final Color tarCol = Color.getHSBColor(Math.min(f + 1F, 5F) / 5F, 1F, 1F);
                    RenderUtil.drawVGradientRect(protectArea.getX() + 67.5, protectArea.getY() + 3.5 + f * 12, 9, 12, new Color(lasCol.getRGB()).getRGB(), new Color(tarCol.getRGB()).getRGB());
                }

                RenderUtil.drawRectWH(protectArea.getX() + 67.5, protectArea.getY() + 2 + hsba[0] * 60, 9, 2, new Color(0, 0, 0).getRGB());
                RenderUtil.drawRectWH(protectArea.getX() + 67.5, protectArea.getY() + 2.5 + hsba[0] * 60, 9, 1, new Color(204, 198, 255).getRGB());

                final boolean onHue = RenderUtil.isHovering(protectArea.getX() + 67, protectArea.getY() + 3, 10, 61, mouseX, mouseY);
                if (onHue && Mouse.isButtonDown(0)) {
                    hsbData.setHue(Math.min(Math.max((mouseY - protectArea.getY() - 3) / 60F, 0), 1));
                    cp.setColor(hsbData.getAsColor().getRGB());
                    cp.setRainbowEnabled(false);
                }

                if (cp.isAlphaChangeable()) {
                    RenderUtil.drawRectWH(protectArea.getX() + 3, protectArea.getY() + 67, 61, 9, new Color(0, 0, 0).getRGB());
                    for (int xPosition = 0; xPosition < 30; xPosition++)
                        for (int yPosition = 0; yPosition < 4; yPosition++)
                            RenderUtil.drawRectWH(protectArea.getX() + 3.5 + (xPosition * 2), protectArea.getY() + 67.5 + (yPosition * 2), 2, 2, ((yPosition % 2 == 0) == (xPosition % 2 == 0)) ? new Color(255, 255, 255).getRGB() : new Color(190, 190, 190).getRGB());

                    RenderUtil.drawHGradientRect(protectArea.getX() + 3.5, protectArea.getY() + 67.5, 60, 8, 0x00F, new Color(Color.getHSBColor(hsba[0], 1, 1).getRGB()).getRGB());
                    RenderUtil.drawRectWH(protectArea.getX() + 2.5 + hsba[3] * 60, protectArea.getY() + 67.5, 2, 8, new Color(0, 0, 0).getRGB());
                    RenderUtil.drawRectWH(protectArea.getX() + 3 + hsba[3] * 60, protectArea.getY() + 67.5, 1, 8, new Color(204, 198, 255).getRGB());

                    final boolean onAlpha = RenderUtil.isHovering(protectArea.getX() + 3, protectArea.getY() + 67, 61, 9, mouseX, mouseY);
                    if (onAlpha && Mouse.isButtonDown(0)) {
                        hsbData.setAlpha(Math.min(Math.max((mouseX - protectArea.getX() - 3) / 60F, 0), 1));
                    }
                }
            }
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            ShaderElement.addBlurTask(() -> RoundedUtils.drawRound(mouseX + 6, mouseY + 6, font.getStringWidth(findLongestString(tooltip.split("\n"))) + 10, tooltip.split("\n").length * 14 + (tooltip.split("\n").length == 1 ? 0 : 4), 2, true, new Color(10, 19, 30, 255)));
            RoundedUtils.drawRound(mouseX + 6, mouseY + 6, font.getStringWidth(findLongestString(tooltip.split("\n"))) + 10, tooltip.split("\n").length * 14 + (tooltip.split("\n").length == 1 ? 0 : 4), 2, false, new Color(0, 0, 0, 30));
            RoundedUtils.drawRound(mouseX + 6, mouseY + 6, font.getStringWidth(findLongestString(tooltip.split("\n"))) + 10, tooltip.split("\n").length * 14 + (tooltip.split("\n").length == 1 ? 0 : 4), 2, true, new Color(10, 19, 30, 100));
            float y = 5;
            for (String s : tooltip.split("\n")) {
                font.drawString(s, mouseX + 6 + 4, mouseY + 6 + y, new Color(255,255,255).getRGB());
                if (tooltip.split("\n").length != 1)
                    y += 14;
            }
        }
    }

    protected boolean check(double x, double y, double x2, double y2, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    private boolean checkClick() {
        if (!mouseDown && Mouse.isButtonDown(0)) {
            mouseDown = true;
            return true;
        }
        return false;
    }

    public static double round(final double value, final double inc) {
        if (inc == 0.0) return value;
        else if (inc == 1.0) return Math.round(value);
        else {
            final double halfOfInc = inc / 2.0;
            final double floored = Math.floor(value / inc) * inc;
            if (value >= floored + halfOfInc)
                return new BigDecimal(Math.ceil(value / inc) * inc).doubleValue();
            else return new BigDecimal(floored).doubleValue();
        }
    }

    private float render(List<Module> modules, float offset, int mouseX, int mouseY) {
        float moduleY = 0 + moduleWheel[1];

        for (Module module : modules) {
            // 初始化展开状态（默认展开）和动画值
            if (!moduleExpandState.containsKey(module)) {
                moduleExpandState.put(module, true);
                moduleExpandAnimation.put(module, 1.0f);
            }

            // 更新平滑展开/收起动画
            float targetAnimation = moduleExpandState.get(module) ? 1.0f : 0.0f;
            float currentAnimation = AnimationUtil.animateSmooth(moduleExpandAnimation.get(module), targetAnimation, 0.1f);
            moduleExpandAnimation.put(module, currentAnimation);

            FontManager.font16.drawString(module.getName().toUpperCase(), offset + 4, y + 50 + moduleY, new Color(255,255,255).getRGB());

            // 计算模块高度（根据动画值动态调整）
            int baseHeight = 16;
            int expandHeight = 0;
            for (Value<?> property : module.getValues()) {
                if (property.isAvailable()) expandHeight += property.getHeight();
            }
            int predictionHeight = baseHeight + (int)(expandHeight * currentAnimation);

            // 绘制模块背景
            RoundedUtils.drawRound(offset, y + 46 + FontManager.font16.getHeight() + 4 + moduleY, 190, predictionHeight, 4, false, new Color(0, 0, 0,50));

            moduleY += 10;

            // 绘制开关状态
            font.drawString("Enabled", offset + 4, y + 50 + moduleY + 6, module.getState() ? new Color(255,255,255).getRGB() : new Color(200,200,200).getRGB());
            RoundedUtils.drawRoundOutline(offset + 162, 2f+y + 48 + moduleY + 5, 23 , 12, 6, 0.1f, module.getState() ? getColor(3, 168, 245) : new Color(80, 80, 80), new Color(100, 100, 100));

            if (RenderUtil.isHovering(offset + 168, y + 50 + moduleY + 5, 16, 8, mouseX, mouseY))
                tooltip = "";

            // 开关动画速度减慢
            module.cGUIAnimation = AnimationUtil.animateSmooth(module.cGUIAnimation, module.getState() ? 10 : 0, 0.1f);
            RenderUtil.drawImage(new ResourceLocation("xylitol/images/shadow.png"),offset + 160 + module.cGUIAnimation, 2f+y + 44 + moduleY + 9,16,16);
            RenderUtil.drawCircleCGUI(offset + 168 + module.cGUIAnimation, 2f+y + 50 + moduleY + 9, 10, new Color(255,255,255).getRGB());

            moduleY += 18;

            // 根据动画值绘制展开的value列表
            if (currentAnimation > 0.01f) {
                // 保存当前透明度
                float originalAlpha = GL11.glGetFloat(GL11.GL_ALPHA);
                GL11.glColor4f(1, 1, 1, currentAnimation);

                for (Value<?> property : module.getValues()) {
                    if (!property.isAvailable()) continue;

                    if (property instanceof BooleanValue) {
                        final BooleanValue bp = (BooleanValue) property;
                        font.drawString(property.getName(), offset + 4, y + 50 + moduleY + 6 + 2f, bp.get() ? new Color(255,255,255).getRGB() : new Color(200,200,200).getRGB());
                        RoundedUtils.drawRoundOutline(offset + 162, 2f+y + 48 + moduleY + 5, 23 , 12, 6, 0.1f, bp.get() ? getColor(3, 168, 245) : new Color(80, 80, 80), new Color(100, 100, 100));
                        // 布尔值动画速度减慢
                        property.animation = AnimationUtil.animateSmooth(property.animation, bp.get() ? 10 : 0, 0.1f);
                        RenderUtil.drawImage(new ResourceLocation("xylitol/images/shadow.png"),offset + 160 + property.animation, 2f+y + 44 + moduleY + 9,16,16);
                        RenderUtil.drawCircleCGUI(offset + 168 + property.animation, 2f+y + 50 + moduleY + 9, 10, new Color(255,255,255).getRGB());
                    }

                    if (property instanceof ColorValue) {
                        ColorValue cp = (ColorValue) property;
                        font.drawString(property.getName(), offset + 4, y + 50 + moduleY + 6 + 2f, new Color(200,200,200).getRGB());
                        RenderUtil.drawCircleCGUI(offset + 175, 2f+y + 50 + moduleY + 9, 11, new Color(cp.getValue().getRGB()).getRGB());

                        if (dropdownItem == cp) {
                            protectArea = new Rectangle(offset + 100, 2f+y + moduleY + 50 + 24, 80, cp.isAlphaChangeable() ? 80 : 67);
                        }
                    }

                    if (property instanceof NumberValue) {
                        DecimalFormat df = new DecimalFormat("#.#");
                        final NumberValue dp = (NumberValue) property;
                        String display = String.valueOf(dp.getValue());
                        if (display.endsWith(".0")) display = display.substring(0, display.length() - 2);
                        else if (display.startsWith("0.")) display = "." + display.substring(2);
                        else if (display.startsWith("-0.")) display = "-" + display.substring(2);

                        font.drawString(property.getName(), offset + 4, y + 50 + moduleY + 6 + 2f, dp.sliding ? new Color(255,255,255).getRGB() : new Color(200,200,200).getRGB());
                        FontManager.font14.drawCenteredString(display, offset + 190 - 13, 2f+y + 50 + moduleY + 6, new Color(145, 166, 179).getRGB());
                        RoundedUtils.drawRound(offset + 96, 2f+y + 50 + moduleY + 8, 70, 2, 2, true, new Color(100,100,100));

                        final double ratio = (dp.getValue() - dp.getMin()) / (dp.getMax() - dp.getMin());
                        int displayLength = (int) (ratio * 70);
                        displayLength = Math.min(displayLength, 70);

                        // 数轴动画速度减慢，确保每次操作都可见
                        dp.animatedPercentage = AnimationUtil.animateSmooth(dp.animatedPercentage, displayLength, 0.05f);
                        RoundedUtils.drawRound(offset + 92, 2f+y + 50 + moduleY + 8, dp.animatedPercentage, 2, 2, true, getColor(3, 168, 245));

                        // 滑块动画速度减慢
                        dp.animation = AnimationUtil.animateSmooth(dp.animation, dp.sliding ? 10 : 8, 0.1f);
                        RenderUtil.drawImage(new ResourceLocation("xylitol/images/shadow.png"),84 + offset + dp.animatedPercentage, 2f+y + 42 + moduleY + 9,16,16);
                        RenderUtil.drawCircleCGUI(92 + offset + dp.animatedPercentage, 2f+y + 50 + moduleY + 9, dp.animation, getColor(3, 168, 245).getRGB());

                        if (dp.sliding) {
                            double num = Math.max(dp.getMin(), Math.min(dp.getMax(), round((mouseX - (offset + 92)) * (dp.getMax() - dp.getMin()) / 70 + dp.getMin(), dp.getInc())));
                            num = (double) Math.round(num * (1.0D / dp.getInc())) / (1.0D / dp.getInc());
                            dp.setValue(num);
                        }
                    }

                    if (property instanceof ModeValue) {
                        final ModeValue sp = (ModeValue) property;
                        sp.height = 24f;
                        font.drawString(property.getName(), offset + 4, y + 50 + moduleY + 9 + 1f, new Color(200,200,200).getRGB());
                        RoundedUtils.drawRoundOutline(offset + 100, y + 50 + moduleY + 3.5F, 80, 16, 4, 0.1f, new Color(80,80,80), new Color(100,100,100));
                        MaskUtil.defineMask();
                        RoundedUtils.drawRoundOutline(offset + 100, y + 50 + moduleY + 3.5F, 80, 16, 4, 0.1f, new Color(80,80,80), new Color(100,100,100));
                        MaskUtil.finishDefineMask();
                        MaskUtil.drawOnMask();
                        try {
                            font.drawString(sp.get(), offset + 104, y + 50 + moduleY + 9, new Color(200,200,200).getRGB());
                        } catch (Exception e) {
                        }
                        MaskUtil.resetMask();

                        float spmaxWidth = 0;
                        for (String s : sp.getModes()) {
                            float f = font.getStringWidth(s) + 12;
                            if (f > spmaxWidth) {
                                spmaxWidth = f;
                            }
                        }

                        if (dropdownItem == sp) {
                            protectArea = new Rectangle(offset + 100, y + moduleY + 50 + 24, spmaxWidth > 80 ? spmaxWidth : 80, 14 * sp.getModes().size());
                        }
                    }

                    moduleY += property.getHeight();

                    final List<Value<?>> visible = new ArrayList<>(module.getValues());
                    visible.removeIf(Value::isHidden);

                    if (visible.indexOf(property) != visible.size() - 1) {
                        RenderUtil.drawRectWH(offset + 4, y + 50 + moduleY, 190 - 8, .5, new Color(100,100,100).getRGB());
                    }
                }

                // 恢复透明度
                GL11.glColor4f(1, 1, 1, originalAlpha);
            }

            moduleY += 4;
        }

        return moduleY - moduleWheel[1];
    }

    public void setQuitting(boolean quitting) {
        this.quitting = quitting;
    }

    public boolean isOpened() {
        return !quitting;
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        try {
            TYClient.instance.configManager.saveAllConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dropdownItem = null;
        searching = false;
        quitting = false;
    }

    private float scrollY;
    private float CscrollY;

    @Override
    public void handleMouseInput() throws IOException {
        this.scrollY += (float) Mouse.getEventDWheel();
        if (this.scrollY >= 0.0f) {
            this.scrollY = 0.0f;
        }

        this.CscrollY += (float) Mouse.getEventDWheel();
        if (this.CscrollY >= 0.0f) {
            this.CscrollY = 0.0f;
        }

        int i = Mouse.getEventX() * new ScaledResolution(mc).getScaledWidth() / this.mc.displayWidth;
        int j = new ScaledResolution(mc).getScaledHeight() - Mouse.getEventY() * new ScaledResolution(mc).getScaledHeight() / this.mc.displayHeight - 1;
        super.handleMouseInput();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN && searching)
            return;

        if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(null);

        if (searching) {
            searchTextField.setFocused(true);
            searchTextField.keyTyped(typedChar, keyCode);
            resetModuleList();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private Color getColor(int r, int g, int b) {
        return RenderUtil.reAlpha(new Color(r, g, b), Math.round((visibleAnimation / 100F) * 255F));
    }

    private Color getColor(int r, int g, int b, int a) {
        return RenderUtil.reAlpha(new Color(r, g, b), Math.round((visibleAnimation / 100F) * a));
    }

    private Color getColor(Color color) {
        return RenderUtil.reAlpha(color, Math.round((visibleAnimation / 100F) * color.getAlpha()));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (currentSliding != null) {
            currentSliding.sliding = false;
            currentSliding = null;
        }
    }

    public String findLongestString(String[] strArray) {
        String longestString = "";
        for (String str : strArray) {
            if (font.getStringWidth(str) > font.getStringWidth(longestString)) {
                longestString = str;
            }
        }
        return longestString;
    }

    @Override
    public void initGui() {
        super.initGui();
        resetModuleList();
    }

    private void click(List<Module> modules, float offset, int mouseX, int mouseY, int mouseButton) {
        float moduleY = 0 + moduleWheel[1];

        for (Module module : modules) {
            moduleY += 10;

            // 左键切换模块开关
            if (RenderUtil.isHovering(offset + 4, y + 50 + moduleY + 4, 184, 12, mouseX, mouseY) && mouseButton == 0) {
                module.toggle();
                return;
            }

            // 右键/左键点击展开/收起按钮
            if (RenderUtil.isHovering(offset + 160, y + 50 + moduleY - 15, 20, 20, mouseX, mouseY)) {
                if (mouseButton == 1 || mouseButton == 0) {
                    moduleExpandState.put(module, !moduleExpandState.get(module));
                    return;
                }
            }

            moduleY += 18;

            // 处理展开模块的value点击
            if (moduleExpandState.get(module) && moduleExpandAnimation.get(module) > 0.5f) {
                for (Value<?> property : module.getValues()) {
                    if (property.isHidden())
                        continue;

                    if (property instanceof BooleanValue) {
                        final BooleanValue bp = (BooleanValue) property;
                        if (RenderUtil.isHovering(offset + 4, y + 50 + moduleY + 5, 184, 4f+8, mouseX, mouseY) && mouseButton == 0) {
                            bp.setValue(!bp.get());
                            return;
                        }
                    }

                    if (property instanceof NumberValue) {
                        final NumberValue dp = (NumberValue) property;
                        if (RenderUtil.isHovering(offset + 88, y + 50 + moduleY + 2, 78, 16, mouseX, mouseY) && mouseButton == 0) {
                            dp.sliding = true;
                            currentSliding = dp;
                        }
                    }

                    if (property instanceof ModeValue) {
                        final ModeValue sp = (ModeValue) property;
                        if (RenderUtil.isHovering(offset + 4, y + 50 + moduleY + 6, 184, 18, mouseX, mouseY)) {
                            if (mouseButton == 0) {
                                if (dropdownItem != property) {
                                    dropdownItem = sp;
                                    sp.animation = 100;
                                    protectArea = new Rectangle(offset + 100, y + moduleY + 50 + 24, 80, 14 * sp.getModes().size());
                                } else {
                                    dropdownItem = null;
                                    protectArea = null;
                                }
                            }
                        }
                    }

                    if (property instanceof ColorValue) {
                        final ColorValue cp = (ColorValue) property;
                        if (RenderUtil.isHovering((float) (offset + 175 - 5.5), (float) (y + 50 + moduleY + 9 - 5.5), 11, 11, mouseX, mouseY)) {
                            if (mouseButton == 0) {
                                dropdownItem = cp;
                                protectArea = new Rectangle(offset + 100, y + moduleY + 50 + 24, 80, cp.isAlphaChangeable() ? 80 : 67);
                            } else {
                                cp.setRainbowEnabled(!cp.isEnabledRainbow());
                            }
                        }
                    }
                    moduleY += property.getHeight();
                }
            }
            moduleY += 4;
        }
    }

    // 获取点击的模块
    private Module getClickedModule(List<Module> modules, float offset, int mouseX, int mouseY) {
        float moduleY = 0 + moduleWheel[1];
        for (Module module : modules) {
            if (RenderUtil.isHovering(offset, y + 46 + FontManager.font16.getHeight() + 4 + moduleY, 190, 16, mouseX, mouseY)) {
                return module;
            }
            moduleY += 10 + 18 + 4;
            if (moduleExpandState.get(module)) {
                for (Value<?> property : module.getValues()) {
                    if (property.isAvailable()) {
                        moduleY += property.getHeight();
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (dropdownItem != null && protectArea != null) {
            if (dropdownItem instanceof ModeValue) {
                final ModeValue property = (ModeValue) dropdownItem;
                int buttonY = 0;
                for (String s : property.getModes()) {
                    final boolean isHovering = RenderUtil.isHovering(protectArea.getX() + .5F, protectArea.getY() + buttonY + .5F, protectArea.getWidth() - 1, 14, mouseX, mouseY);
                    if (isHovering && mouseButton == 0) {
                        property.setValue(s);
                        dropdownItem = null;
                        protectArea = null;
                        return;
                    }
                    buttonY += 14;
                }
            }

            if (dropdownItem instanceof ColorValue) {
                if (!RenderUtil.isHovering(protectArea.getX(), protectArea.getY(), protectArea.getWidth(), protectArea.getHeight() + 1, mouseX, mouseY)) {
                    dropdownItem = null;
                    protectArea = null;
                }
                return;
            }
        }

        if (!RenderUtil.isHovering(x, y, width, height, mouseX, mouseY)) return;
        float width = Math.max(FontManager.bold38.getStringWidth(TYClient.NAME.toUpperCase()), FontManager.bold38.getStringWidth("NOVOLINE"));

        if (RenderUtil.isHovering(x + width + 16 + 10, y + 10, 200, 20, mouseX, mouseY) && current != Category.Pages.CONFIGS) {
            searching = !searching;
            if (searching) {
                lists = new List[]{leftModules, rightModules};
                searchTextField.setText("");
            } else {
                leftModules = (List<Module>) lists[0];
                rightModules = (List<Module>) lists[1];
                resetModuleList();
            }
            searchTextField.setText("");
        }

        float pageY = 44 + CscrollAni;

        if (current == Category.Pages.CONFIGS) {
            boolean hovered = RenderUtil.isHovering(x + width + 16 + 4, y + 10, 89, 20, mouseX, mouseY);
            if (hovered) {
                mc.displayGuiScreen(new SavePresetScreen(this));
            }
            boolean hovered2 = RenderUtil.isHovering(x + width + 16 + 96, y + 10, 89, 20, mouseX, mouseY);
            if (hovered2) {
                Desktop.getDesktop().open(TYClient.instance.configManager.dir);
            }
        }

        if (current == Category.Pages.CONFIGS) {
            float configY = y + 40 + 8 + 14 + scrollAni;
            for (String config : TYClient.configManager.getConfigs()) {
                boolean fuck = RenderUtil.isHovering(x + width + 16 + 6 + 290 - 6, configY + 8, 76, 20, mouseX, mouseY);
                if (fuck) {
                    if (mouseButton == 0) {
                        if ("modules.json".equals(config)) {
                            TYClient.instance.configManager.saveUserConfig(config);
                            NotificationManager.post(NotificationType.SUCCESS, "Config", "Config successfully saved.");
                        } else {
                            TYClient.instance.configManager.loadUserConfig(config);
                            NotificationManager.post(NotificationType.SUCCESS, "Config", "Config successfully loaded.");
                        }
                    }
                }
                configY += 42;
            }
        }

        if (RenderUtil.isHovering(x, y + 44, width + 10, 240, mouseX, mouseY)) {
            for (Category page : Category.values()) {
                pageY += 12;
                for (Category.Pages cate : page.getSubPages()) {
                    if (RenderUtil.isHovering(x + 8, y + pageY, width, 16, mouseX, mouseY) && mouseButton == 0) {
                        if (cate != current) cate.animation.mouseClicked(mouseX, mouseY);
                        scrollAni = 0;
                        scrollY = 0;
                        current = cate;
                        dropdownItem = null;
                        protectArea = null;
                        moduleWheel[0] = 0;
                        moduleWheel[1] = 0;
                        resetModuleList();
                        return;
                    }
                    pageY += 26;
                }
                pageY += 4;
            }
        }

        // 右键展开/关闭模块value列表
        if (current.module && RenderUtil.isHovering(x + width + 16 + 8, y + 40, 400, 400, mouseX, mouseY) && mouseButton == 1) {
            Module clickedModule = getClickedModule(leftModules, x + width + 24, mouseX, mouseY);
            if (clickedModule == null) {
                clickedModule = getClickedModule(rightModules, x + width + 24 + 200, mouseX, mouseY);
            }

            if (clickedModule != null) {
                moduleExpandState.put(clickedModule, !moduleExpandState.get(clickedModule));
                return;
            }
        }

        // 左键处理value点击
        if (current.module && RenderUtil.isHovering(x + width + 16 + 8, y + 40, 400, 400, mouseX, mouseY) && mouseButton == 0) {
            click(leftModules, x + width + 24, mouseX, mouseY, mouseButton);
            click(rightModules, x + width + 24 + 200, mouseX, mouseY, mouseButton);
        }

        if (mouseButton == 0 && RenderUtil.isHovering(x, y, 520, 43, mouseX, mouseY)) {
            dragX = mouseX - x;
            dragY = mouseY - y;
            dragging = true;
        }
    }

    public void setCurrent(Category.Pages current) {
        this.current = current;
    }

    public void resetModuleList() {
        leftModules.clear();
        rightModules.clear();

        final List<Module> allList = new ArrayList<>();

        if (searching) {
            for (Module module : TYClient.instance.getModuleManager().getModuleMap().values()) {
                if (module.getName().toLowerCase().replace(" ", "").contains(searchTextField.getText().toLowerCase().replace(" ", "")))
                    allList.add(module);
            }
        } else {
            allList.addAll(TYClient.instance.getModuleManager().getModsByPage(current));
        }

        // 按模块名称排序
        allList.sort(Comparator.comparing(Module::getName));

        // 双列分配
        int updateIndex = 0;
        while (updateIndex <= allList.size() - 1) {
            leftModules.add(allList.get(updateIndex));
            updateIndex += 2;
        }

        updateIndex = 1;
        while (updateIndex <= allList.size() - 1) {
            rightModules.add(allList.get(updateIndex));
            updateIndex += 2;
        }

        // 初始化所有模块的展开状态和动画值
        for (Module module : allList) {
            if (!moduleExpandState.containsKey(module)) {
                moduleExpandState.put(module, true);
                moduleExpandAnimation.put(module, 1.0f);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static class BindScreen extends GuiScreen {
        private final Module target;
        private final GuiScreen parent;

        public BindScreen(Module module, GuiScreen parent) {
            this.target = module;
            this.parent = parent;
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);

            if (keyCode == 1) {
                this.mc.displayGuiScreen(parent);
            }

            if (keyCode != 1 && keyCode != Keyboard.KEY_DELETE) {
                this.target.setKey(keyCode);
                this.mc.displayGuiScreen(parent);
            }

            if (keyCode == Keyboard.KEY_DELETE) {
                this.target.setKey(Keyboard.KEY_NONE);
                this.mc.displayGuiScreen(parent);
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "Press any key to bind " + EnumChatFormatting.YELLOW + target.getName(), this.width / 2, 150, 0xFFFFFF);
            this.drawCenteredString(this.fontRendererObj, "Press Delete key to remove the bind.", this.width / 2, 170, 0xFFFFFF);

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public static class SavePresetScreen extends GuiScreen {
        private final GuiScreen parent;
        private GuiTextField nameField;

        public SavePresetScreen(GuiScreen parent) {
            this.parent = parent;
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);

            this.nameField.textboxKeyTyped(typedChar, keyCode);

            if (keyCode == 1) {
                this.mc.displayGuiScreen(parent);
            }

            this.nameField.setText(this.nameField.getText().replace(" ", "").replace("#", "").replace("_NONE", ""));
        }

        public void initGui() {
            this.nameField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, this.width / 2 - 100, this.height / 6 + 20, 200, 20);
            this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 6 + 40 + 22 * 5, "Add"));
            this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 6 + 40 + 22 * 6, "Cancel"));
        }

        protected void actionPerformed(GuiButton button) throws IOException {
            if (button.id == 3) {
                TYClient.instance.configManager.saveConfig(this.nameField.getText());
                mc.displayGuiScreen(this.parent);
            }

            if (button.id == 4) {
                mc.displayGuiScreen(this.parent);
            }
        }

        @Override
        protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
            this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void updateScreen() {
            this.nameField.updateCursorCounter();
            super.updateScreen();
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, "Name", this.width / 2 - 89, this.height / 6 + 10, 0xFFFFFF);
            this.nameField.drawTextBox();

            this.drawCenteredString(this.fontRendererObj, "Adding Preset", this.width / 2, 30, 0xFFFFFF);

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}