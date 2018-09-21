package com.todo.messagelite;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by TCG on 2018/9/20.
 */

class PosterPolicy implements Poster {

    private final Map<ExecuteMode, Poster> POSTER_CONTAINER = new EnumMap<>(ExecuteMode.class);

    @Override
    public void enqueue(Receiver receiver, Object data) {
        ExecuteMode mode = receiver.getMode();
        Poster poster = POSTER_CONTAINER.get(mode);
        if (poster == null) {
            synchronized (POSTER_CONTAINER) {
                switch (mode) {
                    case ASYNC:
                        poster = new AsyncPoster();
                        break;
                    case BACKGROUND:
                        poster = new BackgroundPoster();
                        break;
                    case MAIN:
                    default:
                        poster = new MainPoster();
                        break;
                }
                POSTER_CONTAINER.put(mode, poster);
            }
        }
        poster.enqueue(receiver, data);
    }

    @Override
    public void quit() {
        Collection<Poster> posters = POSTER_CONTAINER.values();
        for (Poster p : posters) {
            if (p == null){
                continue;
            }
            p.quit();
        }
        POSTER_CONTAINER.clear();
    }
}
