package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import cn.tianyu.client.util.impl.Workers;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @Author: CN_tianyu
 */
public class AsyncChunkBuilder {
    private static final ConcurrentHashMap<BlockPos, Future<Void>> buildQueue = new ConcurrentHashMap<>();

    public static void rebuildChunkAsync(Chunk chunk) {
        BlockPos pos = new BlockPos(chunk.xPosition << 4, 0, chunk.zPosition << 4);

        Future<Void> old = buildQueue.remove(pos);
        if (old != null && !old.isDone()) {
            old.cancel(true);
        }

        Future<Void> future = Workers.Default.submit(() -> {
            // 在 Worker 线程中计算可见性
            // 计算结果存到队列，主线程读取
            return null;
        });

        buildQueue.put(pos, future);
    }

    public static void processCompleted() {
        for (BlockPos pos : buildQueue.keySet()) {
            Future<Void> future = buildQueue.get(pos);
            if (future != null && future.isDone()) {
                try {
                    future.get();
                    // 主线程中触发渲染更新
                    buildQueue.remove(pos);
                } catch (Exception e) {
                    buildQueue.remove(pos);
                }
            }
        }
    }
}