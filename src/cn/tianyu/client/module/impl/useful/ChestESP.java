package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.stream.Collectors;

/**
 * @Author: CN_tianyu
 */
public class ChestESP extends Module {
    public ChestESP() {
        super("ChestESP", "箱子透视", Category.useful);
    }

    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ColorValue color = new ColorValue("color", new Color(255, 255, 255));
    public final int opacity = 100;

    @EventTarget
    public void onRender(EventRender3D event) {
        if (this.isEnabled()) {
            RenderUtil.enableRenderState();
            for (TileEntity chest : mc.theWorld.loadedTileEntityList.stream().filter(tileEntity -> tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest).collect(Collectors.toList())) {
                AxisAlignedBB aabb = new AxisAlignedBB(
                        (double) chest.getPos().getX() + 0.0625,
                        (double) chest.getPos().getY() + 0.0,
                        (double) chest.getPos().getZ() + 0.0625,
                        (double) chest.getPos().getX() + 0.9375,
                        (double) chest.getPos().getY() + 0.875,
                        (double) chest.getPos().getZ() + 0.9375
                )
                        .offset(
                                -( mc.getRenderManager()).getRenderPosX(),
                                -( mc.getRenderManager()).getRenderPosY(),
                                -( mc.getRenderManager()).getRenderPosZ()
                        );
                Color color = new Color(this.color.getColorInt());
                RenderUtil.drawBoundingBox(
                        aabb, color.getRed(), color.getGreen(), color.getBlue(), (int) ((float) this.opacity / 100.0F * 255.0F), 1.5F
                );
//                if (this.tracers.getValue()) {
//                    Vec3 vec;
//                    if (mc.gameSettings.thirdPersonView == 0) {
//                        vec = new Vec3(0.0, 0.0, 1.0)
//                                .rotatePitch(
//                                        (float) (
//                                                -Math.toRadians(
//                                                        RenderUtil.lerpFloat(
//                                                                mc.getRenderViewEntity().rotationPitch,
//                                                                mc.getRenderViewEntity().prevRotationPitch,
//                                                                ((IAccessorMinecraft) mc).getTimer().renderPartialTicks
//                                                        )
//                                                )
//                                        )
//                                )
//                                .rotateYaw(
//                                        (float) (
//                                                -Math.toRadians(
//                                                        RenderUtil.lerpFloat(
//                                                                mc.getRenderViewEntity().rotationYaw,
//                                                                mc.getRenderViewEntity().prevRotationYaw,
//                                                                ((IAccessorMinecraft) mc).getTimer().renderPartialTicks
//                                                        )
//                                                )
//                                        )
//                                );
//                    } else {
//                        vec = new Vec3(0.0, 0.0, 0.0)
//                                .rotatePitch(
//                                        (float) (
//                                                -Math.toRadians(
//                                                        RenderUtil.lerpFloat(
//                                                                mc.thePlayer.cameraPitch, mc.thePlayer.prevCameraPitch, ((IAccessorMinecraft) mc).getTimer().renderPartialTicks
//                                                        )
//                                                )
//                                        )
//                                )
//                                .rotateYaw(
//                                        (float) (
//                                                -Math.toRadians(
//                                                        RenderUtil.lerpFloat(
//                                                                mc.thePlayer.cameraYaw, mc.thePlayer.prevCameraYaw, ((IAccessorMinecraft) mc).getTimer().renderPartialTicks
//                                                        )
//                                                )
//                                        )
//                                );
//                    }
//                    vec = new Vec3(vec.xCoord, vec.yCoord + (double) mc.getRenderViewEntity().getEyeHeight(), vec.zCoord);
//                    float opacity = (float) ((Tracers) Unfair.moduleManager.modules.get(Tracers.class)).opacity.getValue() / 100.0F;
//                    RenderUtil.drawLine3D(
//                            vec,
//                            (double) chest.getPos().getX() + 0.5,
//                            (double) chest.getPos().getY() + 0.5,
//                            (double) chest.getPos().getZ() + 0.5,
//                            (float) color.getRed() / 255.0F,
//                            (float) color.getGreen() / 255.0F,
//                            (float) color.getBlue() / 255.0F,
//                            opacity,
//                            1.5F
//                    );
//                }
            }
            RenderUtil.disableRenderState();
        }
    }
}
