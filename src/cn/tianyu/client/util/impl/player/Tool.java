package cn.tianyu.client.util.impl.player;

import net.minecraft.item.ItemStack;

import java.util.Objects;

/**
 * @Author: CN_tianyu
 */
public final class Tool {
    private final int slot;
    private final double efficiency;
    private final ItemStack stack;

    public Tool(int slot, double efficiency, ItemStack stack) {
        this.slot = slot;
        this.efficiency = efficiency;
        this.stack = stack;
    }

    public int slot() {
        return slot;
    }

    public double efficiency() {
        return efficiency;
    }

    public ItemStack stack() {
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        return slot == tool.slot &&
                Double.compare(tool.efficiency, efficiency) == 0 &&
                Objects.equals(stack, tool.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, efficiency, stack);
    }

    @Override
    public String toString() {
        return "Tool[" +
                "slot=" + slot +
                ", efficiency=" + efficiency +
                ", stack=" + stack +
                ']';
    }
}