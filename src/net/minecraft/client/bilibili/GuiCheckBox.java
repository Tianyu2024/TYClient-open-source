package net.minecraft.client.bilibili;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public class GuiCheckBox extends GuiButton
{
    private boolean checked;

    public GuiCheckBox(int id, int x, int y, String text, boolean checked)
    {
        super(id, x, y, 150, 20, text);
        this.checked = checked;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // 画勾选框（小方块）
            drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46, 20, 20);

            // 画勾选标记
            if (this.checked)
            {
                drawTexturedModalRect(this.xPosition + 2, this.yPosition + 2, 20, 46, 16, 16);
            }

            // 画文字
            this.drawString(mc.fontRendererObj, this.displayString, this.xPosition + 24, this.yPosition + 6, 0xFFFFFF);
        }
    }

//    @Override
//    public void actionPerformed()
//    {
//        this.checked = !this.checked;
//    }

    public boolean isChecked()
    {
        return this.checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }
}