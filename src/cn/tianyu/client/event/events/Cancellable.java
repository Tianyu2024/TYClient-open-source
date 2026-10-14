package cn.tianyu.client.event.events;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean state);

}
