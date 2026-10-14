package cn.tianyu.client.command.commands;

import cn.tianyu.client.command.Command;
import cn.tianyu.client.util.impl.client.DebugUtil;

import java.util.Arrays;

/**
 * @author CN_tianyu
 */

public class HelpCommand extends Command {
    public HelpCommand() {
        super(new String[]{"help"});
    }

    @Override
    public void run(String[] args) {
        DebugUtil.Clientlog(".bind 绑定快捷键\n .i 发送IRC消息 \n .open 打开XX模块");
    }
}
