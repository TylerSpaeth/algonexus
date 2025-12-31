package com.github.tylerspaeth.strategy.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Strategy {
    String name() default "";
    int version() default 0;
}
