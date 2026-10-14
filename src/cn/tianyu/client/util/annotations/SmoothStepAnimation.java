package cn.tianyu.client.util.annotations;

import cn.tianyu.client.util.impl.player.TimerUtil;

/**
 * @Author: CN_tianyu
 */
public class SmoothStepAnimation extends Animation {
    public TimerUtil timerUtils = new TimerUtil();

    public SmoothStepAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        return -2 * Math.pow(x, 3) + (3 * Math.pow(x, 2));
    }
}