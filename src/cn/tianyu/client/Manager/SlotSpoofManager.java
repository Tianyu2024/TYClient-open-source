package cn.tianyu.client.Manager;

import lombok.Getter;
import net.minecraft.item.ItemStack;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class SlotSpoofManager {
    private int spoofedSlot;

    @Getter
    private boolean spoofing;

    public void startSpoofing(int slot) {
        this.spoofing = true;
        this.spoofedSlot = slot;
    }

    public void stopSpoofing() {
        this.spoofing = false;
    }

    public int getSpoofedSlot() {
        return spoofing ? spoofedSlot : mc.thePlayer.inventory.currentItem;
    }

    public ItemStack getSpoofedStack() {
        return spoofing ? mc.thePlayer.inventory.getStackInSlot(spoofedSlot) : mc.thePlayer.inventory.getCurrentItem();
    }
}
