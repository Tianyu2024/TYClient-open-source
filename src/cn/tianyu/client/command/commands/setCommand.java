package cn.tianyu.client.command.commands;

import cn.tianyu.client.command.Command;
import cn.tianyu.client.util.impl.client.DebugUtil;

/**
 * @Author: CN_tianyu
 */
public class setCommand extends Command {
    public setCommand() {
        super(new String[]{"sb"});
    }

    @Override
    public void run(String[] args) {
        DebugUtil.log("恭喜你发现了宝藏! 2025/6/8 - 13:00 制作的菜单");
    }
}
