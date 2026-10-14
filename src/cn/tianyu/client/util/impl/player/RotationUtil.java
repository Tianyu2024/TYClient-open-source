package cn.tianyu.client.util.impl.player;

import cn.tianyu.client.event.events.player.EventRotation;
import cn.tianyu.client.util.impl.client.Location;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.client.RandomUtils;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.*;
import org.lwjglx.util.vector.Vector2f;

import java.util.List;

import static java.lang.Math.hypot;

/**
 * @Author: CN_tianyu
 */

public class RotationUtil {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static float[] currentRotation = null, serverRotation = new float[]{}, previousRotation = null;

    public static double distanceToEntity(Entity entity) {
        float borderSize = entity.getCollisionBorderSize();
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
        return RotationUtil.distanceToBox(boundingBox);
    }

    public static double distanceToBox(Entity entity, Vec3 point) {
        float borderSize = entity.getCollisionBorderSize();
        return RotationUtil.getDistanceToBox(entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize), point);
    }

    public static double distanceToBox(AxisAlignedBB boundingBox) {
        return RotationUtil.getDistanceToBox(boundingBox, RotationUtil.mc.thePlayer.getPositionEyes(1.0f));
    }

    public static double getDistanceToBox(AxisAlignedBB bb, Vec3 point) {
        if (bb.isVecInside(point)) {
            return 0.0;
        }
        Vec3 closestPoint = getClosestPointOnBox(point, bb);
        return point.distanceTo(closestPoint);
    }

    public static Vec3 getClosestPointOnBox(Vec3 point, AxisAlignedBB bb) {
        double x = MathHelper.clamp_double(point.xCoord, bb.minX, bb.maxX);
        double y = MathHelper.clamp_double(point.yCoord, bb.minY, bb.maxY);
        double z = MathHelper.clamp_double(point.zCoord, bb.minZ, bb.maxZ);
        return new Vec3(x, y, z);
    }

    public static float angleToEntity(Entity entity) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        float borderSize = entity.getCollisionBorderSize();
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
        if (boundingBox.isVecInside(eyePos)) {
            return 0.0f;
        }
        double deltaX = entity.posX - eyePos.xCoord;
        double deltaZ = entity.posZ - eyePos.zCoord;
        return Math.abs(MathHelper.wrapAngleTo180_float((float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f - RotationUtil.mc.thePlayer.rotationYaw)) * 2.0f;
    }

    public static float[] getRotationsToBox(AxisAlignedBB boundingBox, float yaw, float pitch, float maxAngle, float smoothFactor) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
        double height = boundingBox.maxY - boundingBox.minY;
        double[] yRatios = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        Vec3 bestPoint = null;
        double bestDistSq = Double.MAX_VALUE;

        for (double ratio : yRatios) {
            double targetY = boundingBox.minY + ratio * height;
            Vec3 targetPoint = new Vec3(centerX, targetY, centerZ);
            if (RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPoint) == null) {
                double dx = centerX - eyePos.xCoord;
                double dy = targetY - eyePos.yCoord;
                double dz = centerZ - eyePos.zCoord;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestPoint = targetPoint;
                }
            }
        }

        if (bestPoint != null) {
            double deltaX = bestPoint.xCoord - eyePos.xCoord;
            double deltaY = bestPoint.yCoord - eyePos.yCoord;
            double deltaZ = bestPoint.zCoord - eyePos.zCoord;
            return RotationUtil.getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
        }

        double minTargetY = boundingBox.minY + 0.05 * height;
        double maxTargetY = boundingBox.minY + 0.75 * height;
        double deltaX = centerX - eyePos.xCoord;
        double deltaY = eyePos.yCoord >= maxTargetY ? maxTargetY - eyePos.yCoord : (eyePos.yCoord <= minTargetY ? minTargetY - eyePos.yCoord : 0.0);
        double deltaZ = centerZ - eyePos.zCoord;
        return RotationUtil.getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
    }

    public static float[] getRotations(Vec3 vec) {
        return getRotations(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static float[] getRotations(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch, float maxAngle, float smoothFactor) {
        double horizontalDistance = Math.sqrt(targetX * targetX + targetZ * targetZ);
        float yawDelta = MathHelper.wrapAngleTo180_float((float) (Math.atan2(targetZ, targetX) * 180.0 / Math.PI) - 90.0f - currentYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(targetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = Math.abs(yawDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(yawDelta, maxAngle), smoothFactor);
        pitchDelta = Math.abs(pitchDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(pitchDelta, maxAngle), smoothFactor);
        return new float[]{RotationUtil.quantizeAngle(currentYaw + yawDelta), RotationUtil.quantizeAngle(currentPitch + pitchDelta)};
    }

    public static float clampAngle(float angle, float maxAngle) {
        maxAngle = Math.max(0.0f, Math.min(180.0f, maxAngle));
        if (angle > maxAngle) {
            angle = maxAngle;
        } else if (angle < -maxAngle) {
            angle = -maxAngle;
        }
        return angle;
    }

    public static float quantizeAngle(float angle) {
        return (float) ((double) angle - (double) angle % (double) 0.0096f);
    }

    public static float smoothAngle(float angle, float smoothFactor) {
        return angle * (0.5f + 0.5f * (1.0f - Math.max(0.0f, Math.min(1.0f, smoothFactor + RandomUtils.nextFloat(-0.1f, 0.1f)))));
    }

    public static float calculateYawFromSrcToDst(final float yaw,
                                                 final double srcX,
                                                 final double srcZ,
                                                 final double dstX,
                                                 final double dstZ) {
        final double xDist = dstX - srcX;
        final double zDist = dstZ - srcZ;
        final float var1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0F;
        return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw);
    }

    public static float[] getBlockRotations(double x, double y, double z) {
        double var4 = x - RotationUtil.mc.thePlayer.posX + 0.5;
        double var6 = z - RotationUtil.mc.thePlayer.posZ + 0.5;
        double var8 = y - (RotationUtil.mc.thePlayer.posY + (double) RotationUtil.mc.thePlayer.getEyeHeight() - 1.0);
        double var14 = MathHelper.sqrt_double(var4 * var4 + var6 * var6);
        float var12 = (float) (Math.atan2(var6, var4) * 180.0 / Math.PI) - 90.0f;
        return new float[]{var12, (float) (-Math.atan2(var8, var14) * 180.0 / Math.PI)};
    }

    public static float[] getRotations2(final BlockPos pos, final EnumFacing facing) {
        return getRotations2(pos.getX(), pos.getY(), pos.getZ(), facing);
    }

    public static float[] getRotations2(final double x, final double y, final double z, final EnumFacing facing) {
        final EntityPig temp = new EntityPig(mc.theWorld);
        temp.posX = x + 0.5;
        temp.posY = y + 0.5;
        temp.posZ = z + 0.5;

        temp.posX += facing.getDirectionVec().getX() * 0.5;
        temp.posY += facing.getDirectionVec().getY() * 0.5;
        temp.posZ += facing.getDirectionVec().getZ() * 0.5;

        return getRotations2(temp);
    }

    //Skidded from Minecraft
    public static float[] getRotations2(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final double diffX = entity.posX - mc.thePlayer.posX;
        final double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY;
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase elb = (EntityLivingBase) entity;
            diffY = elb.posY + (elb.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        final float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }
    public static Vec3 getClosestPoint(final Vec3 start,
                                       final AxisAlignedBB boundingBox) {
        final double closestX = start.xCoord >= boundingBox.maxX ? boundingBox.maxX :
                start.xCoord <= boundingBox.minX ? boundingBox.minX :
                        boundingBox.minX + (start.xCoord - boundingBox.minX);

        final double closestY = start.yCoord >= boundingBox.maxY ? boundingBox.maxY :
                start.yCoord <= boundingBox.minY ? boundingBox.minY :
                        boundingBox.minY + (start.yCoord - boundingBox.minY);

        final double closestZ = start.zCoord >= boundingBox.maxZ ? boundingBox.maxZ :
                start.zCoord <= boundingBox.minZ ? boundingBox.minZ :
                        boundingBox.minZ + (start.zCoord - boundingBox.minZ);

        return new Vec3(closestX, closestY, closestZ);
    }
    public static float[] getRotations(Entity target) {
        double diffX = target.posX - Minecraft.getMinecraft().thePlayer.posX;
        double diffZ = target.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double diffY = target.posY + target.getEyeHeight() * 0.9 -
                (Minecraft.getMinecraft().thePlayer.posY +
                        Minecraft.getMinecraft().thePlayer.getEyeHeight());

        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float)-(Math.atan2(diffY, dist) * 180.0 / Math.PI);

        yaw += Math.random() > 0.5 ? 1 : -1;
        pitch += Math.random() > 0.5 ? 0.5f : -0.5f;

        return new float[]{
                Minecraft.getMinecraft().thePlayer.rotationYaw +
                        MathHelper.wrapAngleTo180_float(yaw - Minecraft.getMinecraft().thePlayer.rotationYaw),
                Minecraft.getMinecraft().thePlayer.rotationPitch +
                        MathHelper.wrapAngleTo180_float(pitch - Minecraft.getMinecraft().thePlayer.rotationPitch)
        };
    }

    public static Vector2f smoothReal(Vector2f targetRotation) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        float randomYaw = (float) (Math.random() * 2.0 - 1.0) / 10.0f;
        float randomPitch = (float) (Math.random() * 2.0 - 1.0) / 10.0f;
        Vector2f rotations = new Vector2f(yaw += randomYaw, pitch += randomPitch);
        yaw = MathHelper.wrapDegrees(rotations.x);
        pitch = MathHelper.clamp_float(rotations.y, -90.0f, 90.0f);
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f calculate(final Entity entity) {
        return calculate(entity.getCustomPositionVector().add(0, Math.max(0, Math.min(mc.thePlayer.posY - entity.posY +
                mc.thePlayer.getEyeHeight(), (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.9)), 0));
    }



    public static Vector2f calculate(final cn.tianyu.client.util.impl.render.Vector3d from, final cn.tianyu.client.util.impl.render.Vector3d to) {

        final cn.tianyu.client.util.impl.render.Vector3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.getX(), diff.getZ());
        final float yaw = (float) (MathHelper.atan2(diff.getZ(), diff.getX()) * MathHelper.TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.getY(), distance) * MathHelper.TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f calculate(final Vec3 to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), new cn.tianyu.client.util.impl.render.Vector3d(to.xCoord, to.yCoord, to.zCoord));
    }

    public static Vector2f calculate(final cn.tianyu.client.util.impl.render.Vector3d to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), to);
    }

    public static Vector2f calculate(final cn.tianyu.client.util.impl.render.Vector3d position, final EnumFacing enumFacing) {
        double x = position.getX() + 0.5D;
        double y = position.getY() + 0.5D;
        double z = position.getZ() + 0.5D;

        x += (double) enumFacing.getDirectionVec().getX() * 0.5D;
        y += (double) enumFacing.getDirectionVec().getY() * 0.5D;
        z += (double) enumFacing.getDirectionVec().getZ() * 0.5D;
        return calculate(new cn.tianyu.client.util.impl.render.Vector3d(x, y, z));
    }

    public static float getYawToEntity(Entity entity) {
        if (entity == null) return 0f;
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        return (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
    }

    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public static Vec3 getVectorForRotation(Vector2f rotation) {
        float yawCos = MathHelper.cos(-rotation.getX() * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = MathHelper.sin(-rotation.getX() * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getY() * ((float)Math.PI / 180));
        float pitchSin = MathHelper.sin(-rotation.getY() * ((float)Math.PI / 180));
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }


//    public static float[] getRotationBlock(final BlockPos pos) {
//        return getRotationsByVec(mc.thePlayer.getPositionVector().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0), new Vec3(pos.getX() + 0.51, pos.getY() + 0.51, pos.getZ() + 0.51));
//    }
//    private static float[] getRotationsByVec(final Vec3 origin, final Vec3 position) {
//        final Vec3 difference = position.subtract(origin);
//        final double distance = difference.flat().lengthVector();
//        final float yaw = (float) Math.toDegrees(Math.atan2(difference.zCoord, difference.xCoord)) - 90.0f;
//        final float pitch = (float) (-Math.toDegrees(Math.atan2(difference.yCoord, distance)));
//        return new float[]{yaw, pitch};
//    }
    public static float[] getRotationForPlace(BlockPos pos) {
        Vec3 origin = mc.thePlayer.getPositionVector().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // 检测玩家在哪个方向，瞄准对应的侧面
        Vec3 playerVec = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        Vec3 blockVec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 diff = blockVec.subtract(playerVec);

        EnumFacing side = null;
        double max = -1;
        for (EnumFacing face : EnumFacing.values()) {
            double dot = diff.dotProduct(new Vec3(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ()));
            if (dot > max) {
                max = dot;
                side = face;
            }
        }

        if (side != null) {
            x += side.getFrontOffsetX() * 0.5;
            y += side.getFrontOffsetY() * 0.5;
            z += side.getFrontOffsetZ() * 0.5;
        }

        return getRotationsByVec(origin, new Vec3(x, y, z));
    }
    public static float[] getRotationsByVec(Vec3 origin, Vec3 position) {
        double dx = position.xCoord - origin.xCoord;
        double dy = position.yCoord - origin.yCoord;
        double dz = position.zCoord - origin.zCoord;
        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));
        return new float[]{yaw, pitch};
    }

    public static void setVisualRotations(float yaw, float pitch) {

        mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = yaw;


        mc.thePlayer.renderPitchHead = pitch;
    }

    public static float[] getRotateForScaffold(float needYaw, float needPitch, float yaw, float pitch, float minturnspeed, float maxturnspeed) {
        EventRotation smoothAngle = smoothAngle(new EventRotation(needYaw, needPitch), new EventRotation(yaw, pitch),
                MathUtil.getRandomInRange(minturnspeed, maxturnspeed));
        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(smoothAngle.getYaw() - mc.thePlayer.rotationYaw), smoothAngle.getPitch()};
    }

    private static EventRotation smoothAngle(EventRotation e, EventRotation e2, float turnSpeed) {
        EventRotation angle = new EventRotation(e2.getYaw() - e.getYaw(), e2.getPitch() - e.getPitch()).getAngle();
        angle.setYaw(e2.getYaw() - angle.getYaw() / 180.0f * turnSpeed);
        angle.setPitch(e2.getPitch() - angle.getPitch() / 180.0f * turnSpeed);
        return angle.getAngle();
    }

    public static float[] getAngles(Entity entity) {
        if (entity == null)
            return null;
        final EntityPlayerSP thePlayer = mc.thePlayer;

        final double diffX = entity.posX - thePlayer.posX,
                diffY = entity.posY + entity.getEyeHeight() * 0.9 - (thePlayer.posY + thePlayer.getEyeHeight()),
                diffZ = entity.posZ - thePlayer.posZ, dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ); // @on

        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F,
                pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);
        return new float[]{thePlayer.rotationYaw + MathHelper.wrapDegrees(yaw - thePlayer.rotationYaw), thePlayer.rotationPitch + MathHelper.wrapDegrees(pitch - thePlayer.rotationPitch)};
    }

    public static float[] getAnglesAGC(Entity e) {
        EntityPlayerSP playerSP = mc.thePlayer;
        double differenceX = e.posX - playerSP.posX;
        double differenceY = e.posY + e.height - (playerSP.posY + playerSP.height);
        double differenceZ = e.posZ - playerSP.posZ;
        float rotationYaw = (float) (Math.atan2(differenceZ, differenceX) * 180.0 / Math.PI) - 90.0f;
        float rotationPitch = (float) (Math.atan2(differenceY, playerSP.getDistanceToEntity(e)) * 180.0 / Math.PI);
        float finishedYaw = playerSP.rotationYaw + MathHelper.wrapAngleTo180_float(rotationYaw - playerSP.rotationYaw);
        float finishedPitch = playerSP.rotationPitch + MathHelper.wrapAngleTo180_float(rotationPitch - playerSP.rotationPitch);
        return new float[]{finishedYaw, -finishedPitch};
    }
    public static float[] getAngles2(Entity entity) {
        if (entity == null)
            return null;
        final EntityPlayerSP thePlayer = mc.thePlayer;

        double heightFactor = 0.4 + Math.random() * 0.5;
        if (Math.random() < 0.7) {
            heightFactor = 0.3 + Math.random() * 0.35;
        } else if (Math.random() < 0.6) {
            heightFactor = 0.7 + Math.random() * 0.25;
        } else {
            heightFactor = 0.0 + Math.random() * 0.2;
        }

        double time = System.currentTimeMillis() / 1000.0;
        double noiseX = Math.sin(time * 2.7 + 1.3) * 0.08 + (Math.random() - 0.5) * 0.06;
        double noiseZ = Math.sin(time * 3.1 + 0.7) * 0.08 + (Math.random() - 0.5) * 0.06;
        double noiseY = Math.sin(time * 2.3 + 2.1) * 0.04 + (Math.random() - 0.5) * 0.04;

        final double diffX = entity.posX + noiseX - thePlayer.posX;
        final double diffY = entity.posY + entity.getEyeHeight() * heightFactor + noiseY - (thePlayer.posY + thePlayer.getEyeHeight());
        final double diffZ = entity.posZ + noiseZ - thePlayer.posZ;
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        float yawDiff = MathHelper.wrapDegrees(yaw - thePlayer.rotationYaw);
        float pitchDiff = MathHelper.wrapDegrees(pitch - thePlayer.rotationPitch);

        if (Math.abs(yawDiff) > 5) {
            float overshoot = yawDiff * 0.06f * (float)(Math.random() * 0.5 + 0.5);
            yawDiff = yawDiff * 0.6f + overshoot;
        } else {
            yawDiff *= 0.6f;
        }

        if (Math.abs(pitchDiff) > 3) {
            float overshoot = pitchDiff * 0.06f * (float)(Math.random() * 0.5 + 0.5);
            pitchDiff = pitchDiff * 0.6f + overshoot;
        } else {
            pitchDiff *= 0.6f;
        }

        yaw = thePlayer.rotationYaw + yawDiff;
        pitch = MathHelper.clamp_float(thePlayer.rotationPitch + pitchDiff, -90, 90);

        return new float[]{yaw, pitch};
    }






    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * ((float)Math.PI / 180F));
        float f3 = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static MovingObjectPosition rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 lookVec = RotationUtil.mc.thePlayer.getVectorForRotation(pitch, yaw);
        Vec3 targetPos = eyePos.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
        return RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPos);
    }
    public static MovingObjectPosition rayTrace(double range, float partialTicks, float[] rotations, EntityLivingBase ignoreCollision) {
        if (ignoreCollision != null) {
            MovingObjectPosition target = rayTraceIgnore(range, partialTicks, rotations, ignoreCollision);
            if (target != null) {
                return target;
            }
        }
        Entity targetEntity = null;
        MovingObjectPosition hitObject;
        double d0 = range;
        if (rotations == null) {
            rotations = new float[] { mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch };
        }
        hitObject = rayTraceCustom(d0, rotations[0], rotations[1]);
        double distanceTo = d0;
        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);
        if (mc.playerController.extendedReach()) {
            d0 = 6.0;
            distanceTo = 6.0;
        }

        if (hitObject != null) {
            distanceTo = hitObject.hitVec.distanceTo(vec3);
        }

        Vec3 vec31 = getVectorForRotation(rotations[1], rotations[0]);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
        Vec3 vec33 = null;
        float f = 1.0F;
        List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
        double d2 = distanceTo;

        for(int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
            if (axisalignedbb.isVecInside(vec3)) {
                if (d2 >= 0.0) {
                    targetEntity = entity1;
                    vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                    d2 = 0.0;
                }
            }
            else if (movingobjectposition != null) {
                double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                if (d3 < d2 || d2 == 0.0) {
                    //&& !mc.thePlayer.canRiderInteract()
                    if (entity1 == mc.thePlayer.ridingEntity) {
                        if (d2 == 0.0) {
                            targetEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                        }
                    }
                    else {
                        targetEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (targetEntity != null && d2 < distanceTo) {
            return new MovingObjectPosition(targetEntity, vec33);
        }
        return null;
    }
    public static MovingObjectPosition rayTraceIgnore(double range, float partialTicks, float[] rotations, EntityLivingBase ignoreCollision) {
        MovingObjectPosition blockHit = rayTraceCustom(range,
                rotations[0],
                rotations[1]);

        Vec3 start = mc.thePlayer.getPositionEyes(partialTicks);
        double blockDistance = range;
        if (blockHit != null) {
            blockDistance = blockHit.hitVec.distanceTo(start);
        }

        if (ignoreCollision != null) {
            if (rotations == null) {
                rotations = new float[]{
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch
                };
            }
            Vec3 lookVec = getVectorForRotation(
                    rotations[1],  // pitch
                    rotations[0]   // yaw
            );
            Vec3 end = start.addVector(
                    lookVec.xCoord * range,
                    lookVec.yCoord * range,
                    lookVec.zCoord * range
            );

            float f1 = ignoreCollision.getCollisionBorderSize();
            AxisAlignedBB aabb = ignoreCollision.getEntityBoundingBox()
                    .expand(f1, f1, f1);
            MovingObjectPosition ignoreMOP = aabb.calculateIntercept(start, end);

            if (aabb.isVecInside(start)) {
                return new MovingObjectPosition(ignoreCollision, start);
            }
            if (ignoreMOP != null) {
                double ignoreDist = start.distanceTo(ignoreMOP.hitVec);
                if (ignoreDist < blockDistance) {
                    return new MovingObjectPosition(
                            ignoreCollision,
                            ignoreMOP.hitVec
                    );
                }
            }
        }
        if (blockHit != null) {
            return blockHit;
        }
        return null;
    }
    public static MovingObjectPosition rayTraceCustom(double blockReachDistance, float yaw, float pitch) {
        final Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0F);
        final Vec3 vec31 = getVectorForRotation(pitch, yaw);
        final Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public static float getRotationDifference(final Entity entity, final Entity entity2) {
        float[] target = getRotations(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        float[] target2 = getRotations(entity2.posX, entity2.posY + entity2.getEyeHeight(), entity2.posZ);
        return (float) hypot(Math.abs(getAngleDifference(target[0], target2[0])), Math.abs(target[1] - target2[1]));
    }

    public static float[] getRotations(double rotX, double rotY, double rotZ, double startX, double startY, double startZ) {
        double x = rotX - startX;
        double y = rotY - startY;
        double z = rotZ - startZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsT(double posX, double posY, double posZ) {
        double x = posX - mc.thePlayer.posX, z = posZ - mc.thePlayer.posZ, y = posY - (mc.thePlayer.getEyeHeight() + mc.thePlayer.posY);
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(y, d3) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(double posX, double posY, double posZ) {
        return getRotations(posX, posY, posZ, mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }
    public static float getAngleDifference(float a, float b) {
        return MathHelper.wrapAngleTo180_float(a - b);
    }

    public static float getBowRot(Entity entity) {
        double diffY;
        double diffX = entity.posX - RotationUtil.mc.thePlayer.posX;
        double diffZ = entity.posZ - RotationUtil.mc.thePlayer.posZ;
        Location BestPos = new Location(entity.posX, entity.posY, entity.posZ);
        Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + (double)RotationUtil.mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);
        for (diffY = entity.boundingBox.minY + 0.7; diffY < entity.boundingBox.maxY - 0.1; diffY += 0.1) {
            Location location = new Location(entity.posX, diffY, entity.posZ);
            if (!(myEyePos.distanceTo(location) < myEyePos.distanceTo(BestPos))) continue;
            BestPos = new Location(entity.posX, diffY, entity.posZ);
        }
        diffY = BestPos.getY() - (Minecraft.getMinecraft().thePlayer.posY + (double)Minecraft.getMinecraft().thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return yaw;
    }

    public static float getTrajAngleSolutionLow(float d3, float d1, float velocity) {
        float g2 = 0.006f;
        float sqrt = velocity * velocity * velocity * velocity - g2 * (g2 * (d3 * d3) + 2.0f * d1 * (velocity * velocity));
        return (float)Math.toDegrees(Math.atan(((double)(velocity * velocity) - Math.sqrt(sqrt)) / (double)(g2 * d3)));
    }

    public static float[] getRotations(BlockPos blockPos) {
        return getRotations(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    public static Vector2f getRotationsNonLivingEntity(Entity entity) {
        return RotationUtil.getRotations3(entity.posX, entity.posY + (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.5, entity.posZ);
    }
    public static Vector2f getRotations3(double posX, double posY, double posZ) {
        EntityPlayerSP player = RotationUtil.mc.thePlayer;
        double x2 = posX - player.posX;
        double y2 = posY - (player.posY + (double)player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x2 * x2 + z * z);
        float yaw = (float)(Math.atan2(z, x2) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(y2, dist) * 180.0 / Math.PI));
        return new Vector2f(yaw, pitch);
    }

    public static float wrapAngleDiff(float angle, float target) {
        return target + MathHelper.wrapAngleTo180_float(angle - target);
    }

    public static float[] getRotationsTo(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch) {
        return RotationUtil.getRotations(targetX, targetY, targetZ, currentYaw, currentPitch, 180.0f, 0.0f);
    }
}
