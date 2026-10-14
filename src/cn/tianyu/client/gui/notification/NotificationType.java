package cn.tianyu.client.gui.notification;

import cn.tianyu.client.util.impl.render.FontUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */

@Getter
@AllArgsConstructor
public enum NotificationType {
    SUCCESS(new Color(20, 250, 90), FontUtils.CHECKMARK),
    DISABLE(new Color(255, 30, 30), FontUtils.XMARK),
    INFO(Color.DARK_GRAY, FontUtils.INFO),
    WARNING(Color.YELLOW, FontUtils.WARNING);
    private final Color color;
    private final String icon;
}
