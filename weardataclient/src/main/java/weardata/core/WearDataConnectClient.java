package weardata.core;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Daniel on 2019/3/25.
 */
public class WearDataConnectClient implements WearDataContract.Presenter, DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    private WearDataContract.Callback callback;

    private Context context;

    private String dataReceivePath;

    private HashMap<String, String> keys = new HashMap<>();

    public WearDataConnectClient(Context context) {
        this.context = context;
        keys.clear();
    }

    public void setCallback(WearDataContract.Callback callback) {
        this.callback = callback;
    }

    public void setDataReceivePath(String dataReceivePath) {
        this.dataReceivePath = dataReceivePath;
    }

    public void putKey(String key) {
        keys.put(key, key);
    }

    @Override
    public void send(String destinationPath, String key, String data) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(destinationPath);
        putDataMapRequest.getDataMap().putString(key, data);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();
        Wearable.getDataClient(context.getApplicationContext()).putDataItem(request);
    }

    @Override
    public void register() {
        Wearable.getDataClient(context).addListener(this);
        Wearable.getMessageClient(context).addListener(this);
        Wearable.getCapabilityClient(context).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    public void unregister() {
        Wearable.getDataClient(context).removeListener(this);
        Wearable.getMessageClient(context).removeListener(this);
        Wearable.getCapabilityClient(context).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);
        for (DataEvent dataEvent : events) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                Uri uri = dataEvent.getDataItem().getUri();
                String path = uri.getPath();
                if (path != null && path.equals(dataReceivePath)) {
                    //inbound data chain,from watch to phone
                    DataMapItem dataMapItem = DataMapItem
                            .fromDataItem(dataEvent.getDataItem());

                    for (String key : keys.values()) {
                        String resultJson = dataMapItem.getDataMap().getString(key);
                        if (callback != null) {
                            callback.onStringDataReceived(key, resultJson);
                        }
                    }
                }
            }
        }
        dataEvents.release();
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {

    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        if (capabilityInfo != null) {
            boolean hasNodeNearby = false;
            for (Node node : capabilityInfo.getNodes()) {
                if (node.isNearby()) {
                    hasNodeNearby = true;
                    break;
                }
            }
            if (callback != null) {
                callback.onConnectionChanged(hasNodeNearby);
            }
        }
    }
}
