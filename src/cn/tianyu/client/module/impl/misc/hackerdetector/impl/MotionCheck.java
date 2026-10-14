//package cn.tianyu.client.module.impl.misc.hackerdetector.impl;
//
//import cn.tianyu.client.event.events.Packet.EventPacket;
//import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
//import cn.tianyu.client.util.impl.player.MoveUtil;
//import net.minecraft.entity.player.EntityPlayer;
//
//public class MotionCheck extends Check {
//
//    @Override
//    public String getName() {
//        return "Invalid motion";
//    }
//
//    @Override
//    public void onPacketReceive(EventPacket event, EntityPlayer player) {
//
//    }
//
//    @Override
//    public void onUpdate(EntityPlayer player) {
//        double base = MoveUtil.getBaseMoveSpeed(player);
//        double speed = Math.hypot(player.motionX, player.motionZ);
//        if (speed > (base * 1.25f) && player.hurtTime == 0) {
//            flag(player, "Too fast");
//        }
//
//        if (!player.onGround && !MoveUtil.isMoving(player) && player.motionY == 0.0D && player.offGroundTicks >= 5) {
//            flag(player, "Not moving on air for a long time");
//        }
//    }
//}