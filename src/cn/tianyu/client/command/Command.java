package cn.tianyu.client.command;

/**
 * @author CN_tianyu
 */

public abstract class Command {
    private final String[] key;

    public Command(String[] key) {
        this.key = key;
    }

    public abstract void run(String[] args);

    public String[] getKey() {
        return key;
    }
}
