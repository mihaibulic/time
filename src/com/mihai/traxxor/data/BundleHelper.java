package com.mihai.traxxor.data;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;

import com.mihai.traxxor.R;

public class BundleHelper {

    public static ArrayList<Stopwatch> readStopwatchesFromBundle(Bundle savedInstanceState, Stopwatch master) {
        Parcelable masterParcel = savedInstanceState.getParcelable(String.valueOf(R.integer.key_master_stopwatch));
        if (master instanceof Stopwatch) {
            master = (Stopwatch) masterParcel;
        }

        ArrayList<Parcelable> Parcelwatches = savedInstanceState.getParcelableArrayList(
                String.valueOf(R.integer.key_stopwatch_list));
        ArrayList<Stopwatch> watches = new ArrayList<Stopwatch>();
        if (Parcelwatches != null) {
            for (Parcelable watch : Parcelwatches) {
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
