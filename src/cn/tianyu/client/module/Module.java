package cn.tianyu.client.module;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.module.impl.combat.AutoGapple;
import cn.tianyu.client.setting.Setting;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.annotations.Direction;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import cn.tianyu.client.value.Value;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CN_tianyu
 */

public class Module {
    protected static final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    public float cGUIAnimation = 0f;
    @Getter
    private String name;
    @Getter
    private final Category category;
    @Getter
    public final String version;
    @Getter
    private boolean enable;
    @Getter
    private boolean disable;
    @Getter
    public String suffix;
    @Setter
    @Getter
    private int key = -1;
    public double progress;
    @Getter
    @Setter
    private boolean expanded;

    @Getter
    private final List<Setting<?>> setting = new ArrayList<>();

    private final Animation animation = new DecelerateAnimation(250, 1.0).setDirection(Direction.BACKWARDS);


    public Module(String name, String version, Category category) {
        this.name = name;
        this.version = version;
        this.category = category;
        this.suffix = "";
        this.enable = this.isDefaultEnabled();
        if (this.isDefaultEnabled()) {
            TYClient.instance.eventManager.register(this);
        }
    }

    @Getter
    private final List<Value<?>> values = new ArrayList<>();

    public static <T extends Module> T getModule(Class<T> clazz) {
        return (T) TYClient.instance.moduleManager.getByClass(clazz);
    }

    public void toggle() {
        this.setEnable(!this.enable);
    }

    private boolean isDefaultEnabled() {
        return this.getClass().isAnnotationPresent(DefaultEnabled.class);
    }
    public boolean isRenderModule() {
        return this.category == Category.render;
    }
    public final List<Module> getArraylistModules(List<Module> modules) {
        return modules.stream().filter(module -> module.isEnabled() && !(true && module.getCategory().equals(Category.render))).collect(Collectors.toList());
    }

    public boolean getState() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
        if (enable) {
            if(mc.thePlayer != null){
                Minecraft.getMinecraft().getSoundHandler().playSound(
                        PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)
                );
                NotificationManager.post(NotificationType.SUCCESS, "Module", "Enabled " + this.name);
            }
            TYClient.instance.eventManager.register(this);
            onEnable();
        } else {
            if(mc.thePlayer != null){
                Minecraft.getMinecraft().getSoundHandler().playSound(
                        PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)
                );
                NotificationManager.post(NotificationType.DISABLE, "Module", "Disabled " + this.name);}
            TYClient.instance.eventManager.unregister(this);
            onDisable();
        }
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void draw() {

    }

    public void onUpdate() {

    }

    public void key(int key) {

    }

    public void onRender(float partialTicks) {

    }

    public void setSuffix(Object obj) {
        String suffix = obj.toString();
        if (suffix.isEmpty()) {
            this.suffix = suffix;
        } else {
            this.suffix = String.format("§f%s§7", EnumChatFormatting.GRAY + suffix);
        }
    }

    public void sendNoEvent(final Packet<?> packet) {
        if (mc.thePlayer != null) {
            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        }
    }

    public void sendPacket(final Packet<?> packet) {
        if (mc.thePlayer != null) {
            PacketUtils.sendPacket(packet);
        }
    }

    public final boolean isEnabled() {
        return enable;
    }

    public <M extends Module> boolean isEnabled(Class<M> module) {
        Module mod = TYClient.instance.getModuleManager().getModule(module);
        return mod != null && mod.isEnabled();
    }

    public boolean isGapple() {
        return TYClient.instance.getModuleManager().getModule(AutoGapple.class).isEnabled();
    }

    public boolean isFood() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood;
    }

    public boolean isBow() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow;
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.equals("hypixel.net") || mc.getCurrentServerData().serverIP.contains(".hypixel.net"));
    }

    public boolean checknull() {
        return mc.thePlayer == null && mc.theWorld == null;
    }

    public Animation getAnimation() {
        return this.animation;
    }
}
