package cn.tianyu.client.gui.MainMenuGui;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.ui.RoundRect;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MenuButton extends Gui {
    /**
     * Button width in pixels
     */
    @Setter
    protected int width;

    /**
     * Button height in pixels
     */
    protected int height;

    /**
     * The x position of this control.
     */
    public int xPosition;

    /**
     * The y position of this control.
     */
    public int yPosition;

    /**
     * The string displayed on this control.
     */
    public String displayString;
    public int id;

    /**
     * True if this control is enabled, false to disable.
     */
    public boolean enabled;

    /**
     * Hides the button completely if false.
     */
    public boolean visible;
    protected boolean hovered;

    public MenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton2(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            if (hovered) {
                RoundRect.drawRoundRect(this.xPosition + 1, this.yPosition, this.xPosition + width - 1, this.yPosition + height,7,  new Color(255, 255, 255, 165).getRGB());
            } else {
                RoundRect.drawRoundRect(this.xPosition, this.yPosition, this.xPosition + width, this.yPosition + height, 7, new Color(255, 255, 255, 187).getRGB());
            }
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }
            FontManager.F22.drawCenteredString(this.displayString,this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }
}
