package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @Author: CN_tianyu
 */
public final class ChunkBufferPool {
    public static final ChunkBufferPool INSTANCE = new ChunkBufferPool();
    private static final int INIT_CAPACITY = 4096;
    private static final int POOL_MAX_SIZE = 24;
    private final ArrayBlockingQueue<AsyncChunkBuffer> pool = new ArrayBlockingQueue<>(POOL_MAX_SIZE);

    private ChunkBufferPool() {}

    public AsyncChunkBuffer acquire() {
        AsyncChunkBuffer buf = pool.poll();
        if (buf == null) {
            return new AsyncChunkBuffer(INIT_CAPACITY);
        }
        buf.reset();
        return buf;
    }

    public void release(AsyncChunkBuffer buffer) {
        if (buffer == null) return;
        if (pool.size() < POOL_MAX_SIZE) {
            pool.offer(buffer);
        }
    }
}