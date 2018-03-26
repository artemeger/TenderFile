package sample.ipfs.events;

import java.lang.reflect.ParameterizedType;

public abstract class EventRunnable<T extends Event> implements Runnable{

    private T e;

    public abstract void execute(T event);

    public boolean ignoreCancelled() {
        return false;
    }

    @Override
    public void run() {
        execute(e);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Event> type() {
        return (Class<? extends Event>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    public void call(Event event){
        execute((T) event);
    }
}