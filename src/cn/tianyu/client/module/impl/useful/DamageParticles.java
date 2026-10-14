package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.event.events.world.EventEntityUpdate;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class DamageParticles extends Module {
    public DamageParticles(){
        super("DamageParticles","显示打击伤害", Category.useful);
    }
    private final Object2FloatOpenHashMap<EntityLivingBase> healthMap = new Object2FloatOpenHashMap<>();
    private final ArrayDeque<Particles> particles = new ArrayDeque<>();

    @EventTarget
    public void onUpdate(EventEntityUpdate e) {
        if (mc.theWorld == null || mc.thePlayer == null || e == null || e.getEntity() == null) {
            return;
        }

        EntityLivingBase entity = e.getEntity();
        if (entity.isDead || entity == mc.thePlayer) return;

        float previousHealth = healthMap.computeIfAbsent(entity, k -> entity.getHealth());
        float currentHealth = entity.getHealth();

        if (Math.abs(currentHealth - previousHealth) < 0.5f) {
            healthMap.put(entity, currentHealth);
            return;
        }

        final float healthDelta = currentHealth - previousHealth;
        final boolean isHeal = healthDelta > 0;
        final boolean isCrit = checkCritical(entity);

        spawnDamageIndicator(entity, healthDelta, isHeal, isCrit);
        healthMap.put(entity, currentHealth);
    }

    private boolean checkCritical(EntityLivingBase target) {
        return target.hurtResistantTime < 18 ||
                (mc.thePlayer.motionY < -0.2 && !mc.thePlayer.onGround);
    }

    private void spawnDamageIndicator(EntityLivingBase entity, float delta, boolean isHeal, boolean isCrit) {
        final String color = isHeal ? "§a" : (isCrit ? "§4" : "§6");

        // 检查delta是否为有效数字
        if (Float.isNaN(delta)) {
            return;
        }

        // 格式化数字并检查结果
        String formatted = String.format("%.1f", Math.abs(delta));
        if (!formatted.matches("^[0-9.]+$")) {  // 检查是否只包含数字和点
            return;
        }

        final String text = color + formatted;

        AxisAlignedBB bb = entity.getEntityBoundingBox();
        double centerX = bb.minX + (bb.maxX - bb.minX) / 2;
        double centerY = bb.minY + (bb.maxY - bb.minY) / 2;
        double centerZ = bb.minZ + (bb.maxZ - bb.minZ) / 2;

        double randAngle = Math.random() * Math.PI * 2;
        double offsetX = Math.cos(randAngle) * 0.4;
        double offsetZ = Math.sin(randAngle) * 0.4;

        Location particleLoc = new Location(
                centerX + offsetX,
                centerY,
                centerZ + offsetZ,
                entity.rotationYaw,
                entity.rotationPitch
        );

        synchronized (particles) {
            particles.add(new Particles(particleLoc, text));
        }
    }

    @EventTarget
    public void onRender(EventRender3D e) {
        for (Particles p : this.particles) {
            double x = p.location.getX();
            double n = x - mc.getRenderManager().renderPosX;
            double y = p.location.getY();
            double n2 = y - mc.getRenderManager().renderPosY;
            double z = p.location.getZ();
            double n3 = z - mc.getRenderManager().renderPosZ;
            GlStateManager.pushMatrix();
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
            GlStateManager.translate((float) n, (float) n2, (float) n3);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            float textY = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(mc.getRenderManager().playerViewX, textY, 0.0f, 0.0f);
            final double size = 0.03;
            GlStateManager.scale(-size, -size, size);
            GL11.glDepthMask(false);
            FontManager.icontestFont40.drawStringWithShadow(p.text, (float) -(mc.fontRendererObj.getStringWidth(p.text) / 2), (float) -(mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            FontManager.icontestFont40.drawStringWithShadow(p.text, (float) -(mc.fontRendererObj.getStringWidth(p.text) / 2), (float) -(mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            //mc.fontRendererObj.drawStringWithShadow(p.text, (float) -(mc.fontRendererObj.getStringWidth(p.text) / 2), (float) -(mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            //mc.fontRendererObj.drawStringWithShadow(p.text, (float) -(mc.fontRendererObj.getStringWidth(p.text) / 2), (float) -(mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDepthMask(true);
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static double roundToPlace(double p_roundToPlace_0_, int p_roundToPlace_2_) {
        if (p_roundToPlace_2_ < 0) throw new IllegalArgumentException();
        return new BigDecimal(p_roundToPlace_0_).setScale(p_roundToPlace_2_, RoundingMode.HALF_UP).doubleValue();
    }

    @EventTarget
    public void onUpdate(EventUpdate eventUpdate) {
        for (var iterator = this.particles.iterator(); iterator.hasNext(); ) {
            Particles update = iterator.next();
            ++update.ticks;
            if (update.ticks <= 10)
                update.location.setY(update.location.getY() + update.ticks * 0.005);
            if (update.ticks > 20)
                iterator.remove();
        }
    }

    public static class Particles {
        public int ticks;
        public Location location;
        public String text;

        public Particles(final Location location, final String text) {
            this.location = location;
            this.text = text;
            this.ticks = 0;
        }
    }

    @Getter
    @Setter
    public static class Location {
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public Location(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }


        public Location(EntityLivingBase entity) {
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
            this.yaw = 0.0f;
            this.pitch = 0.0f;
        }
    }
}
