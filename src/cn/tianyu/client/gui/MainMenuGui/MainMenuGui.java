package cn.tianyu.client.gui.MainMenuGui;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.ChooseGui;
import cn.tianyu.client.gui.ReallyGui;
import cn.tianyu.client.gui.alt.GuiAltManager;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.LayeringAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.main.Main;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.glu.Project;

import java.awt.*;
import java.io.IOException;

public class MainMenuGui extends GuiScreen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24; // 按钮间距

    private ResourceLocation backgroundTexture;
    private DynamicTexture viewportTexture;

    @Override
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        int START_Y = this.height / 2 - 40;
        buttonList.clear();
        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        buttonList.add(new GuiButton(0, centerX, START_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Singleplayer"));

        buttonList.add(new GuiButton(1, centerX, START_Y + BUTTON_SPACING,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Multiplayer"));

        buttonList.add(new GuiButton(2, centerX, START_Y + BUTTON_SPACING * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT, "AltManager"));

        buttonList.add(new GuiButton(3, centerX, START_Y + BUTTON_SPACING * 3,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Options"));

        buttonList.add(new GuiButton(4, centerX, START_Y + BUTTON_SPACING * 4,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Shutdown"));

//        buttonList.add(new GuiButton(5, centerX, START_Y + BUTTON_SPACING * 4 + 50,
//                BUTTON_WIDTH, BUTTON_HEIGHT, "Choose IRC"));

//                buttonList.add(new GuiButton(6, centerX, START_Y + BUTTON_SPACING * 5,
//                BUTTON_WIDTH, BUTTON_HEIGHT, "共匪"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //TYClient.instance.wallpaperEngine.render(width,height);
//        GlStateManager.disableAlpha();
//        this.renderSkybox(mouseX, mouseY, partialTicks);
//        GlStateManager.enableAlpha();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        RenderUtil.drawImage(new ResourceLocation("client/M.jpg"), 0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        int titleY = 30;
        FontManager.F48.drawCenteredString(TYClient.NAME + "-" + TYClient.VERSION, width / 2, titleY + 21, new Color(255, 255, 255, 255).getRGB());
        FontManager.F22.drawString("Release on 2026/8/20 ", 10.0f, sr.getScaledHeight() - 10.0f - FontManager.F22.getHeight() - 2, new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString(TYClient.NAME.toUpperCase() + " " +TYClient.VERSION, 10.0f, sr.getScaledHeight() - 10.0f, new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawStringFromRight("by: CN_tianyu", this.width - 1, sr.getScaledHeight() - 13.0f, new Color(255, 255, 255, 200).getRGB());


        // draw change logs
        int y = 0;
        FontManager.F22.drawString("[+] AutoProjectile Update", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] add module movefix", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[+] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        //FontManager.F22.drawString("[-] Fix AutoClicker", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        //FontManager.F22.drawString("[-] Fix BowAimbot", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        //FontManager.F22.drawString("[-] Fix Backtrack", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        //FontManager.F22.drawString("[-] Fix AutoRod", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        //FontManager.F22.drawString("[-] Fix AntiVoid", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        FontManager.F22.drawString("[-] ", 0, y += FontManager.F22.getHeight(), new Color(255, 255, 255, 200).getRGB());
        super.drawScreen1(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: // Singleplayer
                LayeringAnimation.play(new GuiSelectWorld(this));
                break;
            case 1: // Multiplayer
                LayeringAnimation.play(new GuiMultiplayer(this));
                break;
            case 2: // AltManager
                LayeringAnimation.play(new GuiAltManager(this));
                break;
            case 3: // Settings
                LayeringAnimation.play(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 4: // Exit
                this.mc.shutdown();
                break;
            case 5: // IRC
                LayeringAnimation.play(new ChooseGui());
                break;
            case 6:
                LayeringAnimation.play(new ReallyGui());
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    private void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_)
    {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        //this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        float f = this.width > this.height ? 120.0F / (float)this.width : 120.0F / (float)this.height;
        float f1 = (float)this.height * f / 256.0F;
        float f2 = (float)this.width * f / 256.0F;
        int i = this.width;
        int j = this.height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double)j, (double)this.zLevel).tex((double)(0.5F - f1), (double)(0.5F + f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos((double)i, (double)j, (double)this.zLevel).tex((double)(0.5F - f1), (double)(0.5F - f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos((double)i, 0.0D, (double)this.zLevel).tex((double)(0.5F + f1), (double)(0.5F - f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(0.5F + f1), (double)(0.5F + f2)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }

    private void rotateAndBlurSkybox(float p_73968_1_)
    {
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;

        for (int j = 0; j < i; ++j)
        {
            float f = 1.0F / (float)(j + 1);
            int k = this.width;
            int l = this.height;
            float f1 = (float)(j - i / 2) / 256.0F;
            worldrenderer.pos((double)k, (double)l, (double)this.zLevel).tex((double)(0.0F + f1), 1.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos((double)k, 0.0D, (double)this.zLevel).tex((double)(1.0F + f1), 1.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(1.0F + f1), 0.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, (double)l, (double)this.zLevel).tex((double)(0.0F + f1), 0.0D).color(1.0F, 1.0F, 1.0F, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }
    
}