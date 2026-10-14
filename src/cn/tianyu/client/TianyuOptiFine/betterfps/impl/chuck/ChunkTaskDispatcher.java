package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import cn.tianyu.client.util.impl.Workers;

/**
 * @Author: CN_tianyu
 */
public final class ChunkTaskDispatcher {
    private ChunkTaskDispatcher() {}

    public static void submit(ChunkGenTask task) {
        // 注册任务到追踪器，方便后续批量取消
        ChunkTaskTracker.INSTANCE.track(task);
        // 提交到你自带的默认CPU线程池
        Workers.Default.submit(() -> {
            try {
                task.run();
            } finally {
                // 执行完成移除追踪
                ChunkTaskTracker.INSTANCE.untrack(task);
            }
        });
    }
}