package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: CN_tianyu
 */
public final class ChunkTaskTracker {
    public static final ChunkTaskTracker INSTANCE = new ChunkTaskTracker();
    // 存储所有未完成的区块构建任务
    private final CopyOnWriteArrayList<ChunkGenTask> runningTasks = new CopyOnWriteArrayList<>();

    private ChunkTaskTracker() {}

    // 提交任务时注册
    public void track(ChunkGenTask task) {
        runningTasks.add(task);
    }

    // 任务执行完毕注销
    public void untrack(ChunkGenTask task) {
        runningTasks.remove(task);
    }

    // 获取所有活跃任务，用于视野外取消
    public List<ChunkGenTask> getAllRunning() {
        return new ArrayList<>(runningTasks);
    }
}