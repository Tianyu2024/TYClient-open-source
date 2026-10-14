package cn.tianyu.client.TianyuOptiFine.betterfps.impl.render;

import cn.tianyu.client.util.impl.Workers;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: CN_tianyu
 */
public class FastTessellator {
    // 使用更小、更安全的缓冲区大小
    private static final int MAX_VERTS = 0x10000; // 65536 个顶点（比原来小16倍）
    private static final int MAX_INDICES = MAX_VERTS * 6;

    // 缓冲区池（复用 DirectBuffer，避免频繁分配）
    private static final ConcurrentLinkedQueue<FloatBuffer> vertexPool = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<FloatBuffer> texPool = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<FloatBuffer> colorPool = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<IntBuffer> indexPool = new ConcurrentLinkedQueue<>();

    private FloatBuffer vertexBuffer;
    private FloatBuffer texBuffer;
    private FloatBuffer colorBuffer;
    private IntBuffer indicesBuffer;

    private int vertexCount = 0;
    private int vaoId = -1;
    private int vboVertex = -1, vboTex = -1, vboColor = -1, vboIndices = -1;
    private boolean built = false;

    public FastTessellator() {
        // 从池中获取缓冲区，而不是每次都新建
        vertexBuffer = getFloatBuffer(MAX_VERTS * 3);
        texBuffer = getFloatBuffer(MAX_VERTS * 2);
        colorBuffer = getFloatBuffer(MAX_VERTS * 4);
        indicesBuffer = getIntBuffer(MAX_INDICES);

        initGL();
    }

    private FloatBuffer getFloatBuffer(int size) {
        FloatBuffer buffer = vertexPool.poll();
        if (buffer == null || buffer.capacity() < size) {
            buffer = BufferUtils.createFloatBuffer(size);
        }
        buffer.clear();
        return buffer;
    }

    private IntBuffer getIntBuffer(int size) {
        IntBuffer buffer = indexPool.poll();
        if (buffer == null || buffer.capacity() < size) {
            buffer = BufferUtils.createIntBuffer(size);
        }
        buffer.clear();
        return buffer;
    }

    private void initGL() {
        try {
            vaoId = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaoId);

            vboVertex = GL15.glGenBuffers();
            vboTex = GL15.glGenBuffers();
            vboColor = GL15.glGenBuffers();
            vboIndices = GL15.glGenBuffers();

            // 配置顶点属性
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(0);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTex);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(1);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
            GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(2);

            GL30.glBindVertexArray(0);
            built = true;
        } catch (Exception e) {
            e.printStackTrace();
            // 降级处理
            built = false;
        }
    }

    public void addVertex(float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        // 检查缓冲区是否已满，如果满了则自动绘制并重置
        if (vertexCount >= MAX_VERTS - 1) {
            flush();
        }

        vertexBuffer.put(x).put(y).put(z);
        texBuffer.put(u).put(v);
        colorBuffer.put(r).put(g).put(b).put(a);
        vertexCount++;
    }

    /**
     * 立即绘制并清空缓冲区
     */
    public void flush() {
        if (vertexCount == 0 || !built) return;

        try {
            // 准备缓冲区用于读取
            vertexBuffer.flip();
            texBuffer.flip();
            colorBuffer.flip();

            // 上传数据到 VBO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTex);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texBuffer, GL15.GL_STREAM_DRAW);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColor);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STREAM_DRAW);

            // 渲染
            GL30.glBindVertexArray(vaoId);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);
            GL30.glBindVertexArray(0);

        } catch (Exception e) {
            e.printStackTrace();
            // 如果 VBO 失败，降级到传统绘制
            drawLegacy();
        }

        clearBuffers();
    }

    public void draw() {
        flush();
    }

    private void drawLegacy() {
        // 传统 OpenGL 绘制（兼容方案）
        try {
            vertexBuffer.flip();
            texBuffer.flip();
            colorBuffer.flip();

            GL11.glBegin(GL11.GL_QUADS);
            for (int i = 0; i < vertexCount; i++) {
                int vi = i * 3;
                int ti = i * 2;
                int ci = i * 4;

                GL11.glColor4f(colorBuffer.get(ci), colorBuffer.get(ci + 1),
                        colorBuffer.get(ci + 2), colorBuffer.get(ci + 3));
                GL11.glTexCoord2f(texBuffer.get(ti), texBuffer.get(ti + 1));
                GL11.glVertex3f(vertexBuffer.get(vi), vertexBuffer.get(vi + 1), vertexBuffer.get(vi + 2));
            }
            GL11.glEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearBuffers() {
        vertexBuffer.clear();
        texBuffer.clear();
        colorBuffer.clear();
        indicesBuffer.clear();
        vertexCount = 0;

        // 归还缓冲区到池中（异步，不阻塞主线程）
        Workers.IO.execute(() -> {
            if (vertexBuffer != null && vertexBuffer.capacity() <= MAX_VERTS * 3) {
                vertexPool.offer(vertexBuffer);
            }
            if (texBuffer != null && texBuffer.capacity() <= MAX_VERTS * 2) {
                texPool.offer(texBuffer);
            }
            if (colorBuffer != null && colorBuffer.capacity() <= MAX_VERTS * 4) {
                colorPool.offer(colorBuffer);
            }
            if (indicesBuffer != null && indicesBuffer.capacity() <= MAX_INDICES) {
                indexPool.offer(indicesBuffer);
            }
        });
    }

    public void dispose() {
        try {
            if (vboVertex != -1) GL15.glDeleteBuffers(vboVertex);
            if (vboTex != -1) GL15.glDeleteBuffers(vboTex);
            if (vboColor != -1) GL15.glDeleteBuffers(vboColor);
            if (vboIndices != -1) GL15.glDeleteBuffers(vboIndices);
            if (vaoId != -1) GL30.glDeleteVertexArrays(vaoId);
        } catch (Exception ignored) {}

        // 清空缓冲区池
        vertexPool.clear();
        texPool.clear();
        colorPool.clear();
        indexPool.clear();
    }
}