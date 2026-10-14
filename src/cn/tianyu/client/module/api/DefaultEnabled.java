package cn.tianyu.client.module.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: CN_tianyu
 */
@Target(value={ElementType.TYPE})
@Retention(value= RetentionPolicy.RUNTIME)
public @interface DefaultEnabled {
}