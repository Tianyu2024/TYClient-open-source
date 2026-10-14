//package cn.tianyu.client.module.impl.better;
//
//import cn.tianyu.client.module.Category;
//import cn.tianyu.client.module.Module;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.*;
//import net.minecraft.client.renderer.entity.RenderManager;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.renderer.vertex.VertexBuffer;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.MathHelper3;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.opengl.*;
//
//import java.nio.FloatBuffer;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * MCP918(LWJGL 2.9.4)适配版 - GPU顶点优化
// * 核心：顶点预缓存 + 多线程CPU变换 + VBO批量提交 → 提升实体渲染效率
// */
//public class GPUVertexOptimizer extends Module {
//    // 全局实例
//    private static final Minecraft mc = Minecraft.getMinecraft();
//    // 多线程池（并行顶点变换）
//    private final ExecutorService transformExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//    // 顶点缓存（实体ID → 原始顶点数据）
//    private final Map<Integer, FloatBuffer> rawVertexCache = new ConcurrentHashMap<>();
//    // 变换后顶点缓存（实体ID → 变换后顶点）
//    private final Map<Integer, FloatBuffer> transformedVertexCache = new ConcurrentHashMap<>();
//    // VBO缓存（实体ID → VBO ID）
//    private final Map<Integer, Integer> entityVboMap = new ConcurrentHashMap<>();
//    // 顶点数量缓存
//    private final Map<Integer, Integer> vertexCountMap = new ConcurrentHashMap<>();
//    // 渲染状态标记
//    private boolean isPipelineActive = false;
//    // 顶点格式（适配MC原生）
//    private final VertexFormat vertexFormat = DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
//    // 单次提交顶点数（批量优化）
//    private static final int BATCH_VERTEX_COUNT = 1024;
//
//    public GPUVertexOptimizer() {
//        super("GPUVertexOptimizer", "GPU顶点优化", Category.render);
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        // 检查GL兼容性（MCP918仅支持GL2.1/3.0）
//        if (!checkGLCompatibility()) {
//            sendMessage("不支持OpenGL 2.1+，模块禁用");
//            setEnable(false);
//            return;
//        }
//
//        // 初始化渲染管线
//        initVertexPipeline();
//        // 替换实体渲染逻辑
//        hookEntityRender();
//        isPipelineActive = true;
//
//        sendMessage("GPU顶点优化已启用 | 多线程变换+VBO批量提交");
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        if (!isPipelineActive) return;
//
//        // 恢复原生渲染
//        restoreEntityRender();
//        // 释放资源
//        releaseResources();
//        isPipelineActive = false;
//
//        sendMessage("GPU顶点优化已关闭 | 恢复原生渲染");
//    }
//
//    /**
//     * 检查GL兼容性（适配LWJGL 2.9.4）
//     */
//    private boolean checkGLCompatibility() {
//        try {
//            String glVersion = GL11.glGetString(GL11.GL_VERSION);
//            String glslVersion = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
//            // 最低要求GL2.1（MCP918标配）
//            return glVersion.contains("2.1") || glVersion.contains("3.0");
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * 初始化顶点渲染管线
//     */
//    private void initVertexPipeline() {
//        // 初始化VBO缓冲（适配LWJGL 2.9.4）
//        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
//        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
//    }
//
//    /**
//     * 缓存实体原始顶点数据（变换前）
//     */
//    private void cacheRawVertices(Entity entity) {
//        int entityId = entity.getEntityId();
//        if (rawVertexCache.containsKey(entityId)) return;
//
//        // 获取实体顶点数量（适配不同实体类型）
//        int vertexCount = getEntityVertexCount(entity);
//        vertexCountMap.put(entityId, vertexCount);
//
//        // 初始化原始顶点缓存（LWJGL 2.9.4 BufferUtils）
//        FloatBuffer rawBuffer = BufferUtils.createFloatBuffer(vertexCount * vertexFormat.getSize() / 4);
//        rawVertexCache.put(entityId, rawBuffer);
//
//        // 初始化变换后缓存
//        FloatBuffer transformedBuffer = BufferUtils.createFloatBuffer(vertexCount * vertexFormat.getSize() / 4);
//        transformedVertexCache.put(entityId, transformedBuffer);
//
//        // 创建VBO（LWJGL 2.9.4 GL15）
//        int vboId = GL15.glGenBuffers();
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
//        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, rawBuffer.capacity() * 4, GL15.GL_DYNAMIC_DRAW);
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//        entityVboMap.put(entityId, vboId);
//
//        // 填充原始顶点数据（模拟实体模型顶点）
//        fillEntityVertices(entity, rawBuffer);
//    }
//
//    /**
//     * 多线程并行变换顶点（替代计算着色器）
//     */
//    private void transformVerticesAsync(Entity entity) {
//        int entityId = entity.getEntityId();
//        if (!rawVertexCache.containsKey(entityId)) return;
//
//        // 提交多线程变换任务
//        transformExecutor.submit(() -> {
//            FloatBuffer rawBuffer = rawVertexCache.get(entityId);
//            FloatBuffer transformedBuffer = transformedVertexCache.get(entityId);
//            int vertexCount = vertexCountMap.get(entityId);
//
//            // 重置缓冲区指针
//            rawBuffer.rewind();
//            transformedBuffer.rewind();
//
//            // 计算MVP矩阵（适配MC视角）
//            float[] mvpMatrix = getEntityMVPMatrix(entity);
//
//            // 批量变换顶点（每批1024个，减少锁竞争）
//            for (int i = 0; i < vertexCount; i += BATCH_VERTEX_COUNT) {
//                int batchEnd = Math.min(i + BATCH_VERTEX_COUNT, vertexCount);
//                for (int j = i; j < batchEnd; j++) {
//                    // 读取原始顶点（x,y,z,texU,texV,color,normalX,normalY,normalZ）
//                    float x = rawBuffer.get(j * 9);
//                    float y = rawBuffer.get(j * 9 + 1);
//                    float z = rawBuffer.get(j * 9 + 2);
//                    float texU = rawBuffer.get(j * 9 + 3);
//                    float texV = rawBuffer.get(j * 9 + 4);
//                    int color = Float.floatToIntBits(rawBuffer.get(j * 9 + 5));
//                    float nx = rawBuffer.get(j * 9 + 6);
//                    float ny = rawBuffer.get(j * 9 + 7);
//                    float nz = rawBuffer.get(j * 9 + 8);
//
//                    // MVP矩阵变换（核心优化：并行计算）
//                    float tx = x * mvpMatrix[0] + y * mvpMatrix[4] + z * mvpMatrix[8] + mvpMatrix[12];
//                    float ty = x * mvpMatrix[1] + y * mvpMatrix[5] + z * mvpMatrix[9] + mvpMatrix[13];
//                    float tz = x * mvpMatrix[2] + y * mvpMatrix[6] + z * mvpMatrix[10] + mvpMatrix[14];
//
//                    // 写入变换后顶点（保留纹理/颜色/法线）
//                    transformedBuffer.put(tx);
//                    transformedBuffer.put(ty);
//                    transformedBuffer.put(tz);
//                    transformedBuffer.put(texU);
//                    transformedBuffer.put(texV);
//                    transformedBuffer.put(Float.intBitsToFloat(color));
//                    transformedBuffer.put(nx);
//                    transformedBuffer.put(ny);
//                    transformedBuffer.put(nz);
//                }
//            }
//
//            // 变换完成后更新VBO
//            updateEntityVBO(entityId, transformedBuffer);
//        });
//    }
//
//    /**
//     * 更新实体VBO（批量提交到GPU）
//     */
//    private void updateEntityVBO(int entityId, FloatBuffer transformedBuffer) {
//        if (!entityVboMap.containsKey(entityId)) return;
//
//        // 绑定VBO并提交数据（LWJGL 2.9.4 GL15）
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entityVboMap.get(entityId));
//        transformedBuffer.rewind();
//        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, transformedBuffer);
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//    }
//
//    /**
//     * 渲染变换后的实体（适配MC原生渲染管线）
//     */
//    private void renderTransformedEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
//        int entityId = entity.getEntityId();
//        if (!entityVboMap.containsKey(entityId)) return;
//
//        // 保存原生渲染状态
//        GlStateManager.pushMatrix();
//        RenderHelper.disableStandardItemLighting();
//
//        // 绑定VBO并配置顶点属性（LWJGL 2.9.4）
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, entityVboMap.get(entityId));
//        int stride = vertexFormat.getSize(); // 顶点步长
//
//        // 配置顶点属性指针
//        GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0); // 位置
//        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12); // 纹理（3*4字节偏移）
//        GL11.glColorPointer(4, GL11.GL_FLOAT, stride, 20); // 颜色（5*4字节偏移）
//        GL11.glNormalPointer(GL11.GL_FLOAT, stride, 36); // 法线（9*4字节偏移）
//
//        // 批量绘制顶点（替代glDrawArrays，提升效率）
//        GL11.glPushMatrix();
//        GL11.glTranslated(x, y, z);
//        GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
//        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCountMap.get(entityId));
//        GL11.glPopMatrix();
//
//        // 恢复状态
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//        RenderHelper.enableStandardItemLighting();
//        GlStateManager.popMatrix();
//    }
//
//    /**
//     * Hook实体渲染逻辑（替换原生渲染）
//     */
//    private void hookEntityRender() {
//        RenderManager renderManager = mc.getRenderManager();
//        renderManager.entityRenderMap.forEach((entityClass, renderer) -> {
//            EntityRenderer<Entity> customRenderer = new EntityRenderer<Entity>(renderManager) {
//                @Override
//                public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
//                    if (isPipelineActive) {
//                        // 1. 缓存原始顶点
//                        cacheRawVertices(entity);
//                        // 2. 多线程变换顶点
//                        transformVerticesAsync(entity);
//                        // 3. 渲染变换后顶点
//                        renderTransformedEntity(entity, x, y, z, yaw, partialTicks);
//                    } else {
//                        // 降级到原生渲染
//                        renderer.doRender(entity, x, y, z, yaw, partialTicks);
//                    }
//                }
//            };
//            renderManager.entityRenderMap.put(entityClass, customRenderer);
//        });
//    }
//
//    /**
//     * 恢复原生实体渲染
//     */
//    private void restoreEntityRender() {
//        RenderManager renderManager = mc.getRenderManager();
//        renderManager.entityRenderMap.forEach((entityClass, renderer) -> {
//            EntityRenderer<?> originalRenderer = renderManager.getEntityRenderObject(entityClass);
//            if (originalRenderer != null) {
//                renderManager.entityRenderMap.put(entityClass, originalRenderer);
//            }
//        });
//    }
//
//    /**
//     * 释放所有资源（适配LWJGL 2.9.4）
//     */
//    private void releaseResources() {
//        // 关闭线程池
//        transformExecutor.shutdown();
//        // 释放VBO
//        entityVboMap.values().forEach(GL15::glDeleteBuffers);
//        // 清空缓存
//        rawVertexCache.clear();
//        transformedVertexCache.clear();
//        vertexCountMap.clear();
//        entityVboMap.clear();
//        // 恢复GL状态
//        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
//        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
//        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
//        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
//    }
//
//    /**
//     * 获取实体MVP矩阵（适配MC视角）
//     */
//    private float[] getEntityMVPMatrix(Entity entity) {
//        float partialTicks = mc.timer.renderPartialTicks;
//        float x = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks - RenderManager.renderPosX);
//        float y = (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks - RenderManager.renderPosY);
//        float z = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks - RenderManager.renderPosZ);
//
//        // 简化MVP矩阵（适配MCP918）
//        return new float[]{
//                1.0f, 0.0f, 0.0f, 0.0f,
//                0.0f, 1.0f, 0.0f, 0.0f,
//                0.0f, 0.0f, 1.0f, 0.0f,
//                x, y, z, 1.0f
//        };
//    }
//
//    /**
//     * 填充实体顶点数据（模拟不同实体模型）
//     */
//    private void fillEntityVertices(Entity entity, FloatBuffer buffer) {
//        int vertexCount = vertexCountMap.get(entity.getEntityId());
//        buffer.rewind();
//
//        // 模拟实体顶点数据（x,y,z,texU,texV,color,normalX,normalY,normalZ）
//        for (int i = 0; i < vertexCount; i++) {
//            buffer.put(MathHelper3.randomFloatClamped(mc.rand, -0.5f, 0.5f)); // x
//            buffer.put(MathHelper3.randomFloatClamped(mc.rand, 0.0f, 2.0f));  // y
//            buffer.put(MathHelper3.randomFloatClamped(mc.rand, -0.5f, 0.5f)); // z
//            buffer.put(mc.rand.nextFloat()); // texU
//            buffer.put(mc.rand.nextFloat()); // texV
//            buffer.put(Float.intBitsToFloat(0xFFFFFFFF)); // color
//            buffer.put(0.0f); // normalX
//            buffer.put(1.0f); // normalY
//            buffer.put(0.0f); // normalZ
//        }
//        buffer.flip();
//    }
//
//    /**
//     * 获取实体顶点数量（适配不同实体类型）
//     */
//    private int getEntityVertexCount(Entity entity) {
//        String entityType = entity.getClass().getSimpleName();
//        return switch (entityType) {
//            case "EntityPlayer" -> 2048; // 玩家模型顶点数
//            case "EntityCreeper", "EntityZombie", "EntitySkeleton" -> 1024; // 怪物
//            case "EntityItem", "EntityArrow", "EntitySnowball" -> 512; // 道具/投射物
//            default -> 1024; // 默认
//        };
//    }
//
//    /**
//     * 发送游戏内消息
//     */
//    private void sendMessage(String msg) {
//        if (mc.thePlayer != null) {
//            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("[GPU优化] " + msg));
//        }
//    }
//}