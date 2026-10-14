package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import cn.tianyu.client.util.impl.Workers;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.chunk.Chunk;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: CN_tianyu
 */
public class ChunkGenTask implements Workers.WorkerTask {
    public final RenderChunk renderChunk;
    public final AsyncChunkBuffer buffer;
    public final AtomicBoolean cancelFlag = new AtomicBoolean(false);

    public ChunkGenTask(RenderChunk rc, AsyncChunkBuffer buf) {
        this.renderChunk = rc;
        this.buffer = buf;
    }

    @Override
    public void run() {
        if (cancelFlag.get()) return;
        buffer.reset();
        Chunk worldChunk = renderChunk.getChunk();
        // MCP公开字段 chunk.xPos / chunk.zPos，不访问私有chunkX
        int baseWorldX = worldChunk.xPosition * 16;
        int baseWorldZ = worldChunk.zPosition * 16;

        // 纯CPU顶点计算，无任何GL、Tessellator调用
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    if (cancelFlag.get()) return;
                    buildBlockMesh(worldChunk, x, y, z, baseWorldX, baseWorldZ, buffer);
                }
            }
        }
    }

    // 方块顶点生成逻辑（仅写入缓冲）
    private void buildBlockMesh(Chunk chunk, int bx, int by, int bz, int worldX, int worldZ, AsyncChunkBuffer buf) {
        // 你原版方块光照、六面剔除、UV、法线代码
    }

    // 外部取消任务
    public void cancel() {
        cancelFlag.set(true);
    }
}