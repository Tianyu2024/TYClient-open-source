package cn.tianyu.client.setting;

import cn.tianyu.client.value.Value;

/**
 * @Author: CN_tianyu
 */
@FunctionalInterface
public interface Dependency {
    boolean check();

    static Dependency visibleWhen(Value<Boolean> value) {
        return value::getValue;
    }

    static Dependency and(Dependency... dependencies) {
        return () -> {
            for (Dependency dep : dependencies) {
                if (!dep.check()) return false;
            }
            return true;
        };
    }

    static Dependency or(Dependency... dependencies) {
        return () -> {
            for (Dependency dep : dependencies) {
                if (dep.check()) return true;
            }
            return false;
        };
    }

    static Dependency not(Dependency dependency) {
        return () -> !dependency.check();
    }
}
