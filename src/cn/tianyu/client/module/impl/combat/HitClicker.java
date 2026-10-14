package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.ModuleManager;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import com.yumegod.obfuscation.Native;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

/**
 * @Author: CN_tianyu (HitClicker / Attack Rhythm Controller)
 */
public class HitClicker extends Module {
    public HitClicker() {
        super("HitClicker", "打击节拍器", Category.combat);
    }

    public static NumberValue minCps = new NumberValue("Min CPS", 8.0, 1.0, 20.0, 1.0);
    public static NumberValue maxCps = new NumberValue("Max CPS", 12.0, 1.0, 20.0, 1.0);
    public static BooleanValue syncAim = new BooleanValue("Sync AimAssist", true); // 仅在自瞄锁定目标时出刀

    private final Random random = new Random();
    private long nextClickTime = 0L;

    @Native
    @EventTarget
    @SuppressWarnings("unused")
    public void onTick(EventTick event) {
        if (!isEnable()) return;

        // 如果开启了同步自瞄，先检查自瞄模块是否找到了有效目标
        if (syncAim.getValue()) {
            AimAssist aimModule = (AimAssist) TYClient.getModuleManager().getModule(AimAssist.class);
            if (aimModule == null || !aimModule.isEnable() || aimModule.getTarget() == null) {
                return;
            }
        }

        // 检查玩家当前鼠标指向（如果是空气且没按住攻击键，则不出刀，模拟正常合规行为）
        if (mc.currentScreen != null) return;
        if (!mc.gameSettings.keyBindAttack.isKeyDown() && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            return;
        }

        // 核心节拍器控制：时间戳判断
        long currentTime = System.currentTimeMillis();
        if (currentTime >= nextClickTime) {

            // 1. 触发一次标准的攻击左键
            this.sendClickPacket();

            // 2. 动态计算下一次出刀的延迟“节拍” (引入 Jitter 概念)
            double min = minCps.getValue();
            double max = maxCps.getValue();
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }

            // 在设定的 CPS 范围内随机取一个频次
            double randomCps = min + (random.nextDouble() * (max - min));

            // 加上高斯扰动，使打击间隔（时间差）呈现钟形曲线分布，消除固定频率检测
            long baseDelay = (long) (1000.0 / randomCps);
            long jitter = (long) (random.nextGaussian() * (baseDelay * 0.12));

            this.nextClickTime = currentTime + Math.max(50L, baseDelay + jitter);
        }
    }

    /**
     * 模拟客户端发包与手部点击动作
     */
    private void sendClickPacket() {
        // 模拟鼠标左键按下效果
        KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

        // 触发手部挥动动画（不仅是视觉，反作弊会检测 C0APacketAnimation）
        mc.thePlayer.swingItem();

        // 如果准心恰好指着实体，则触发攻击流
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Entity target = mc.objectMouseOver.entityHit;
            if (target != null && target.isEntityAlive()) {
                mc.playerController.attackEntity(mc.thePlayer, target);
            }
        }
    }
}