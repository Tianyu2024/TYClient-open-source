package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.Random;

/**
 * @Author: CN_tianyu
 */
public class BetterGrass extends Module {
    public static BetterGrass INSTANCE;
    private float windStrength = 0.15f;
    private float windSpeed = 0.8f;
    private float windDirection = 0.0f;
    private final Random random = new Random();
    private Thread renderThread;

    private final Block[] GRASS_FLOWER_BLOCKS = {
            Blocks.tallgrass,
            Blocks.grass,
            Blocks.double_plant,
            Blocks.red_flower,
            Blocks.yellow_flower,
            Blocks.brown_mushroom,
            Blocks.red_mushroom,
            Blocks.deadbush,
            Blocks.reeds,
            Blocks.vine,
            Blocks.waterlily
    };

    public BetterGrass() {
        super("BetterGrass", "更好的草", Category.better);
        INSTANCE = this;
        this.renderThread = Thread.currentThread();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.renderThread = Thread.currentThread();
        if (Minecraft.getMinecraft().thePlayer != null) {

        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isMainThread()) {
            GlStateManager.resetColor();
            GlStateManager.loadIdentity();
        }
        if (Minecraft.getMinecraft().thePlayer != null) {

        }
    }

    public void preRenderGrass(Block block, BlockPos pos) {
        if (!isEnabled() || !isGrassOrFlower(block) || !isMainThread()) return;

        updateWindParameters();

        long seed = (long)(pos.getX() * 3129871L + pos.getZ() * 116129781L) ^ (long)pos.getY();
        random.setSeed(seed);

        long worldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        float time = worldTime + random.nextFloat() * 1000.0f;
        float swayX = MathHelper.sin(time * windSpeed + seed) * windStrength;
        float swayZ = MathHelper.cos(time * windSpeed + seed + 1.5f) * windStrength;

        if (block == Blocks.tallgrass || block == Blocks.double_plant) {
            swayX *= 1.8f;
            swayZ *= 1.8f;
        } else if (block == Blocks.reeds || block == Blocks.vine) {
            swayX *= 0.8f;
            swayZ *= 0.8f;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        float rotateY = (float) (windDirection * 180.0 / Math.PI);
        GlStateManager.rotate(rotateY, 0, 1, 0);

        float bendAmount = MathHelper.sin(time * 0.5f) * 0.1f + 0.1f;
        GlStateManager.rotate(swayX * 15.0f * bendAmount, 0, 0, 1);
        GlStateManager.rotate(swayZ * 15.0f * bendAmount, 1, 0, 0);

        GlStateManager.translate(-(pos.getX() + 0.5), -pos.getY(), -(pos.getZ() + 0.5));

        float floatY = MathHelper.sin(time * 0.3f + seed) * 0.05f;
        GlStateManager.translate(0, floatY, 0);
    }

    public void postRenderGrass(Block block) {
        if (!isEnabled() || !isGrassOrFlower(block) || !isMainThread()) return;

        try {
            GlStateManager.popMatrix();
        } catch (Exception e) {}
    }

    private void updateWindParameters() {
        long worldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
        if (worldTime % 1000 == 0) {
            windDirection = random.nextFloat() * (float) Math.PI * 2.0f;
            windStrength = 0.1f + random.nextFloat() * 0.15f;
        }
        windSpeed = 0.7f + MathHelper.sin(worldTime * 0.001f) * 0.1f;
    }

    public boolean isGrassOrFlower(Block block) {
        for (Block target : GRASS_FLOWER_BLOCKS) {
            if (block == target) {
                return true;
            }
        }
        return block instanceof net.minecraft.block.BlockGrass;
    }

    private boolean isMainThread() {
        return Thread.currentThread() == this.renderThread || Thread.currentThread().getName().contains("Client Thread");
    }
}