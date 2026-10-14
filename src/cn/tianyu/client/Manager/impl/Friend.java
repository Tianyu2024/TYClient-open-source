package cn.tianyu.client.Manager.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */
@Getter
@Setter
public final class Friend {
    private final String username;
    private String alias;

    public Friend(final String username) {
        this(username, username);
    }

    public Friend(final String alias, final String username) {
        this.alias = alias;
        this.username = username;
    }
}
