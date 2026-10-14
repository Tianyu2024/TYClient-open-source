package cn.tianyu.client.util.annotations;

public class OutputAnimation {
    private float now;

    public OutputAnimation(int now) {
        this.now = now;
    }

    public void animate(float target, float speed) {
        now = AnimationUtil.animate(target, now, speed);
    }

    public double getOutput() {
        return now;
    }

    public void setNow(int now) {
        this.now = now;
    }
}
