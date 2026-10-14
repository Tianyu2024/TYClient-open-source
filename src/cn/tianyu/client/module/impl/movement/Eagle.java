package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import org.lwjglx.input.Keyboard;

/**
 * @Author: CN_tianyu
 */
public class Eagle extends Module {
    public Eagle(){
        super("Eagle","Eagle", Category.movement);
        setKey(Keyboard.KEY_V);
    }
    public static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    @EventTarget
    public void onUpdate(final EventMotion event) {
        if (event.isPre()) {
            if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
                if (mc.thePlayer.onGround) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }
            } else if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }

    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        mc.thePlayer.setSneaking(false);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        super.onDisable();
    }
}
