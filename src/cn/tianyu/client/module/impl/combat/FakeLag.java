//package cn.tianyu.client.module.impl.combat;
//
//import cn.tianyu.client.TYClient;
//import cn.tianyu.client.event.EventTarget;
//import cn.tianyu.client.event.events.Packet.EventPacket;
//import cn.tianyu.client.event.events.game.EventTick;
//import cn.tianyu.client.gui.clickgui.tianyu.NeverLoseClickGui;
//import cn.tianyu.client.module.Category;
//import cn.tianyu.client.module.Module;
//import cn.tianyu.client.module.impl.player.Blink;
//import cn.tianyu.client.util.impl.client.DebugUtil;
//import cn.tianyu.client.util.impl.client.PacketUtils;
//import cn.tianyu.client.value.impl.NumberValue;
//import net.minecraft.network.Packet;
//import net.minecraft.network.play.client.*;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentSkipListMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * @Author: CN_tianyu
// */
//public class FakeLag extends Module {
//    public NumberValue packetDelaySlider = new NumberValue("Packet delay", 200.0, 0.0, 1500.0, 20.0);
//    private final ConcurrentSkipListMap<Long, List<Packet<?>>> packetQueue = new ConcurrentSkipListMap<>();
//    private static final int MAX_QUEUE_LIMIT = 45;
//
//    public FakeLag() {
//        super("FakeLag", "FakeLag", Category.combat);
//    }
//
//    @Override
//    public String getSuffix() {
//        return packetDelaySlider.getValue().intValue() + "ms";
//    }
//
//    @Override
//    public void onEnable() {
//        if (mc.isSingleplayer()) {
//            DebugUtil.log("&cFake lag cannot be enabled in singleplayer.");
//            toggle();
//            return;
//        }
//        if (TYClient.moduleManager.getModule(Blink.class).isEnabled()) {
//            DebugUtil.log("&cCannot use fake lag with blink!");
//            toggle();
//            return;
//        }
//        packetQueue.clear();
//    }
//
//    @Override
//    public void onDisable() {
//        updatePacketQueue(true);
//    }
//
//    private void updatePacketQueue(boolean flush) {
//        if (packetQueue.isEmpty()) return;
//
//        if (flush) {
//            for (List<Packet<?>> packets : packetQueue.values()) {
//                for (Packet<?> packet : packets) {
//                    PacketUtils.sendPacketNoEvent(packet);
//                }
//            }
//            packetQueue.clear();
//            return;
//        }
//
//        long now = System.currentTimeMillis();
//        Iterator<Map.Entry<Long, List<Packet<?>>>> it = packetQueue.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<Long, List<Packet<?>>> entry = it.next();
//            if (now < entry.getKey()) break;
//
//            for (Packet<?> packet : entry.getValue()) {
//                PacketUtils.sendPacketNoEvent(packet);
//            }
//            it.remove();
//        }
//    }
//
//    private boolean shouldBlockPacket(Packet<?> packet) {
//        BackTrack backTrack = TYClient.moduleManager.getModule(BackTrack.class);
//
//        if (backTrack.isEnabled() && backTrack.target != null) {
//            return packet instanceof C03PacketPlayer;
//        }
//
//        if (packet instanceof C00PacketKeepAlive
//                || packet instanceof C0FPacketConfirmTransaction
//                || packet instanceof C01PacketChatMessage
//                || packet instanceof C02PacketUseEntity
//                || packet instanceof C09PacketHeldItemChange) {
//            return false;
//        }
//        return true;
//    }
//
//    @EventTarget
//    public void onPacketSent(EventPacket e) {
//        if (e.getState() != EventPacket.EventState.SEND) return;
//        if (mc.thePlayer == null || mc.theWorld == null || mc.isSingleplayer()) return;
//
//        int delay = packetDelaySlider.getValue().intValue();
//        if (delay <= 0 || e.isCancelled()) return;
//        if (!shouldBlockPacket(e.getPacket())) return;
//
//        if (packetQueue.size() > MAX_QUEUE_LIMIT) {
//            updatePacketQueue(true);
//        }
//
//        long execTime = System.currentTimeMillis() + delay;
//        packetQueue.computeIfAbsent(execTime, k -> new CopyOnWriteArrayList<>()).add(e.getPacket());
//        e.setCancelled(true);
//    }
//
//    @EventTarget
//    public void onTick(EventTick e) {
//        if (mc.theWorld == null) {
//            packetQueue.clear();
//            return;
//        }
//
//        if (mc.currentScreen instanceof NeverLoseClickGui) {
//            updatePacketQueue(true);
//            return;
//        }
//
//        updatePacketQueue(false);
//    }
//}