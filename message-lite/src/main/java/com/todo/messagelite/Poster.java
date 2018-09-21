package com.todo.messagelite;

interface Poster {

    void enqueue(Receiver receiver,Object data);

    void quit();

}
