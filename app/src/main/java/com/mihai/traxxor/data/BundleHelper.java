package com.mihai.traxxor.data;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;

import com.mihai.traxxor.R;

public class BundleHelper {

    public static Stopwatch readMasterStopwatchFromBundle(Bundle savedInstanceState) {
        Parcelable masterParcel = savedInstanceState.getParcelable(String.valueOf(R.integer.key_master_stopwatch));
        if (masterParcel instanceof Stopwatch) {
            return (Stopwatch) masterParcel;
        }

        return null;
    }

    public static ArrayList<Stopwatch> readStopwatchesFromBundle(Bundle savedInstanceState) {
        ArrayList<Parcelable> parcelWatches = savedInstanceState.getParcelableArrayList(
                String.valueOf(R.integer.key_stopwatch_list));
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

    public static void writeStopwatchesToBundle(Bundle onInstanceState, ArrayList<Stopwatch> watches, Stopwatch master) {
        onInstanceState.putParcelable(String.valueOf(R.integer.key_master_stopwatch), master);
        onInstanceState.putParcelableArrayList(String.valueOf(R.integer.key_stopwatch_list), watches);
    }
}
