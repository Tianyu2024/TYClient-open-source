package cn.tianyu.client.gui.music;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class MusicManager {

    // 播放状态枚举
    public enum PlayState {
        PLAYING, PAUSED, STOPPED
    }

    private PlayState currentState = PlayState.STOPPED;
    private float volume = 0.5f; // 范围 0.0f 到 1.0f
    private float playSpeed = 1.0f; // 倍速，如 1.0, 1.25, 1.5, 2.0
    private int currentProgress = 0; // 当前播放进度（秒）
    private int totalDuration = 180; // 歌曲总时长（秒）

    private String currentSongId = "";

    public MusicManager() {
        // 初始化播放器
    }

    // ==================== 播放控制方法 ====================

    public void play(String songId) {
        this.currentSongId = songId;
        this.currentState = PlayState.PLAYING;
        // TODO: 调用音频播放库（如 JLayer / JavaFX Media）开始播放音乐
    }

    public void pause() {
        if (currentState == PlayState.PLAYING) {
            currentState = PlayState.PAUSED;
            // TODO: 暂停音频流
        }
    }

    public void resume() {
        if (currentState == PlayState.PAUSED) {
            currentState = PlayState.PLAYING;
            // TODO: 恢复音频流
        }
    }

    public void stop() {
        currentState = PlayState.STOPPED;
        currentProgress = 0;
        // TODO: 停止音频流
    }

    // ==================== 属性设置与获取 ====================

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume)); // 限制在 0-1 之间
        // TODO: 调节底层音频流音量
    }

    public void setPlaySpeed(float speed) {
        this.playSpeed = speed;
        // TODO: 调节音频播放速率（需要支持 SoundStretch 或 JavaFX 的 rate 属性）
    }

    // ==================== 开放接口：供其它地方调用 ====================

    /**
     * 获取当前或指定歌曲的歌词列表
     * @param songId 歌曲唯一标识或路径
     * @return 歌词文本行列表（可包含时间戳，或纯文本）
     */
    public List<String> getSongLyrics(String songId) {
        List<String> lyrics = new ArrayList<>();
        // TODO: 在这里编写获取歌词的逻辑（读本地 .lrc 文件，或者请求网络 API）
        // 示例数据：
        lyrics.add("[00:00.00] 歌曲名 - 艺术家");
        lyrics.add("[00:15.00] 这是一个美丽的音乐播放器...");
        return lyrics;
    }

    /**
     * 获取当前或指定歌曲的封面图片
     * @param songId 歌曲唯一标识或路径
     * @return BufferedImage 供 MC 渲染引擎或 GUI 转换为 Texture
     */
    public BufferedImage getSongCoverImage(String songId) {
        // TODO: 在这里编写获取封面图的逻辑（读取 MP3 内嵌图片，或下载网络图片）
        // 建议：在此处下载/加载成功后，将其转换为 Minecraft 的 ResourceLocation 缓存起来以便渲染
        return null;
    }

    // Getters & Setters
    public PlayState getCurrentState() { return currentState; }
    public float getVolume() { return volume; }
    public float getPlaySpeed() { return playSpeed; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTotalDuration() { return totalDuration; }
    public String getCurrentSongId() { return currentSongId; }
}