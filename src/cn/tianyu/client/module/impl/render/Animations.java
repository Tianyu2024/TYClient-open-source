package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.render.EventBlockAnimation;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class Animations extends Module {
    public static final ModeValue mode = new ModeValue("Mode", "Smooth",
            "Stella", "Middle", "1.7", "Lunar", "Exhi", "Exhi 2", "Exhi 3", "Exhi 4", "Exhi 5", "Shred", "Smooth", "Sigma", "Custom");
    public static final BooleanValue sneak = new BooleanValue("1.7 sneak", true);
    public final BooleanValue health = new BooleanValue("Health", true);
    public final BooleanValue rod = new BooleanValue("Rod", false);
    public final BooleanValue bow = new BooleanValue("Bow", false);
    public static final BooleanValue oldDamage = new BooleanValue("Old Damage", true);
    public static final NumberValue slowdown = new NumberValue("Swing Slowdown", 1, 1, 15, 1);
    public static final NumberValue x = new NumberValue("X", 0, -50, 50, 1);
    public static final NumberValue y = new NumberValue("Y", 0, -50, 50, 1);
    public static final NumberValue size = new NumberValue("Size", 0, -50, 50, 1);

    public Animations(){
        super("Animations","1.7防砍动画", Category.render);
    }

    @EventTarget
    public void onTick(EventTick event) {
        //this.setSuffix(this..getValue());
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        if (mc.thePlayer.getItemInUseCount() > 0) {
            boolean mouseDown = mc.gameSettings.keyBindAttack.isKeyDown() && mc.gameSettings.keyBindUseItem.isKeyDown();
            if (mouseDown && !mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
                this.swingItem(mc.thePlayer);
            }
        }
    }

    public void swingItem(EntityPlayerSP entityplayersp) {
        int swingAnimationEnd = entityplayersp.isPotionActive(Potion.digSpeed)
                ? (6 - (1 + entityplayersp.getActivePotionEffect(Potion.digSpeed).getAmplifier()))
                : (entityplayersp.isPotionActive(Potion.digSlowdown)
                ? (6 + (1 + entityplayersp.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2)
                : 6);
        if (!entityplayersp.isSwingInProgress || entityplayersp.swingProgressInt >= swingAnimationEnd / 2
                || entityplayersp.swingProgressInt < 0) {
            entityplayersp.swingProgressInt = -1;
            entityplayersp.isSwingInProgress = true;
            //mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
    }

}
