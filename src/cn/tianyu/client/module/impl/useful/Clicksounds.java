package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventKey;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.Debug;
import cn.tianyu.client.module.impl.combat.AutoClicker;
import cn.tianyu.client.util.impl.Multithreading;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Mouse;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: CN_tianyu
 */
public class Clicksounds extends Module {
    public Clicksounds() {
        super("Clicksounds", "点击声音", Category.useful);
    }

    public BooleanValue BlockReallySound = new BooleanValue("BlockSound", true);
    public BooleanValue ClicksSounds = new BooleanValue("ClickSound", true);
    public BooleanValue MoveSounds = new BooleanValue("MoveSound  ", true);

    private final ResourceLocation MOUSE_CLICK = new ResourceLocation("minecraft", "client/sounds/1.wav");
    private final ResourceLocation KEYBOARD = new ResourceLocation("client/sounds/2.mp3");
    private final ResourceLocation BLOCK_SOUND = new ResourceLocation("client/sounds/block.mp3");

    private boolean wasMoving = false;
    private boolean lastSneaking = false;
    private boolean lastJump = false;

    @EventTarget
    public void onMoveInput(EventMoveInput e){
        if (!isEnabled() || !MoveSounds.getValue()) return;
        if (mc.thePlayer == null) return;

        boolean moving = Math.abs(e.getForward()) > 0.01 || Math.abs(e.getStrafe()) > 0.01;
        boolean sneaking = mc.thePlayer.isSneaking();
        boolean jumping = mc.thePlayer.movementInput.jump;

        if (moving && !wasMoving) {
            playKeyboardSound();
            wasMoving = true;
        } else if (!moving && wasMoving) {
            playKeyboardSound();
            wasMoving = false;
        }

        if (sneaking != lastSneaking) {
            playKeyboardSound();
            lastSneaking = sneaking;
        }

        if (jumping && !lastJump) {
            playKeyboardSound();
        }
        lastJump = jumping;
    }

    @EventTarget
    public void onUpdate(EventUpdate e){
        if (!isEnabled() || !BlockReallySound.getValue()) return;
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.isBlocking() && mc.thePlayer.hurtTime > 0 && mc.thePlayer.hurtTime < 5) {
            playBlockSound();
        }
    }

    @EventTarget
    public void onTick(EventTick e){
        if (!isEnabled() || !ClicksSounds.getValue()) return;
        //if (e.getKey() == 0 || e.getKey() == 1) {
        if(TYClient.instance.moduleManager.getModule(AutoClicker.class).isEnable()
                && TYClient.getModuleManager().getModule(AutoClicker.class).canClick()
                && Mouse.isButtonDown(0)) {
            playMouseSound();
        }
        //}
    }

    public void playMouseSound() {

        //SoundEvent.NAME = new SoundEvent(new ResourceLocation("minecraft:client/sounds/1"));
        playSound(MOUSE_CLICK, 0.75f);
    }

    public static void playSound(ResourceLocation location, float volume) {
        Multithreading.runAsync(() -> {
            try {
                // MCP 918 中获取资源的方式
                InputStream inputStream = mc.getResourceManager().getResource(location).getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                // 检查文件格式
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * Math.min(volume, 1.0f)) + gainControl.getMinimum();
                gainControl.setValue(gain);

                clip.start();

                // 添加监听器，播放完毕后自动释放资源
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (UnsupportedAudioFileException e) {
                System.err.println("不支持的音频格式: " + location);
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("无法读取音频文件: " + location);
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                System.err.println("音频线路不可用");
                e.printStackTrace();
            }
        });
    }

    public void playKeyboardSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(KEYBOARD, 1.0f));
    }

    public void playBlockSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(BLOCK_SOUND));
    }

    public static final Minecraft mc = Minecraft.getMinecraft();
}