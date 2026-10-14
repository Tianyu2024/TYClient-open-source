package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventRotation;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;

/**
 * @Author: CN_tianyu
 */
public class FreeLook extends Module {
    public BooleanValue invertPitch = new BooleanValue("Invert Pitch", false);

    private int previousPerspective;
    public float originalYaw, originalPitch, lastYaw, lastPitch;

    @Override
    public void onEnable() {
        if(mc.thePlayer != null){
            previousPerspective = mc.gameSettings.thirdPersonView;
            originalYaw = lastYaw = mc.thePlayer.rotationYaw;
            originalPitch = lastPitch = mc.thePlayer.rotationPitch;

            if (invertPitch.getValue()) lastPitch *= -1;
        }
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer != null){
            mc.thePlayer.rotationYaw = originalYaw;
            mc.thePlayer.rotationPitch = originalPitch;
            mc.gameSettings.thirdPersonView = previousPerspective;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D e){

        if (this.getKey() == Keyboard.KEY_NONE || !Keyboard.isKeyDown(this.getKey())) {
            this.setEnable(false);
            return;
        }

        this.mc.mouseHelper.mouseXYChange();
        final float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        final float f1 = (float) (f * f * f * 1.5);
        lastYaw += this.mc.mouseHelper.deltaX * f1;
        lastPitch -= this.mc.mouseHelper.deltaY * f1;

        lastPitch = MathHelper.clamp_float(lastPitch, -90, 90);
        mc.gameSettings.thirdPersonView = 1;
    };

    @EventTarget
    public void R(EventRotation e) {
        originalYaw = e.getYaw();
        originalPitch = e.getPitch();
    };

    public FreeLook() {
        super("FreeLook", "自由视角", Category.useful);
        setKey(Keyboard.KEY_LMENU);
    }
}