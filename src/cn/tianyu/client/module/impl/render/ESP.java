//package cn.tianyu.client.module.impl.render;
//
//import cn.tianyu.client.TYClient;
//import cn.tianyu.client.event.EventTarget;
//import cn.tianyu.client.event.events.render.EventRender2D;
//import cn.tianyu.client.module.impl.misc.AntiBot;
//import org.lwjglx.opengl.Display;
//import org.lwjgl.opengl.GL11;
//import cn.tianyu.client.event.events.render.EventRender3D;
//import cn.tianyu.client.font.FontManager;
//import cn.tianyu.client.font.RapeMasterFontManager;
//import cn.tianyu.client.module.Category;
//import cn.tianyu.client.module.Module;
//import cn.tianyu.client.module.impl.client.HUD;
//import cn.tianyu.client.module.impl.misc.Teams;
//import cn.tianyu.client.module.impl.player.Blink;
//import cn.tianyu.client.util.RenderUtil;
//import cn.tianyu.client.util.impl.client.MathUtil;
//import cn.tianyu.client.util.impl.render.ColorUtil;
//import cn.tianyu.client.util.impl.render.ShaderElement;
//import cn.tianyu.client.value.impl.BooleanValue;
//import cn.tianyu.client.value.impl.NumberValue;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GLAllocation;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.item.EntityArmorStand;
//import net.minecraft.entity.item.EntityItem;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.AxisAlignedBB;
//import net.minecraft.util.Vec3;
//import org.lwjgl.BufferUtils;
//import org.lwjglx.util.glu.GLU;
//import org.lwjglx.util.vector.Vector3f;
//import org.lwjglx.util.vector.Vector4f;
//
//import java.awt.*;
//import java.nio.FloatBuffer;
//import java.nio.IntBuffer;
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
//import java.util.*;
//import java.util.List;
//
//import static cn.tianyu.client.util.impl.client.MathUtil.DF_1;
//import static org.lwjgl.opengl.GL11.*;
//
///**
// * @Author: CN_tianyu
// */
//public class ESP extends Module {
//    public ESP() {
//        super("ESP","ESP", Category.render);
//    }
//
//    private final BooleanValue armorValue = new BooleanValue("Armor", true);
//    private final BooleanValue healthValue = new BooleanValue("Health", true);
//    private final BooleanValue boxValue = new BooleanValue("Box", true);
//    private final BooleanValue nameValue = new BooleanValue("Name", false);
//    private final NumberValue width2d = new NumberValue("BoxWidth", 0.5, 0.1, 1.0, 0.1);
//    private final DecimalFormat decimalFormat = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.ENGLISH));
//    private static final FloatBuffer modelView = GLAllocation.createDirectFloatBuffer(16);
//    private static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
//    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
//    private final List<Vec3> positions = new ArrayList<>();
//    private final Map<Entity, Vector4f> entityPosition = new HashMap<>();
//
//    private static final FloatBuffer windPos = BufferUtils.createFloatBuffer(4);
//    private static final IntBuffer intBuffer = GLAllocation.createDirectIntBuffer(16);
//    private static final FloatBuffer floatBuffer1 = GLAllocation.createDirectFloatBuffer(16);
//    private static final FloatBuffer floatBuffer2 = GLAllocation.createDirectFloatBuffer(16);
//
//
//    @EventTarget
//    public void onRender3D(EventRender3D event) {
//        for (final Entity entity : mc.theWorld.loadedEntityList) {
//            if (entity instanceof EntityPlayer || this.isValid(entity)) {
//                updateView();
//            }
//        }
//    }
//
//    private boolean shouldRender(Entity entity) {
//        if (entity.isDead || entity.isInvisible()) {
//            return false;
//        }
//        if (AntiBot.INSTANCE.isBot((EntityLivingBase) entity)) {
//            return false;
//        }
//        if (entity instanceof EntityPlayer) {
//            if (entity == mc.thePlayer) {
//                return mc.gameSettings.thirdPersonView != 0;
//            }
//            return !entity.getDisplayName().getUnformattedText().contains("[NPC");
//        }
//        return false;
//    }
//
//    @EventTarget
//    public void onRender3DEvent(EventRender3D e) {
//
//        entityPosition.clear();
//        for (final Entity entity : mc.theWorld.loadedEntityList) {
//            if(entity == mc.thePlayer){
//                return;
//            }
//            if (shouldRender(entity) && RenderUtil.isInView(entity)) {
//                entityPosition.put(entity, getEntityPositionsOn2D(entity));
//            }
//        }
//    }
//
//    @EventTarget
//    public void onRender2DEvent(EventRender2D e) {
//        final ScaledResolution sr = RenderUtil.getScaledResolution();
//        GlStateManager.pushMatrix();
//        final double twoScale = sr.getScaleFactor() / Math.pow(sr.getScaleFactor(), 2.0);
//        GlStateManager.scale(twoScale, twoScale, twoScale);
//        for (final EntityPlayer entity : getLoadedPlayers()) {
//            if (this.isValid(entity) && RenderUtil.isInViewFrustrum(entity)) {
//                this.updatePositions(entity);
//                int maxLeft = Integer.MAX_VALUE;
//                int maxRight = Integer.MIN_VALUE;
//                int maxBottom = Integer.MIN_VALUE;
//                int maxTop = Integer.MAX_VALUE;
//                final Iterator<Vec3> iterator2 = this.positions.iterator();
//                boolean canEntityBeSeen = false;
//                while (iterator2.hasNext()) {
//                    final Vec3 screenPosition = WorldToScreen(iterator2.next());
//                    if (screenPosition != null && screenPosition.zCoord >= 0.0 && screenPosition.zCoord < 1.0) {
//                        maxLeft = (int) Math.min(screenPosition.xCoord, maxLeft);
//                        maxRight = (int) Math.max(screenPosition.xCoord, maxRight);
//                        maxBottom = (int) Math.max(screenPosition.yCoord, maxBottom);
//                        maxTop = (int) Math.min(screenPosition.yCoord, maxTop);
//                        canEntityBeSeen = true;
//                    }
//                }
//                if (canEntityBeSeen) {
//                    if (this.healthValue.getValue()) {
//                        this.drawHealth(entity, (float) maxLeft, (float) maxTop, (float) maxBottom);
//                    }
//                    if (this.armorValue.getValue()) {
//                        this.drawArmor(entity, (float) maxTop, (float) maxRight, (float) maxBottom);
//                    }
//                    if (this.boxValue.getValue()) {
//                        this.drawBox(maxLeft, maxTop, maxRight, maxBottom);
//                    }
//                    /*if (this.nameValue.getValue()) {
//                        GlStateManager.scale(1, 1, 1);
//                        this.drawName(entity, maxLeft, maxTop, maxRight);
//                    }*/
//                }
//            }
//        }
//        GlStateManager.popMatrix();
//
//
//        for (Entity entity : entityPosition.keySet()) {
//            Vector4f pos = entityPosition.get(entity);
//            float x = pos.getX(),
//                    y = pos.getY(),
//                    right = pos.getZ();
//
//            if (entity instanceof EntityLivingBase) {
//                if (this.nameValue.getValue()) {
//                    this.drawName(entity, (int) x, (int) y, (int) right);
//                }
//            }
//
//        }
//    }
//
//    private void drawName(final Entity e, final int left, final int top, final int right) {
//        Vector4f pos = entityPosition.get(e);
//
//        RapeMasterFontManager font = FontManager.font18;
//        EntityLivingBase renderingEntity = (EntityLivingBase) e;
//        String rank = "";
//        // 仅计算一次是否同队以减少重复调用
//        boolean isSameTeam = TYClient.instance.moduleManager.getModule(Teams.class).getState() && Teams.isSameTeam(renderingEntity);
//
//        if (renderingEntity == mc.thePlayer) {
//            rank = "§a[You] ";
//        }  else if(isSameTeam){
//            rank = "§a[Team]";
//        }
//
////        if (IRC.client.isConnected()) {
////            for (IRCUser user : IRC.client.getOnlineUsers()) {
////                if (renderingEntity.getName().equals(user.getIngamename())) {
////                    rank = "§a[" + user.getUsername() + "] " + rank;
////                    break;
////                }
////            }
////        }
//        String formattedRank = !rank.isEmpty() ? rank + " " : "";
//        String name = renderingEntity.getDisplayName().getFormattedText();
//        String health = " " + DF_1.format(renderingEntity.getHealth()); // 前后添加空格以便与其他文本分隔
//        String formattedText = formattedRank + name + health;
//
//        float textWidth = FontManager.arial16.getStringWidth(formattedText);
//        float extraSpace = 16;
//        float width = textWidth + extraSpace;
//        float height = 14;
//        float x = pos.x;
//        float y = pos.y;
////            RoundedUtil.drawRound(x, y, width, height, 3f, new Color(0, 0, 0, 150));
//        RenderUtil.drawRectWH(x, y, width, height, ColorUtil.getColor(0, 0, 0, 76));
//
//        ShaderElement.addBlurTask(() -> RenderUtil.drawRectWH(x, y, width, height, -1));
//        ShaderElement.addBloomTask(() -> RenderUtil.drawRectWH(x, y, width, height, ColorUtil.getColor(0, 0, 0, 200)));
//
//        RenderUtil.drawRectWH(x, y + 3, 1, 8, HUD.color(1).getRGB());
//
//        FontManager.arial16.drawString(formattedText, x + 6f, y + 4, -1);
//    }
//
//    public static List<EntityPlayer> getLoadedPlayers() {
//        return mc.theWorld.playerEntities;
//    }
//
//    private void drawBox(int left, int top, int right, int bottom) {
//        RenderUtil.drawRectBordered(left + 0.5, top + 0.5, right - 0.5, bottom - 0.5, 1.0, ColorUtil.getColor(0, 0, 0, 0), ColorUtil.WHITE);
//        RenderUtil.drawRectBordered(left - 0.5, top - 0.5, right + 0.5, bottom + 0.5, 1.0, ColorUtil.getColor(0, 0), ColorUtil.getColor(0));
//        RenderUtil.drawRectBordered(left + 1.5, top + 1.5, right - 1.5, bottom - 1.5, 1.0, ColorUtil.getColor(0, 0), ColorUtil.getColor(0));
//    }
//
//    private void drawArmor(final EntityLivingBase entityLivingBase, final float top, final float right, final float bottom) {
//        final float height = bottom + 1.0f - top;
//        final float currentArmor = (float) entityLivingBase.getTotalArmorValue();
//        final float armorPercent = currentArmor / 20.0f;
//        final float MOVE = 2.0f;
//        final int line = 1;
//        RenderUtil.drawESPRect(right + 2.0f + 1.0f + MOVE, top - 2.0f, right + 1.0f - 1.0f + MOVE, bottom + 1.0f, new Color(25, 25, 25, 150).getRGB());
//        RenderUtil.drawESPRect(right + 3.0f + MOVE, top + height * (1.0f - armorPercent) - 1.0f, right + 1.0f + MOVE, bottom, new Color(78, 206, 229).getRGB());
//        RenderUtil.drawESPRect(right + 3.0f + MOVE + line, bottom + 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(right + 1.0f + MOVE, bottom + 1.0f, right + 1.0f + MOVE - line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(right + 1.0f + MOVE, top - 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(right + 1.0f + MOVE, bottom + 1.0f, right + 3.0f + MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
//    }
//
//    private void drawHealth(final EntityLivingBase entityLivingBase, final float left, final float top, final float bottom) {
//        final float height = bottom + 1.0f - top;
//        final float currentHealth = entityLivingBase.getHealth();
//        final float maxHealth = entityLivingBase.getMaxHealth();
//        final float healthPercent = currentHealth / maxHealth;
//        final float MOVE = 2.0f;
//        final int line = 1;
//        final String healthStr = "§f" + this.decimalFormat.format(currentHealth) + "§c❤";
//        final float bottom2 = top + height * (1.0f - healthPercent) - 1.0f;
//        final float health = entityLivingBase.getHealth();
//        final float[] fractions = {0.0f, 0.5f, 1.0f};
//        final Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
//        final float progress = health / entityLivingBase.getMaxHealth();
//        final Color customColor = (health >= 0.0f) ? ColorUtil.blendColors(fractions, colors, progress).brighter() : Color.RED;
//        mc.fontRendererObj.drawStringWithShadow(healthStr, left - 3.0f - MOVE - mc.fontRendererObj.getStringWidth(healthStr), bottom2, -1);
//        RenderUtil.drawESPRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, top - 1.0f, new Color(25, 25, 25, 150).getRGB());
//        RenderUtil.drawESPRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, bottom2, customColor.getRGB());
//        RenderUtil.drawESPRect(left - 3.0f - MOVE, bottom + 1.0f, left - 3.0f - MOVE - line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(left - 1.0f - MOVE + line, bottom + 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(left - 3.0f - MOVE, top - 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
//        RenderUtil.drawESPRect(left - 3.0f - MOVE, bottom + 1.0f, left - 1.0f - MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
//    }
//
//    private int getColor(EntityLivingBase ent) {
////        if (AntiBot.isServerBot(ent)) {
////            return new Color(255, 0, 0).getRGB();
////        }
//        if (Teams.isSameTeam(ent) || ent instanceof EntityPlayerSP) {
//            return new Color(0, 255, 0).getRGB();
//        }
//        return new Color(255, 0, 0).getRGB();
//    }
//
//    private static Vec3 WorldToScreen(final Vec3 position) {
//        final FloatBuffer screenPositions = BufferUtils.createFloatBuffer(3);
//        final boolean result = GLU.gluProject((float) position.xCoord, (float) position.yCoord, (float) position.zCoord, ESP.modelView, ESP.projection, ESP.viewport, screenPositions);
//        if (result) {
//            return new Vec3(screenPositions.get(0), Display.getHeight() - screenPositions.get(1), screenPositions.get(2));
//        }
//        return null;
//    }
//
//    public void updatePositions(final Entity entity) {
//        this.positions.clear();
//        final Vec3 position = getEntityRenderPosition(entity);
//        final double x = position.xCoord - entity.posX;
//        final double y = position.yCoord - entity.posY;
//        final double z = position.zCoord - entity.posZ;
//        final double height = (entity instanceof EntityItem) ? 0.5 : (entity.height + 0.1);
//        final double width = (entity instanceof EntityItem) ? 0.25 : this.width2d.getValue();
//        final AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - width + x, entity.posY + y, entity.posZ - width + z, entity.posX + width + x, entity.posY + height + y, entity.posZ + width + z);
//        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ));
//        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.maxZ));
//        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ));
//        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.maxZ));
//        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ));
//        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.maxZ));
//        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ));
//        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ));
//    }
//
//    private static Vec3 getEntityRenderPosition(final Entity entity) {
//        return new Vec3(getEntityRenderX(entity), getEntityRenderY(entity), getEntityRenderZ(entity));
//    }
//
//    private static double getEntityRenderX(final Entity entity) {
//        return entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * Minecraft.getMinecraft().timer.renderPartialTicks - Minecraft.getMinecraft().getRenderManager().renderPosX;
//    }
//
//    private static double getEntityRenderY(final Entity entity) {
//        return entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * Minecraft.getMinecraft().timer.renderPartialTicks - Minecraft.getMinecraft().getRenderManager().renderPosY;
//    }
//
//    private static double getEntityRenderZ(final Entity entity) {
//        return entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * Minecraft.getMinecraft().timer.renderPartialTicks - Minecraft.getMinecraft().getRenderManager().renderPosZ;
//    }
//
//    private boolean isValid(final Entity entity) {
//        final Blink blink = (Blink) TYClient.instance.moduleManager.getModule(Blink.class);
//        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) {
//            return false;
//        }
//        if (entity.isInvisible()) {
//            return false;
//        }
//        if (entity instanceof EntityArmorStand) {
//            return false;
//        }
//        if (blink.getState()) {
//            return true;
//        }
//        return entity instanceof EntityPlayer;
//    }
//
//    private static void updateView() {
//        GL11.glGetFloat(2982, modelView);
//        GL11.glGetFloat(2983, projection);
//        GL11.glGetInteger(2978, viewport);
//    }
//
//    public static Vector4f getEntityPositionsOn2D(Entity entity) {
//        final AxisAlignedBB bb = getInterpolatedBoundingBox(entity);
//
//        float yOffset = 0;
//
//        final List<Vector3f> vectors = Arrays.asList(
//                new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
//                new Vector3f((float) bb.minX, (float) bb.maxY - yOffset, (float) bb.minZ),
//                new Vector3f((float) bb.maxX, (float) bb.minY, (float) bb.minZ),
//                new Vector3f((float) bb.maxX, (float) bb.maxY - yOffset, (float) bb.minZ),
//                new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.maxZ),
//                new Vector3f((float) bb.minX, (float) bb.maxY - yOffset, (float) bb.maxZ),
//                new Vector3f((float) bb.maxX, (float) bb.minY, (float) bb.maxZ),
//                new Vector3f((float) bb.maxX, (float) bb.maxY - yOffset, (float) bb.maxZ));
//
//        Vector4f entityPos = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f);
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
//        for (Vector3f vector3f : vectors) {
//            vector3f = projectOn2D(vector3f.x, vector3f.y, vector3f.z, sr.getScaleFactor());
//            if (vector3f != null && vector3f.z >= 0.0 && vector3f.z < 1.0) {
//                entityPos.x = Math.min(vector3f.x, entityPos.x);
//                entityPos.y = Math.min(vector3f.y, entityPos.y);
//                entityPos.z = Math.max(vector3f.x, entityPos.z);
//                entityPos.w = Math.max(vector3f.y, entityPos.w);
//            }
//        }
//        return entityPos;
//    }
//
//    public static AxisAlignedBB getInterpolatedBoundingBox(Entity entity) {
//        final double[] renderingEntityPos = getInterpolatedPos(entity);
//        final double entityRenderWidth = entity.width / 1.5;
//        return new AxisAlignedBB(renderingEntityPos[0] - entityRenderWidth,
//                renderingEntityPos[1], renderingEntityPos[2] - entityRenderWidth, renderingEntityPos[0] + entityRenderWidth,
//                renderingEntityPos[1] + entity.height + (entity.isSneaking() ? -0.3 : 0.18), renderingEntityPos[2] + entityRenderWidth).expand(0.15, 0.15, 0.15);
//    }
//
//    public static double[] getInterpolatedPos(Entity entity) {
//        float ticks = mc.timer.renderPartialTicks;
//        return new double[]{
//                MathUtil.interpolate(entity.lastTickPosX, entity.posX, ticks) - mc.getRenderManager().viewerPosX,
//                MathUtil.interpolate(entity.lastTickPosY, entity.posY, ticks) - mc.getRenderManager().viewerPosY,
//                MathUtil.interpolate(entity.lastTickPosZ, entity.posZ, ticks) - mc.getRenderManager().viewerPosZ
//        };
//    }
//
//    public static Vector3f projectOn2D(float x, float y, float z, int scaleFactor) {
//        glGetFloat(GL_MODELVIEW_MATRIX, floatBuffer1);
//        glGetFloat(GL_PROJECTION_MATRIX, floatBuffer2);
//        glGetInteger(GL_VIEWPORT, intBuffer);
//        if (GLU.gluProject(x, y, z, floatBuffer1, floatBuffer2, intBuffer, windPos)) {
//            return new Vector3f(windPos.get(0) / scaleFactor, (mc.displayHeight - windPos.get(1)) / scaleFactor, windPos.get(2));
//        }
//        return null;
//    }
//}
