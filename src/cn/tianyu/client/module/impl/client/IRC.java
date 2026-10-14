package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

@DefaultEnabled
public class IRC extends Module {
    public IRC() {
        super("IRC", "IRC聊天服务器", Category.client);
    }
    @Override
    public void onDisable() {
        NotificationManager.post(NotificationType.INFO, "IRC", "Use .irc to send messages");
        setEnable(true);
    }
}
