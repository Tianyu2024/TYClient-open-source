package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.PlayerUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Map;

/**
 * @Author: CN_tianyu
 */
public class NoWeb extends Module {
    public NoWeb() {
        super("NoWeb","无蜘蛛网等减速", Category.movement);
    }
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", "GrimAC", "Intave", "Vanilla");
    public final BooleanValue noDown = new BooleanValue("No Down",true);
    public final BooleanValue upAndDown = new BooleanValue("Up And Down",true);

    @EventTarget
    public void onMotion(EventMotion event) {
        setSuffix(mode.getValue());
        if (!mc.thePlayer.isInWeb) {
            return;
        }

        switch (mode.getValue()) {
            case "Vanilla":
                mc.thePlayer.isInWeb = false;
                break;
            case "GrimAC":
                Map<BlockPos, Block> searchBlock = PlayerUtils.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    }
                }
                mc.thePlayer.isInWeb = false;
                break;
            case "Intave":
                searchBlock = PlayerUtils.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        if (noDown.get()) {
                            if (upAndDown.get())
                                if (mc.gameSettings.keyBindSneak.isKeyDown())
                                    mc.thePlayer.motionY = -0.2;
                                else if (mc.gameSettings.keyBindJump.isKeyDown())
                                    mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 2 == 0 ? 0.2 : -0.01;
                                else
                                    mc.thePlayer.motionY = -0.01;
                            else
                                mc.thePlayer.motionY = -0.01;
                        }

                    }
                }
                break;
        }
    }
}
