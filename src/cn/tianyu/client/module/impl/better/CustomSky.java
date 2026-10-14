package cn.tianyu.client.module.impl.better;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import static cn.tianyu.client.TYClient.mc;

public class CustomSky {
    public static CustomSky Instance = new CustomSky();
    private static final float SIZE = 256.0F;
    private Properties properties = new Properties();
    private ResourceLocation texture;
    private boolean rotate;
    private float axisX, axisY, axisZ;
    private float fadeInStart, fadeInEnd, fadeOutStart, fadeOutEnd;
    private String blendMode;
    private boolean valid;

    private void loadConfig(int dimension) {
        try {
            String propPath = "mcpatcher/sky/world" + dimension + "/sky1.properties";
            InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", propPath)).getInputStream();
            properties.load(new BufferedReader(new InputStreamReader(stream)));

            String source = properties.getProperty("source").replace("./", "");
            texture = new ResourceLocation("minecraft", "mcpatcher/sky/world" + dimension + "/" + source);

            fadeInStart = parseTime(properties.getProperty("startFadeIn"));
            fadeInEnd = parseTime(properties.getProperty("endFadeIn"));
            fadeOutStart = parseTime(properties.getProperty("startFadeOut"));
            fadeOutEnd = parseTime(properties.getProperty("endFadeOut"));
            blendMode = properties.getProperty("blend");
            rotate = Boolean.parseBoolean(properties.getProperty("rotate"));
            String[] axis = properties.getProperty("axis").split(" ");
            axisX = Float.parseFloat(axis[0]);
            axisY = Float.parseFloat(axis[1]);
            axisZ = Float.parseFloat(axis[2]);

            valid = true;
            stream.close();
        } catch (Exception e) {
            valid = false;
        }
    }

    private float parseTime(String time) {
        String[] parts = time.split(":");
        float hour = Float.parseFloat(parts[0]);
        float minute = Float.parseFloat(parts[1]);
        return (hour * 1000) + (minute * 1000 / 60);
    }

    private float calculateAlpha(World world, float ticks) {
        float time = (world.getWorldTime() + ticks) % 24000;
        if (time < fadeInStart) return 0.0F;
        if (time < fadeInEnd) return (time - fadeInStart) / (fadeInEnd - fadeInStart);
        if (time < fadeOutStart) return 1.0F;
        if (time < fadeOutEnd) return 1.0F - ((time - fadeOutStart) / (fadeOutEnd - fadeOutStart));
        return 0.0F;
    }

    public void renderSky(WorldRenderer wr, float partialTicks, boolean reverse) {
        if(mc.thePlayer == null){
            System.out.println("jump");
            return;
        }
        System.out.println("y");
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        int dim = world.provider.getDimensionId();
        loadConfig(dim);

        if (!valid) return;

        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        mc.getTextureManager().bindTexture(texture);

        float alpha = calculateAlpha(world, partialTicks);
        if (alpha <= 0.0F) return;

        GlStateManager.enableBlend();
        if (blendMode.equals("multiply")) {
            GlStateManager.blendFunc(GL11.GL_DST_COLOR, GL11.GL_ZERO);
        } else {
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        GlStateManager.pushMatrix();
        if (rotate) {
            float angle = (world.getWorldTime() + partialTicks) * 0.05F;
            GlStateManager.rotate(angle, axisX, axisY, axisZ);
        }

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        wr.pos(-SIZE, SIZE, -SIZE).tex(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        wr.pos(SIZE, SIZE, -SIZE).tex(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        wr.pos(SIZE, SIZE, SIZE).tex(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        wr.pos(-SIZE, SIZE, SIZE).tex(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
    }
}