//package cn.tianyu.client.TianyuOptiFine.betterfps.impl;
//
//import net.minecraft.client.renderer.*;
//import net.minecraft.client.renderer.chunk.RenderChunk;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.renderer.vertex.VertexBuffer;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import org.lwjgl.opengl.GL11;
//import org.lwjgl.opengl.GL15;
//
//import java.nio.ByteBuffer;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class ChunkVBOThreadOptimizer {
//
//    // 独立VBO上传线程（单线程安全，避免GL上下文冲突）
//    private static final ExecutorService VBO_UPLOAD_THREAD = new ThreadPoolExecutor(
//            1, 1, 0L, TimeUnit.MILLISECONDS,
//            new LinkedBlockingQueue<>(8192),
//            r -> new Thread(r, "ChunkVBO-Upload"));
//
//    private static final AtomicBoolean isUploading = new AtomicBoolean(false);
//
//    /**
//     * 【原版区块编译完成后调用】
//     * 把WorldRenderer数据异步上传到VBO，不卡主线程
//     */
//    public static void uploadChunkVBOAsync(RenderChunk renderChunk, WorldRenderer renderer) {
//        // 确保RenderChunk有VBO
//        if (renderChunk.vertexBuffer == null) {
//            renderChunk.vertexBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
//        }
//
//        VertexBuffer vbo = renderChunk.vertexBuffer;
//        ByteBuffer buffer = renderer.getByteBuffer();
//        int drawMode = renderer.getDrawMode();
//
//        VBO_UPLOAD_THREAD.submit(() -> {
//            try {
//                // 1.8.9 正确写法：用GlStateManager绑定缓冲区
//                GlStateManager.bindBuffer(GL15.GL_ARRAY_BUFFER, vbo.glBufferId);
//                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
//                GlStateManager.bin(GL15.GL_ARRAY_BUFFER, 0);
//            } finally {
//                isUploading.set(false);
//            }
//        });
//    }
//
//    /**
//     * 【原版区块渲染时调用】
//     * 多线程安全的VBO绘制（1.8.9 GlStateManager兼容）
//     */
//    public static void renderChunkVBO(VertexBuffer vbo, int vertexCount, int drawMode) {
//        if (vbo == null || vertexCount <= 0) return;
//
//        // 1.8.9 正确状态保存（用GlStateManager，无pushAttrib）
//        GlStateManager.pushMatrix();
//        GlStateManager.enableTexture2D();
//        GlStateManager.enableAlpha();
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//        // 绑定VBO并绘制
//        GlStateManager.bindBuffer(GL15.GL_ARRAY_BUFFER, vbo.glBufferId);
//        vbo.getVertexFormat().setupBufferAttributes(0L);
//        GL11.glDrawArrays(drawMode, 0, vertexCount);
//        GlStateManager.bindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//
//        GlStateManager.popMatrix();
//    }
//
//    /**
//     * 游戏启动时调用一次
//     */
//    public static void initGlobalRenderState() {
//        GlStateManager.enableDepth();
//        GlStateManager.depthFunc(GL11.GL_LEQUAL);
//        GlStateManager.enableCull();
//        GlStateManager.cullFace(GL11.GL_BACK);
//        GlStateManager.shadeModel(GL11.GL_SMOOTH);
//        GlStateManager.enableColorMaterial();
//    }
//}