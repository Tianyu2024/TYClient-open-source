package cn.tianyu.client.event.events;


public abstract class EventCancellable implements Cancellable {

    private boolean cancelled;


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
