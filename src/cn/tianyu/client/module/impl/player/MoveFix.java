package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.Manager.RotationState;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;

/**
 * @Author: CN_tianyu
 */
public class MoveFix extends Module {
    public MoveFix() {
        super("MoveFix", "移动修复", Category.player);
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanValue groundOnly = new BooleanValue("ground-only", false);

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }
        if (this.groundOnly.getValue() && !mc.thePlayer.onGround) {
            return;
        }
        if (RotationState.isActived() && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }
}
