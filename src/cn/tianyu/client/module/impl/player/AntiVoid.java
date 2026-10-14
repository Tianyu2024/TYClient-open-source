package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.render.EventPostRender;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: CN_tianyu
 */
public class AntiVoid extends Module {

    private static NumberValue distance = new NumberValue("Distance", 5, 1, 10, 0.5);
    private final BooleanValue renderTimer = new BooleanValue("Render Timer", false);
    private final BooleanValue disablePractice = new BooleanValue("Disable in Practice", false);

    public ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();
    private int color = new Color(0, 187, 255, 255).getRGB();
    public int blinkTicks;
    public boolean started, wait;
    public double y;

    public AntiVoid() {
        super("AntiVoid", "防虚空", Category.player);
    }

    @Override
    public void onEnable() {
        blinkedPackets.clear();
        blinkTicks = 0;
        started = false;
        wait = false;
    }

    @Override
    public void onDisable() {
        release();
    }

    public boolean spectatorCheck() {
        return mc.thePlayer.inventory.getStackInSlot(8) != null && mc.thePlayer.inventory.getStackInSlot(8).getDisplayName().contains("Return");
    }

    public boolean isBedwarsPractice() {
        if (!checknull()) return false;
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;
        return stripString(objective.getDisplayName()).contains("PRACTICE");
    }

    public static String stripString(final String s) {
        if (s == null) return "";
        final char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder validated = new StringBuilder();
        for (final char c : nonValidatedString) {
            if (c < '' && c > '') {
                validated.append(c);
            }
        }
        return validated.toString();
    }

    @EventTarget
    public void onSendPacket(EventPacket e) {
        if (e.getState() == EventPacket.EventState.RECEIVE) return;
        if (checknull()) return;
        if(e.getPacket() == null){
            return;
        }
        Packet packet = e.getPacket();

        if (!started && (!overVoid() || mc.thePlayer.onGround)) {
            if(mc.thePlayer == null) {
                return;
            }
            y = mc.thePlayer.posY;
            wait = false;
            return;
        }

        if (spectatorCheck() || (isBedwarsPractice() && disablePractice.getValue())) {
            return;
        }

        if (mc.thePlayer.ticksExisted <= 10) return;
        if (packet instanceof C00PacketLoginStart || packet instanceof C00Handshake) return;
        if (wait) return;

        if (!e.isCancelled() && overVoid() && !mc.thePlayer.onGround) {
            started = true;
            blinkedPackets.add(packet);
            e.setCancelled(true);
        }
    }

    public boolean isReplay() {
        if (!checknull()) return false;
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;
        return stripString(objective.getDisplayName()).contains("REPLAY");
    }

    public static boolean overVoid() {
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        for (int i = (int) mc.thePlayer.posY; i > -100; i--) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ);
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    @EventTarget
    public void onPreUpdate(EventMotion e) {
        if (e.isPost()) return;
        if (!checknull()) return;

        setSuffix(blinkTicks + "");

        if (!overVoid() || mc.thePlayer.onGround) {
            release();
            return;
        }

        if (started) {
            blinkTicks++;

            if (mc.thePlayer.posY <= y - distance.getValue()) {
                posPacket();
                release();
                wait = true;
            }
        }
    }

    @EventTarget
    public void onRenderTick(EventPostRender ev) {
        if (!checknull() || !renderTimer.getValue() || blinkTicks == 0) return;

        handleTimer(color, blinkTicks);
    }

    public static void handleTimer(int color, int ticks) {
        color = new Color(130, 215, 223).getRGB();
        String text = String.valueOf(ticks);
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int midX = scaledResolution.getScaledWidth() / 2;
        int midY = scaledResolution.getScaledHeight() / 2;
        mc.fontRendererObj.drawString(text, midX - mc.fontRendererObj.getStringWidth(text) / 2, midY + 8, color, true);
    }

    public void posPacket() {
        PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY + 3.5,
                mc.thePlayer.posZ,
                true
        ));
    }

    public void release() {
        for (Packet packet : blinkedPackets) {
            try {
                PacketUtils.sendPacketNoEvent(packet);
            } catch (Exception ignored) {}
        }
        blinkedPackets.clear();
        blinkTicks = 0;
        started = false;
        wait = false;
    }

    public static double distanceToGround(Entity entity, int x, int z) {
        if (entity.onGround) return 0;
        double fallDistance = -1;
        double y = entity.posY;
        if (entity.posY % 1 == 0) y--;

        for (int i = (int) Math.floor(y); i > -100; i--) {
            BlockPos pos = new BlockPos(x, i, z);
            if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir)) {
                fallDistance = y - i;
                break;
            }
        }
        return fallDistance <= 0 ? 100 : fallDistance;
    }
}