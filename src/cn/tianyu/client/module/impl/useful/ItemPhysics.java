package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;


@DefaultEnabled
public class ItemPhysics extends Module {
    public ItemPhysics() {
        super("ItemPhysics", "物理掉落", Category.client);
    }
}
