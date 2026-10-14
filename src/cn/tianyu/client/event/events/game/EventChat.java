package cn.tianyu.client.event.events.game;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class EventChat extends EventCancellable implements Event {
    private String message;
    private final List<ChatLine> chatLines;
    private IChatComponent component;

    public EventChat(String message, IChatComponent minecomponent, List<ChatLine> minechatline) {
        this.message = message;
        chatLines = minechatline;
        component = minecomponent;
    }

    public IChatComponent getComponent() {
        return component;
    }

    public void setComponent(IChatComponent p_setComponent_1_) {
        component = p_setComponent_1_;
    }

    public List<ChatLine> getChatLines() {
        return chatLines;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public IChatComponent getChatComponent() {
        return component;
    }

    public void setChatComponent(IChatComponent ChatComponent) {
        component = ChatComponent;
    }
}
