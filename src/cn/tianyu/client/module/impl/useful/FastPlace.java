package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.BlockUtil;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author CN_tianyu
 */

public class FastPlace extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat df = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));
    public final NumberValue delay = new NumberValue("delay", 1.0F, 1.0F, 3.0F, 1.0f);
    public final BooleanValue blocksOnly = new BooleanValue("blocks-only", true);
    public final BooleanValue placeFix = new BooleanValue("place-fix", true);
    public final BooleanValue skipObsidian = new BooleanValue("skip-obsidian", true);
    public final BooleanValue skipInteractable = new BooleanValue("skip-interactable", true);
    private long delayMS = 0L;
    public FastPlace() {
        super("FastPlace", "快速放置", Category.useful);
    }

    private boolean canPlace() {
        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack != null) {
            Item item = stack.getItem();
            if (item instanceof ItemFishingRod) {
                return false;
            }
            if (item instanceof ItemBlock) {
                Block block = ((ItemBlock) item).getBlock();
                if (skipObsidian.getValue() && block instanceof BlockObsidian) {
                    return false;
                }
                if (skipInteractable.getValue() && BlockUtil.isInteractable(block)) {
                    return false;
                }
                if (!(Boolean) this.placeFix.getValue()) {
                    return true;
                }
                MovingObjectPosition mop = RotationUtil.rayTrace(
                        mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.playerController.getBlockReachDistance(), 1.0F
                );
                return mop != null
                        && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                        && ((ItemBlock) item).canPlaceBlockOnSide(mc.theWorld, mop.getBlockPos(), mop.sideHit, mc.thePlayer, stack);
            }
        }
        return !(Boolean) this.blocksOnly.getValue();
    }

    @EventTarget
    public void onTick(EventMotion event) {
        if (this.isEnabled() && event.getEventState() == EventMotion.EventState.PRE) {
            int rightClickDelayTimer = mc.getRightClickDelayTimer();
            if (rightClickDelayTimer == 4) {
                this.delayMS = this.delayMS + (long) (50.0F * this.delay.getValue());
            }
            if (this.delayMS > 0L) {
                this.delayMS = this.delayMS - 50;
            }
            if (this.delayMS <= 0L && rightClickDelayTimer > 1 && this.canPlace()) {
                mc.setRightClickDelayTimer(0);
            }
        }
    }

    @Override
    public void onDisable() {
        this.delayMS = 0L;
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

//    @Override
//    public String[] getSuffix() {
//        return new String[]{df.format(this.delay.getValue())};
//    }

    @Override
    public void onEnable() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

}
