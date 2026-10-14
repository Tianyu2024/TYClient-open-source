package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;

/**
 * @Author: CN_tianyu
 */
public class EventText implements Event {

    public String string;

    public EventText(String string) {
        this.string = string;
    }

    public String getText() {
        return string;
    }

    public void setText(String pass) {
        this.string = pass;
    }
}
