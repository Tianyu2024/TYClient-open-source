//package cn.tianyu.client.module.impl.better;
//
//import cn.tianyu.client.module.Category;
//import cn.tianyu.client.module.Module;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.WorldRenderer;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.renderer.vertex.VertexBuffer;
//import org.lwjgl.opengl.GL11;
//
//public class VertexOpt extends Module {
//
//    private final Minecraft mc = Minecraft.getMinecraft();
//    private VertexBuffer buffer;
//    private boolean isBatching;
//    private final Runnable renderHook;
//
//    public VertexOpt() {
//        super("VertexOpt", "顶点缓冲优化", Category.better);
//        this.renderHook = this::tickRender;
//    }
//
//    @Override
//    public void onEnable() {
//        if (buffer == null) {
//            buffer = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR_TEX_NORMAL);
//        }
//        isBatching = false;
//        // 挂载到游戏主循环，不依赖事件
//        mc.addScheduledTask(renderHook);
//    }
//
//    @Override
//    public void onDisable() {
//        flush();
//        if (buffer != null) {
//            buffer.reset();
//            buffer.unbindBuffer();
//        }
//    }
//
//    private void tickRender() {
//        if (!this.isEnabled()) return;
//
//        // 每一帧渲染前开启批量缓冲
//        if (!isBatching) {
//            startBatch();
//        }
//
//        // 帧结束统一提交
//        mc.ingameGUI.drawScreen(0);
//        flush();
//
//        // 循环挂载
//        mc.addScheduledTask(renderHook);
//    }
//
//    private void startBatch() {
//        isBatching = true;
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
//
//        // 复用全局顶点缓冲，不替换原版渲染
//        worldRenderer.setVertexBuffer(buffer);
//
//        // 固定GL状态，减少频繁切换损耗
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//    }
//
//    private void flush() {
//        if (!isBatching) return;
//        isBatching = false;
//
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
//
//        // 批量提交顶点数据
//        if (worldRenderer.isDrawing) {
//            tessellator.draw();
//        }
//
//        // 解绑缓冲，完全交还给原版MC
//        worldRenderer.setVertexBuffer(null);
//        buffer.unbindBuffer();
//
//        // 恢复原生GL状态
//        GL11.glDisable(GL11.GL_BLEND);
//    }
//}