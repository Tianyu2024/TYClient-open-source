package cn.tianyu.client.script;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author: CN_tianyu
 */
public class ScriptManager {
    public static final ScriptManager INSTANCE = new ScriptManager();
    private final List<Script> scriptList = new ArrayList<>();

    public void register(Script script) {
        scriptList.add(script);
    }

    public void unregister(Script script) {
        scriptList.remove(script);
    }

    public List<Script> getScripts() {
        return new ArrayList<>(scriptList);
    }

    public Script getScriptByName(String name) {
        for (Script s : scriptList) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    public void toggleScript(String name) {
        Script target = getScriptByName(name);
        if (target == null) return;
        if (target.running) {
            target.stop();
        } else {
            target.start();
        }
    }

    public void stopAll() {
        for (Script s : scriptList) {
            if (s.running) s.stop();
        }
    }

    @EventTarget
    public void onTick(EventTick event) {
        for (Script script : scriptList) {
            if (script.running) {
                script.onTick();
            }
        }
    }

    public void clientShutdown() {
        for (Script s : scriptList) {
            s.onDisable();
        }
        scriptList.clear();
    }
}