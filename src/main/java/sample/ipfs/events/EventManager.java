package sample.ipfs.events;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EventManager {

    private ArrayList<EventRunnable<? extends Event>> firsts;
    private ArrayList<EventRunnable<? extends Event>> normals;
    private ArrayList<EventRunnable<? extends Event>> lasts;

    public EventManager() {
        firsts = new ArrayList<>();
        normals = new ArrayList<>();
        lasts = new ArrayList<>();
    }

    public void register(EventRunnable<? extends Event> runnable) {
        register(Priority.NORMAL, runnable);
    }

    public void register(Priority priority, EventRunnable<? extends Event> runnable) {
        switch (priority) {
            case FIRST: {
                firsts.add(runnable);
                break;
            }
            case NORMAL: {
                normals.add(runnable);
                break;
            }
            case LAST: {
                lasts.add(runnable);
                break;
            }
        }
    }

    public void call(final Event event) {

        Consumer<? super EventRunnable<? extends Event>> call = new Consumer<EventRunnable<? extends Event>>() {
            @Override
            public void accept(EventRunnable<? extends Event> r) {

                if (!r.type().equals(event.getClass())) return;

                cancel:
                {
                    if (!(event instanceof CancellableEvent)) break cancel;
                    CancellableEvent c = (CancellableEvent) event;

                    if (!c.isCancelled()) break cancel;
                    if (r.ignoreCancelled()) break cancel;

                    return;
                }

                r.call(event);

            }
        };

        for (EventRunnable<? extends Event> plugin : firsts) call.accept(plugin);
        for (EventRunnable<? extends Event> plugin : normals) call.accept(plugin);
        for (EventRunnable<? extends Event> plugin : lasts) call.accept(plugin);
    }

    public ArrayList<EventRunnable<? extends Event>> getAllRunnables() {
        ArrayList<EventRunnable<? extends Event>> runnables = new ArrayList<>();
        runnables.addAll(firsts);
        runnables.addAll(normals);
        runnables.addAll(lasts);
        return runnables;
    }

    public enum Priority {
        FIRST,
        NORMAL,
        LAST;
    }
}