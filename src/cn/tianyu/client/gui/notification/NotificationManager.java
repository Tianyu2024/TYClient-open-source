package cn.tianyu.client.gui.notification;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.module.impl.client.Notifications;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: CN_tianyu
 */
public class NotificationManager {
    @Getter
    @Setter
    private static float toggleTime = 2;

    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void post(NotificationType type, String title, String description) {
        if(TYClient.moduleManager.getByClass(Notifications.class).isEnable()){
            post(new Notification(type, title, description));
        }
    }

    public static void post(NotificationType type, String title, String description, float time) {
        if(TYClient.moduleManager.getByClass(Notifications.class).isEnable()){
            post(new Notification(type, title, description, time));
        }
    }

    public static void post(Notification notification) {
        if(TYClient.moduleManager.getByClass(Notifications.class).isEnable()){
            notifications.add(notification);
        }
    }

}
