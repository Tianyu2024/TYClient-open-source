package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.Manager.impl.TimedPacket;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import de.gerrygames.viarewind.utils.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BackTrack extends Module {

    public BackTrack() {
        super("Backtrack", "回溯", Category.combat);
    }

    private static final Minecraft mc = Minecraft.getMinecraft();
    private final NumberValue minLatency = new NumberValue("MinMS", 50, 10, 1000, 10);
    private final NumberValue maxLatency = new NumberValue("MaxMS", 100, 10, 1000, 10);
    private final NumberValue minDistance = new NumberValue("MinDistance", 0.0F, 0.0F, 3.0F, 1.0f);
    private final NumberValue maxDistance = new NumberValue("MaxDistance", 6.0F, 0.0F, 10.0F, 1.0f);
    private final NumberValue stopOnTargetHurtTime = new NumberValue("PlayerHurtTime", -1, -1, 10, 1);
    private final NumberValue stopOnSelfHurtTime = new NumberValue("StopOnSelfHurtTime", -1, -1, 10, 1);
    private final BooleanValue drawRealPosition = new BooleanValue("DrawRealPosition", true);
    private final NumberValue red = new NumberValue("Red", 255, 0, 255, 1, drawRealPosition::getValue);
    private final NumberValue green = new NumberValue("Green", 255, 0, 255, 1, drawRealPosition::getValue);
    private final NumberValue blue = new NumberValue("Blue", 255, 0, 255, 1, drawRealPosition::getValue);
    private final NumberValue alpha = new NumberValue("Alpha", 255, 0, 255, 1, drawRealPosition::getValue);

    private final Queue<PacketEntry> packetQueue = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> skipPackets = new ArrayList<>();

    private Vec3 vec3;
    private EntityPlayer target;
    private int currentLatency = 0;

    private static class PacketEntry {
        private final Packet<?> packet;
        private final long releaseTime;

        public PacketEntry(Packet<?> packet, long releaseTime) {
            this.packet = packet;
            this.releaseTime = releaseTime;
        }

        public Packet<?> getPacket() { return packet; }
        public boolean isReady() { return System.currentTimeMillis() >= releaseTime; }
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        skipPackets.clear();
        vec3 = null;
        target = null;
        currentLatency = 0;
    }

    @Override
    public void onDisable() {
        releaseAll();
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (!isEnabled() || e.getEventState() == EventMotion.EventState.POST || target == null) return;
        try {
            final double distance = vec3.distanceTo(mc.thePlayer.getPositionVector());
            if (distance > maxDistance.getValue() || distance < minDistance.getValue()) {
                currentLatency = 0;
            }
        } catch (NullPointerException ignored) {}
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (!isEnabled() || e.getType() == EventTick.EventType.POST) return;

        Iterator<PacketEntry> iterator = packetQueue.iterator();
        while (iterator.hasNext()) {
            PacketEntry entry = iterator.next();
            if (entry.isReady()) {
                Packet<?> packet = entry.getPacket();
                skipPackets.add(packet);
                PacketUtils.receivePacket(packet);
                iterator.remove();
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            vec3 = target.getPositionVector();
        }
    }

    @EventTarget
    public void onAttack(EventAttack e) {
        if (!isEnabled()) return;
        final Vec3 targetPos = e.getTarget().getPositionVector();
        if (e.getTarget() instanceof EntityPlayer) {
            if (target == null || e.getTarget() != target) {
                vec3 = targetPos;
            }
            target = (EntityPlayer) e.getTarget();

            try {
                final double distance = targetPos.distanceTo(mc.thePlayer.getPositionVector());
                if (distance > maxDistance.getValue() || distance < minDistance.getValue())
                    return;
            } catch (NullPointerException ignored) {}

            currentLatency = (int) (Math.random() * (maxLatency.getValue() - minLatency.getValue()) + minLatency.getValue());
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (target == null || vec3 == null || target.isDead || !isEnabled()) return;
        final Vec3 pos = currentLatency > 0 ? vec3 : target.getPositionVector();
        if (drawRealPosition.getValue()) drawBox(pos);
    }

    @EventTarget
    public void onReceivePacket(EventPacket e) {
        if (!isEnabled() || e.getEventType() == EventPacket.EventState.SEND) return;
        Packet<?> p = e.getPacket();
        if (skipPackets.contains(p)) {
            skipPackets.remove(p);
            return;
        }

        if (target != null && stopOnTargetHurtTime.getValue() != -1 && target.hurtTime == stopOnTargetHurtTime.getValue()) {
            releaseAll();
            return;
        }
        if (stopOnSelfHurtTime.getValue() != -1 && mc.thePlayer.hurtTime == stopOnSelfHurtTime.getValue()) {
            releaseAll();
            return;
        }

        try {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
                packetQueue.clear();
                return;
            }

            if (target == null) {
                releaseAll();
                return;
            }

            if (e.isCancelled()) return;

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth) {
                return;
            }

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                releaseAll();
                target = null;
                vec3 = null;
                return;
            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    if (id == target.getEntityId()) {
                        target = null;
                        vec3 = null;
                        releaseAll();
                        return;
                    }
                }
                return;
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    vec3 = vec3.add(new Vec3(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D,
                            wrapper.func_149064_e() / 32.0D));
                    long releaseTime = System.currentTimeMillis() + currentLatency;
                    packetQueue.add(new PacketEntry(p, releaseTime));
                    e.setCancelled(true);
                }
                return;
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                    long releaseTime = System.currentTimeMillis() + currentLatency;
                    packetQueue.add(new PacketEntry(p, releaseTime));
                    e.setCancelled(true);
                }
                return;
            }
        } catch (NullPointerException ignored) {}
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            for (PacketEntry entry : packetQueue) {
                Packet<?> packet = entry.getPacket();
                skipPackets.add(packet);
                PacketUtils.receivePacket(packet);
            }
            packetQueue.clear();
        }
        currentLatency = 0;
    }

    public static final int color = new Color(255, 255, 255, 200).getRGB();

    public void drawBox(@NotNull Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);

        float r = red.getValue().floatValue() / 255.0F;
        float g = green.getValue().floatValue() / 255.0F;
        float b = blue.getValue().floatValue() / 255.0F;
        float a = alpha.getValue().floatValue() / 255.0F;

        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB abb, float r, float g, float b) {
        drawBoundingBox(abb, r, g, b, 0.25f);
    }

    public static void drawBoundingBox(@NotNull AxisAlignedBB abb, float r, float g, float b, float a) {
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer vb = ts.getWorldRenderer();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
    }
}