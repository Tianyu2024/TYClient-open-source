package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

@DefaultEnabled
public class Notifications extends Module {

    public Notifications() {
        super("Notifications", "通知功能", Category.client);
    }
}