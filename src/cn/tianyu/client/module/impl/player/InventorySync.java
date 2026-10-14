package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class InventorySync extends Module {

    public short action;

    public InventorySync() {
        super("InventorySync","背包同步", Category.player);
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        final Packet<?> packet = event.getPacket();

        if (event.getState() == EventPacket.EventState.RECEIVE) {
            if (packet instanceof S32PacketConfirmTransaction) {
                final S32PacketConfirmTransaction wrapper = (S32PacketConfirmTransaction) packet;
                final Container inventory = mc.thePlayer.inventoryContainer;

                if (wrapper.getWindowId() == inventory.windowId) {
                    action = wrapper.getActionNumber();

                    if (action > 0 && action < inventory.transactionID) {
                        inventory.transactionID = (short) (action + 1);
                    }
                }
            }
        }
    }
}
