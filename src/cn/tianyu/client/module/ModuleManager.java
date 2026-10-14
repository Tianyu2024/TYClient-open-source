package cn.tianyu.client.module;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventKey;
import cn.tianyu.client.module.impl.HUD.TargetHUD;
import cn.tianyu.client.module.impl.better.*;
import cn.tianyu.client.module.impl.client.*;
import cn.tianyu.client.module.impl.combat.*;
import cn.tianyu.client.module.impl.combat.AutoClicker;
import cn.tianyu.client.module.impl.misc.*;
import cn.tianyu.client.module.impl.movement.*;
import cn.tianyu.client.module.impl.player.*;
import cn.tianyu.client.module.impl.render.*;
import cn.tianyu.client.module.impl.useful.*;
import cn.tianyu.client.module.impl.useful.XRay;
import cn.tianyu.client.module.impl.world.*;
import cn.tianyu.client.module.impl.wtf.LTap;
import cn.tianyu.client.module.impl.client.musicManager;
import cn.tianyu.client.module.impl.wtf.wtffff;
import cn.tianyu.client.smartDev.ServerOp;
import cn.tianyu.client.value.Value;
import cn.tianyu.client.viamcp.ViaMCP;
import com.yumegod.obfuscation.*;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CN_tianyu
 */

@CallEncryption
public class ModuleManager {
    @Getter
    private final List<Module> mods = new ArrayList<>();

    private final Map<String, Module> moduleMap = new HashMap<>();

    public List<Module> getEnableMods() {
        return mods.stream().filter(Module::isEnable).collect(Collectors.toList());
    }

    public void onKey(int key) {
        for (Module enableMod : mods) {
            if (enableMod.getKey() == key) {
                enableMod.setEnable(!enableMod.isEnable());
            }
        }
    }

