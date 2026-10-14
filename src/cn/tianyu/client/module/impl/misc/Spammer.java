package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import org.apache.commons.lang3.RandomUtils;

/**
 * @Author: CN_tianyu
 */
public class Spammer extends Module {
    public Spammer() {
        super("Spammer","自动狗叫", Category.misc);
    }

    private final TimerUtil stopWatch = new TimerUtil();
    private final String[] chineseMessage = {
            "我按下了w键",
            "我按下了A键",
            "我按下了S键",
            "我按下了D键",
            "我按下了空格键",
            "我按下了鼠标",
            "我按点击了一下",
            "何意味",
    };

    @EventTarget
    public void onTick(EventTick e) {

        if (isOnServer("loyisa.cn")) {
            DebugUtil.Clientlog("在loyisa不刷");
            return;
        }

        if (stopWatch.delay(2200L)) {
            mc.thePlayer.sendChatMessage(chineseMessage[RandomUtils.nextInt(0, chineseMessage.length)]);
            stopWatch.reset();
        }
    }

    public static boolean isOnServer(final String server) {
        return !mc.isSingleplayer() && mc.getCurrentServerData().serverIP.contains(server);
    }
}
