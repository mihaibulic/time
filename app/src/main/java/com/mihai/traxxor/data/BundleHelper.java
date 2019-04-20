package com.mihai.traxxor.data;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import com.mihai.traxxor.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleHelper {

    public static Stopwatch readMasterStopwatchFromBundle(Bundle savedInstanceState) {
        Parcelable masterParcel = savedInstanceState.getParcelable(String.valueOf(R.integer.parcel_key_master_stopwatch));
        if (masterParcel instanceof Stopwatch) {
            return (Stopwatch) masterParcel;
        }

        return null;
    }

    public static ArrayList<Stopwatch> readStopwatchesFromBundle(Bundle savedInstanceState) {
        ArrayList<Parcelable> parcelWatches = savedInstanceState.getParcelableArrayList(
                String.valueOf(R.integer.parcel_key_stopwatch_list));
        ArrayList<Stopwatch> watches = new ArrayList<>();
        if (parcelWatches != null) {
            for (Parcelable watch : parcelWatches) {
                if (watch instanceof Stopwatch) {
                    watches.add((Stopwatch) watch);
                }
            }
        }

        return watches;
    }

    public static EventHandler readEventHandlerFromBundle(@NonNull Bundle savedInstanceState, @NonNull Context context, @Nullable EventHandler.Listener listener) {
        Parcelable parcelEventHandler = savedInstanceState.getParcelable(String.valueOf(R.integer.parcel_key_event_handler));
        if (parcelEventHandler instanceof EventHandler) {
            ((EventHandler) parcelEventHandler).initOnParcelRestore(context, listener);
            return (EventHandler) parcelEventHandler;
        }

        return null;
    }

    public static void writeStopwatchesToBundle(@NonNull Bundle onInstanceState, @NonNull ArrayList<Stopwatch> watches, @NonNull Stopwatch master) {
        onInstanceState.putParcelable(String.valueOf(R.integer.parcel_key_master_stopwatch), master);
        onInstanceState.putParcelableArrayList(String.valueOf(R.integer.parcel_key_stopwatch_list), watches);
    }

    public static void writeEventHandlerToBundle(@NonNull Bundle onInstanceState, @NonNull EventHandler eventHandler) {
        onInstanceState.putParcelable(String.valueOf(R.integer.parcel_key_event_handler), eventHandler);
    }
}
