package cn.tianyu.client.module;

import cn.tianyu.client.util.impl.render.RippleAnimation;

public enum Category {
    combat(Pages.COMBAT),
    movement(Pages.MOVEMENT),
    useful(Pages.USEFUL),
    misc(Pages.MISC),
    render(Pages.RENDER),
    player(Pages.PLAYER),
    better(Pages.BETTER),
    world(Pages.WORLD),
    client(Pages.Client),
    HUD(Pages.HUD),
    CONFIGS(Pages.CONFIGS);
    public final Pages pages;

    Category(Pages pages) {
        this.pages = pages;
    }

    public enum Pages {
        COMBAT(true, false),
        MOVEMENT(true, false),
        USEFUL(true,false),
        RENDER(true, false),
        BETTER(true, false),
        PLAYER(true, false),
        MISC(true, false),
        WORLD(true, false),
        Client(true, true),
        HUD(true,false),
        CONFIGS(false,false);
        public final boolean module;
        public final boolean gradient;

        public final RippleAnimation animation;

        Pages(boolean module, boolean gradient) {
            this.module = module;
            this.gradient = gradient;
            this.animation = new RippleAnimation();
        }
    }
    public Pages[] getSubPages() {
        return Pages.values();
    }
}