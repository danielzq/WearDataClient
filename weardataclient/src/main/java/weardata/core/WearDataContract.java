package weardata.core;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

/**
 * Created by Daniel on 2019/3/25.
 */
public interface WearDataContract {

    interface Presenter extends LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        void register();

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void unregister();

        void send(String destinationPath, String key, String data);
    }

    interface Callback {

        void onConnectionChanged(boolean hasNodeNearby);

        void onStringDataReceived(String key, String data);
    }
}
