package at.gastronaut.android.classes.Devices;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.Hashtable;

import at.gastronaut.android.classes.SettingsChangedListener;

/**
 * Created by p.rathgeb on 15.05.2017.
 */

public abstract class Device extends BroadcastReceiver {
    protected Hashtable<String, SettingsChangedListener> notify;
    protected Context context;

    Device(Context c) {
        notify = new Hashtable<>();
        context = c;
    }

    public abstract void enable();

    public abstract void disable();

    public abstract boolean isEnabled();

    public void addSettingsChangedListener(String name, SettingsChangedListener scl) {
        if (!notify.containsKey(name)) {
            notify.put(name, scl);
        }
    }

    public Context getContext() {
        return context;
    }

    public void notifySettingsChanged() {
        for (SettingsChangedListener scl : notify.values()) {
            scl.changePerformed();
        }
    }
}
