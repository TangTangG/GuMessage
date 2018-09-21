package com.todo.gumessage;

/**
 * Created by TCG on 2018/9/20.
 */

public class MessageFlag {

    public static final int TEST_MSG_BASE = 0x0001;

    public static final int MAIN_MSG_BASE = TEST_MSG_BASE + 1;
    public static final int ASYNC_MSG_BASE = TEST_MSG_BASE + 2;
    public static final int BACKGROUND_MSG_BASE = TEST_MSG_BASE + 3;

}