    private void addModule(Module module) {
        for (Field field : module.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object obj = field.get(module);
                if (!(obj instanceof Value)) continue;
                module.getValues().add((Value)obj);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.mods.add(module);
        moduleMap.put(module.getClass().getSimpleName(), module);
    }

    public void load() {
        //combat
        addModule(new VlFix());
        addModule(new AimAssist());
        addModule(new AutoBlock());
        //addModule(new AntiKb());
        //addModule(new AntiAura());
        addModule(new AutoClicker());
        addModule(new AutoGapple());
        addModule(new AutoPot());
        addModule(new AutoProjectile());
        addModule(new AutoRod());
        addModule(new AutoSoup());
        addModule(new BackTrack());
        addModule(new BlockHit());
        addModule(new BowAimbot());
        addModule(new Criticals());
        //addModule(new FakeLag());
        addModule(new HitClicker());
        addModule(new JumpReset());
        addModule(new KeepSprint());
        addModule(new KillAura());
        //addModule(new HypixelKillAura());
        addModule(new IdleFighter());
        addModule(new KnowBackDelay());
        addModule(new Lagrange());
        addModule(new MoreKB());
        addModule(new TargetStrafe());
        addModule(new TickBase());
        addModule(new Velocity());
        //movement
        addModule(new Fly());
        addModule(new GuiMove());
        addModule(new Eagle());
        addModule(new NoJumpDelay());
        addModule(new NoSlow());
        addModule(new NoWeb());
        addModule(new SafeWalk());
        addModule(new LongJump());
        //addModule(new TellyBridge());
        addModule(new LegitScaffold());
        addModule(new Scaffold());
        addModule(new HypScaffold());
        addModule(new Speed());
        addModule(new Sprint());
        //render
        addModule(new Animations());
        addModule(new Atmosphere());
        addModule(new BrightPlayers());
        addModule(new Chams());
        addModule(new EffectGui());
        addModule(new EmoJiMask());
        addModule(new FireFlies());
        addModule(new FullBright());
        addModule(new HitBox());
        addModule(new HitBubbles());
        addModule(new Indicators());
        addModule(new JumpCircle());
        addModule(new KeyBoard());
        addModule(new LineGlyphs());
        //addModule(new NameLabel());
        addModule(new NoHurtCamera());
        addModule(new PlayerSize());
        addModule(new PostProcessing());
        addModule(new Projectile());
        addModule(new SelectBlock());
        addModule(new StrengthRing());
        addModule(new TabListAnimations());
        addModule(new TNTTag());
        //misc
        addModule(new AntiBot());
        addModule(new AntiExploit());
        addModule(new AntiFreeze());
        addModule(new AutoRespawn());
        addModule(new ClientSpoofer());
        addModule(new Crasher());
        addModule(new Disabler());
        addModule(new HackerDetector());
        addModule(new MemoryFix());
        addModule(new NoGuiClose());
        addModule(new NoRotate());
        addModule(new RawMouseInput());
        addModule(new Regen());
        addModule(new Spammer());
        addModule(new Teams());
        addModule(new VoidFlickFix());

        addModule(new ClickGUI());
        //useful
        addModule(new BedESP());
        addModule(new ChestESP());
        addModule(new Clicksounds());
        addModule(new DamageParticles());
        addModule(new NoClickDelay());
        addModule(new FreeLook());
        addModule(new Particles());
        addModule(new FastPlace());
        addModule(new ItemPhysics());
        addModule(new ItemTags());
        addModule(new HurtColor());
        addModule(new MotionBlur());
        //player
        //addModule(new AntiFall());
        addModule(new ScaffoldAim());
        addModule(new SpeedMine());
        //addModule(new AntiVoid());
        addModule(new Girl());
        addModule(new AutoTool());
        addModule(new Blink());
        addModule(new ChestStealer());
        addModule(new InvCleaner());
        addModule(new InventorySync());
        addModule(new MoveFix());
        addModule(new NoFall());
        addModule(new Phase());

        //world
        this.addModule(new XRay());
        //client
        addModule(new ServerOp());
        this.addModule(new Debug());
        this.addModule(new CapeMod());
        this.addModule(new HUD());
        this.addModule(new IRC());
        addModule(new MouseDelayFix());
        this.addModule(new Notifications());
        //better
        addModule(new WaveyCapes());
        addModule(new FPSBoost());
        addModule(new AsyncParticle());
        addModule(new MemoryCleaner());
        addModule(new ASyncScreenShot());
        addModule(new AnimatedView());
        addModule(new BetterChat());
        addModule(new BetterGrass());
        this.addModule(new BetterTab());
        //this.addModule(new CustomSky2());
        addModule(new Camera());
        addModule(new ChunkAnimator());
        //wtf
        this.addModule(new LTap());
        this.addModule(new musicManager());
        this.addModule(new wtffff());

        //world
        addModule(new AutoPearl());
        addModule(new BedNuker());
        addModule(new ChestAura());
        addModule(new ItemDetector());
        addModule(new Stuck());

        addModule(new TargetHUD());

        sortModulesByName();

        try {
            TYClient.LOGGER.info("Starting ViaMCP...");
            ViaMCP viaMCP = ViaMCP.getInstance();
            viaMCP.start();
            viaMCP.initAsyncSlider(5, 5, 110, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sortModulesByName() {
        List<Map.Entry<String, Module>> entryList = new ArrayList<>(moduleMap.entrySet());
        entryList.sort(Comparator.comparing(entry -> entry.getValue().getName()));

        moduleMap.clear();
        for (Map.Entry<String, Module> entry : entryList) {
            moduleMap.put(entry.getKey(), entry.getValue());
        }
    }

    public Module getByName(String name) {
        for (Module mod : mods) {
            if (name.equalsIgnoreCase(mod.getName())) {
                return mod;
            }
        }
        return null;
    }

    public <T extends Module> T getModule(Class<T> cls) {
        return cls.cast(moduleMap.get(cls.getSimpleName()));
    }



    public Module getModule(String name) {
        for (Module module : moduleMap.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public Map<String, Module> getModuleMap() {
        return moduleMap;
    }

    public <T extends Module> T getModule1(Class<T> cls) {
        return cls.cast(mods);
    }

    public Module getByClass(Class<? extends Module> modClass) {
        for (Module mod : mods) {
            if (mod.getClass() == modClass) {
                return mod;
            }
        }
        return null;
    }

    public List<Module> getByCategory(Category category) {
        return mods.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    @EventTarget
    public void onKey(EventKey e) {
        moduleMap.values().stream()
                .filter(module -> module.getKey() == e.getKey() && e.getKey() != -1)
                .forEach(Module::toggle);
    }

    public List<Module> getModsByCategory(Category m) {
        return moduleMap.values().stream()
                .filter(module -> module.getCategory() == m)
                .collect(Collectors.toList());
    }

    public List<Module> getModsByPage(Category.Pages m) {
        return moduleMap.values().stream()
                .filter(module -> module.getCategory().pages == m)
                .collect(Collectors.toList());
    }

    public List<Module> getModules() {
        return this.mods;
    }
}
