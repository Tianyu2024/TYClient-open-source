package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: CN_tianyu
 */
public class AsyncChunkBuffer {
    private final ReentrantLock lock = new ReentrantLock();
    private final int stride = 12 * 4;
    private ByteBuffer buffer;
    private int vertexCount;

    public AsyncChunkBuffer(int initVertexCapacity) {
        buffer = ByteBuffer.allocateDirect(initVertexCapacity * stride);
    }

    public void reset() {
        lock.lock();
        try {
            buffer.clear();
            vertexCount = 0;
        } finally {
            lock.unlock();
        }
    }

    // 写入完整顶点数据
    public void put(float x, float y, float z, float u, float v, float r, float g, float b, float a, float nx, float ny, float nz) {
        lock.lock();
        try {
            buffer.putFloat(x);
            buffer.putFloat(y);
            buffer.putFloat(z);
            buffer.putFloat(u);
            buffer.putFloat(v);
            buffer.putFloat(r);
            buffer.putFloat(g);
            buffer.putFloat(b);
            buffer.putFloat(a);
            buffer.putFloat(nx);
            buffer.putFloat(ny);
            buffer.putFloat(nz);
            vertexCount++;
        } finally {
            lock.unlock();
        }
    }

    // 读取一整组顶点浮点数据
    public void readVertex(float[] out) {
        lock.lock();
        try {
            if (buffer.remaining() < stride) return;
            out[0] = buffer.getFloat();
            out[1] = buffer.getFloat();
            out[2] = buffer.getFloat();
            out[3] = buffer.getFloat();
            out[4] = buffer.getFloat();
            out[5] = buffer.getFloat();
            out[6] = buffer.getFloat();
            out[7] = buffer.getFloat();
            out[8] = buffer.getFloat();
            out[9] = buffer.getFloat();
            out[10] = buffer.getFloat();
            out[11] = buffer.getFloat();
        } finally {
            lock.unlock();
        }
    }

    public ByteBuffer getReadBuffer() {
        lock.lock();
        try {
            buffer.flip();
            return buffer.slice();
        } finally {
            lock.unlock();
        }
    }

    public int getVertexCount() {
        lock.lock();
        try {
            return vertexCount;
        } finally {
            lock.unlock();
        }
    }
}