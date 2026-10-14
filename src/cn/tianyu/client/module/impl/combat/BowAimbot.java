package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.AntiBot;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import org.lwjglx.util.vector.Vector2f;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: CN_tianyu
 */
public class BowAimbot extends Module {
    public static EntityLivingBase target;
    public static NumberValue fov;
    public static NumberValue range;
    public static ModeValue priority;

    public BowAimbot() {
        super("BowAimbot", "弓箭自瞄", Category.combat);
    }

    @EventTarget
    public void onUpdatePre(EventMotion event) {
        if (event.isPost()) {
            return;
        }
        if (mc.thePlayer.inventory.getCurrentItem() == null || mc.thePlayer.inventory.getCurrentItem().getItem() != Items.bow || !mc.thePlayer.isUsingItem()) {
            target = null;
            return;
        }
        target = this.getTarget();
        if (target == null) {
            return;
        }
        float[] rotation = this.getPlayerRotations(target);
        TYClient.rotationManager.setRotation(new Vector2f(rotation[0], rotation[1]), 10.0f, true);
    }

    @Override
    public void onDisable() {
        target = null;
        super.onDisable();
    }

    private float[] getPlayerRotations(Entity entity) {
        double posX = entity.posX - mc.thePlayer.posX;
        double posZ = entity.posZ - mc.thePlayer.posZ;
        double posY = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());

        double distance = MathHelper.sqrt_double(posX * posX + posZ * posZ);

        float yaw = (float)(Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-Math.atan2(posY, distance) * 180.0 / Math.PI);

        float arrowSpeed = 1.0f;
        float arrowGravity = 0.05f;
        float velocity = arrowSpeed;
        float time = (float) (distance / velocity);
        float gravityOffset = (float)(0.5 * arrowGravity * time * time);
        float adjustedPitch = (float)(-Math.atan2(posY + gravityOffset, distance) * 180.0 / Math.PI);

        float finalPitch = Math.max(-90, Math.min(90, adjustedPitch));

        return new float[]{yaw, finalPitch};
    }

    public static boolean isFovInRange(Entity entity, float fovValue) {
        fovValue = (float)((double)fovValue * 0.5);
        float yawDiff = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - RotationUtil.getYawToEntity(entity));
        return Math.abs(yawDiff) <= fovValue;
    }

    private EntityLivingBase getTarget() {
        double maxRange = range.getValue().floatValue();
        float fovValue = fov.getValue().floatValue();

        Stream<EntityPlayer> stream = mc.theWorld.playerEntities.stream()
                .filter(e -> e != mc.thePlayer)
                .filter(e -> !e.isDead)
                .filter(e -> !Teams.isSameTeam(e))
                .filter(e -> !AntiBot.INSTANCE.isBot(e))
                .filter(e -> mc.thePlayer.getDistanceToEntity(e) <= maxRange)
                .filter(e -> mc.thePlayer.canEntityBeSeen(e))
                .filter(e -> isFovInRange(e, fovValue));

        if (priority.getValue().equals("Range")) {
            stream = stream.sorted(Comparator.comparingDouble(e -> e.getDistanceToEntity(mc.thePlayer)));
        } else if (priority.getValue().equals("Angle")) {
            stream = stream.sorted(Comparator.comparingDouble(e -> Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - RotationUtil.getYawToEntity(e)))));
        }

        List<EntityLivingBase> list = stream.map(e -> (EntityLivingBase)e).collect(Collectors.toList());

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    static {
        fov = new NumberValue("FoV", 180.0, 10.0, 360.0, 10.0);
        range = new NumberValue("Range", 100.0, 1.0, 200.0, 10.0);
        priority = new ModeValue("Priority", "Angle", "Angle", "Range");
    }
}