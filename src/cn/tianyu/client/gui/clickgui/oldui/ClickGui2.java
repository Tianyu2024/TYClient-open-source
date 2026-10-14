package cn.tianyu.client.gui.clickgui.oldui;

import cn.tianyu.client.module.Category;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CN_tianyu
 */
public class ClickGui2 extends GuiScreen {
    private final List<CategoryPanel2> categoryPanels = new ArrayList<>();
    private int startX;
    private int startY;
    private int padding = 5;
    private int scrollY = 0;
    private final int scrollSpeed = 18;

    public ClickGui2() {
        this.startX = 20;
        this.startY = 20;
        initPanels();
    }

    @Override
    public void initGui() {
        scrollY = 0;
        if (categoryPanels.isEmpty()) {
            initPanels();
        }
    }

    private void initPanels() {
        Category[] categories = Category.values();
        int currentX = startX;

        for (Category category : categories) {
            CategoryPanel2 panel = new CategoryPanel2(currentX, startY, category);
            categoryPanels.add(panel);
            currentX += panel.width + padding;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        handleScroll();

        GlStateManager.pushMatrix();

        float scale = (float) width / 1130f;
        GL11.glScalef(scale, scale, 1f);
        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale);

        GlStateManager.translate(0, scrollY, 0);

        int finalMouseX = mouseX;
        int finalMouseY = mouseY - scrollY;
        categoryPanels.forEach(it -> it.drawScreen(finalMouseX, finalMouseY, partialTicks));

        GlStateManager.popMatrix();
    }

    private void handleScroll() {
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            if (wheel > 0) {
                scrollY += scrollSpeed;
            } else {
                scrollY -= scrollSpeed;
            }

            if (scrollY > 0) scrollY = 0;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float scale = (float) width / 1130f;
        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale) - scrollY;

        for (CategoryPanel2 panel : categoryPanels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        float scale = (float) width / 1130f;
        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale) - scrollY;

        for (CategoryPanel2 panel : categoryPanels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}