package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.MathUtils;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

import static cn.tianyu.client.util.impl.player.MoveUtil.isMoving;

/**
 * @Author: CN_tianyu
 */
public class AutoSoup extends Module {
    private final TimerUtil timer = new TimerUtil();
    public final NumberValue MaxDELAY = new NumberValue("MaxDelay", 350.0, 0.0, 1000.0, 10.0);
    public final NumberValue MinDELAY = new NumberValue("MinDelay", 350.0, 0.0, 1000.0, 10.0);
    public final NumberValue HEALTH = new NumberValue("Health", 3.0, 0.0, 20.0, 1.0);
    public final BooleanValue DROP = new BooleanValue("Drop", true);
    public final BooleanValue safeSwap = new BooleanValue("SafeSwap", false);

    public AutoSoup() {
        super("AutoSoup", "自动喝汤", Category.combat);
    }

    @EventTarget
    public void onEvent(EventMotion event) {
        if(!event.isPre()){
            return;
        }
        int soupSlot = getSoupFromInventory();
        if (soupSlot != -1 && mc.thePlayer.getHealth() < (HEALTH.getValue().floatValue())
                && timer.hasReached(MathUtils.randomNumber(MaxDELAY.getValue(), MinDELAY.getValue()))) {
            if(!safeSwap.get() || !isMoving()) {
                swap(getSoupFromInventory());
            }
            sendPacket(new C09PacketHeldItemChange(6));
            sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    protected void swap(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 6, 2, mc.thePlayer);
    }

    public int getSoupFromInventory() {
        int soup = -1;
        for (int i = 1; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (Item.getIdFromItem(item) == 282) {
                    soup = i;
                }
            }
        }
        return soup;
    }
}
