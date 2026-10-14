package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import cn.tianyu.client.util.impl.Workers;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @Author: CN_tianyu
 */
public class AsyncVisibility {
    private static Future<List<RenderChunk>> visibilityTask;

    public static void updateVisibilityAsync(List<RenderChunk> chunks, BlockPos playerPos, float fov) {
        if (visibilityTask != null && !visibilityTask.isDone()) {
            visibilityTask.cancel(true);
        }

        visibilityTask = Workers.Default.submit(() -> {
            List<RenderChunk> visible = new ArrayList<>();

            for (RenderChunk chunk : chunks) {
                if (Thread.interrupted()) break;

                if (isChunkVisible(chunk, playerPos, fov)) {
                    visible.add(chunk);
                }
            }

            return visible;
        });
    }

    public static List<RenderChunk> getVisibleChunks() {
        if (visibilityTask != null && visibilityTask.isDone()) {
            try {
                return visibilityTask.get();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static boolean isChunkVisible(RenderChunk chunk, BlockPos pos, float fov) {
        double dx = chunk.getPosition().getX() + 8 - pos.getX();
        double dz = chunk.getPosition().getZ() + 8 - pos.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 64) return false;

        double angle = Math.toDegrees(Math.atan2(dz, dx));
        float yaw = MathHelper.wrapAngleTo180_float((float) angle - pos.getX());

        return Math.abs(yaw) < fov / 2;
    }
}