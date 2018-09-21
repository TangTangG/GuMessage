package com.todo.messagelite.annotation;

import com.todo.messagelite.ExecuteMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by TCG on 2018/9/10.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsReceiver {

    int target();

    ExecuteMode mode() default ExecuteMode.MAIN;

}
