package cn.tianyu.client.gui.notification;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import cn.tianyu.client.util.impl.render.ShaderElement;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */

@Getter
public class Notification {
    private final NotificationType notificationType;
    private final String title, description;
    private final float time;
    private final TimerUtil timerUtil;
    private final Animation animation;
    public String icon;
    private final Animation anim = new DecelerateAnimation(250, 1);

    public Notification(NotificationType type, String title, String description) {
        this(type, title, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String title, String description, float time) {
        this.title = title;
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtil = new TimerUtil();
        this.notificationType = type;
        animation = new DecelerateAnimation(300, 1);

        switch (type) {
            case DISABLE:
                this.icon = "B";
                break;
            case SUCCESS:
                this.icon = "A";
                break;
            case INFO:
                this.icon = "C";
                break;
            case WARNING:
                this.icon = "D";
                break;
        }

    }

    public void drawSimple(float x, float y, float width, float height) {
        ShaderElement.addBlurTask(() -> RenderUtil.drawRectWH(x, y, width, height, -1));
        ShaderElement.addBloomTask(() -> RenderUtil.drawRectWH(x, y, width, height, ColorUtil.getColor(0, 0, 0, 200)));

        String icon;
        switch (title) {
            case "Friend Manager":
                // material-supervisor_account
                icon = "\uEC2D";
                break;
            case "Config":
                // material-insert_drive_file
                icon = "\uEA21";
                break;
            case "Module":
                // material-layers
                icon = "\uEB13";
                break;
            case "IRC":
                // material-insert_comment
                icon = "\uEA20";
                break;
            default:
                // material-info
                icon = "\uEBF8";
                break;
        }

        RenderUtil.drawRectWH(x, y, width, height, ColorUtil.getColor(0, 0, 0, 76));

        float textX = x + 2 + 5;
        float textY = y + 6 - 1;

        RenderUtil.drawRectWH(x, textY, 1, 8, HUD.color(1).getRGB());

        FontManager.material18.drawStringDynamic(icon, textX - 1, textY + 2, 1,2);
        FontManager.axBold20.drawStringDynamic(title, textX + FontManager.material18.getStringWidth(icon) + 2, y + 6, 2,6);
        FontManager.font18.drawString(description, textX, textY + (FontManager.axBold20.getHeight() - 2), -1);
    }

    public void drawExhibition(float x, float y, float width, float height) {
        RenderUtil.drawRectWH(x, y, width, height, new Color(16, 16, 16, 180).getRGB());
        float percentage = Math.min((timerUtil.getTime() / getTime()), 1);
        RenderUtil.drawRectWH(x + (width * percentage), y + height - 1, width - (width * percentage), 1, getNotificationType().getColor().getRGB());
        RenderUtil.drawImage(new ResourceLocation("client/images/" + getNotificationType().getIcon() + ".png"), x + 3, (y + FontManager.icontestFont40.getMiddleOfBox(height) + 0.8f), 18, 18, -1);

        RapeMasterFontManager tahomaFont18 = FontManager.font18;
        tahomaFont18.drawString(getTitle(), x + 25, y + 4, Color.WHITE.getRGB());
        FontManager.font15.drawString(getDescription(), x + 25, y + 3.1f + tahomaFont18.getHeight(), Color.WHITE.getRGB());
    }
}
