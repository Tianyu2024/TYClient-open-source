package org.lwjglx.better;

import net.minecraft.util.BlockPos;

/**
 * @Author: CN_tianyu
 */
public class MutableBlockPos extends BlockPos {
    private int x;
    private int y;
    private int z;

    public MutableBlockPos(int x, int y, int z) {
        super(0, 0, 0); // 传 0 给父类，避免父类 final 字段占用
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // 关键：允许动态修改坐标，不创建新对象
    public org.lwjglx.better.MutableBlockPos setPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public int getX() { return this.x; }
    @Override
    public int getY() { return this.y; }
    @Override
    public int getZ() { return this.z; }
}