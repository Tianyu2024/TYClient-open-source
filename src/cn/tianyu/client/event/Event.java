package cn.tianyu.client.event;

import cn.tianyu.client.event.events.EventCancellable;

public interface Event {
    public static class StateEvent extends EventCancellable {
        private boolean pre = true;

        public boolean isPre() {
            return pre;
        }

        public boolean isPost() {
            return !pre;
        }

        public void setPost() {
            pre = false;
        }
    }
}