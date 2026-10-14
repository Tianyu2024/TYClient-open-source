package cn.tianyu.client.gui.alt.altimpl;


import cn.tianyu.client.gui.alt.AccountEnum;
import cn.tianyu.client.gui.alt.Alt;

public final class MicrosoftAlt
extends Alt {
    private final String refreshToken;

    public MicrosoftAlt(String userName, String refreshToken) {
        super(userName, AccountEnum.MICROSOFT);
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }
}

