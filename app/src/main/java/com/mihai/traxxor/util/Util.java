package com.mihai.traxxor.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mihai.traxxor.R;
import com.mihai.traxxor.activities.GraphActivity;
import com.mihai.traxxor.data.StatCalculator;
import com.mihai.traxxor.data.Stopwatch;

public class Util {
    private static final String PREFS = "traxxor_shared_prefs";
    private static final int MS_PER_SEC = 1000;
    private static final int MS_PER_MIN = MS_PER_SEC * 60;
    private static final int MS_PER_HOUR = MS_PER_MIN * 60;

    public static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String calculateTimeString(Stopwatch watch) {
        long durationMs = watch.getCurrentDuration();
        long hours = durationMs / MS_PER_HOUR;
        durationMs -= hours * MS_PER_HOUR;
        long minutes = durationMs / MS_PER_MIN;
        durationMs -= minutes * MS_PER_MIN;
        long seconds = durationMs / MS_PER_SEC;
        durationMs -= seconds * MS_PER_SEC;

        return String.format("%s:%s:%s", getTimeSubString(hours), getTimeSubString(minutes), getTimeSubString(seconds));
    }

    public static String getTimeSubString(long substring) {
        return (substring == 0 ? "00" : (substring < 10 ? "0" + substring : "" + substring));
    }

    public static String getGraphXExtra(String id) {
        return id + "_x";
    }

    public static String getGraphYExtra(String id) {
        return id + "_y";
    }

    public static String getGraphPrefix(int id) {
        return "graph_" + id;
    }

    /**
     * @return date in YYYY-MM-DD format
     */
    public static String getToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void graphStopwatches(Activity act, ArrayList<Stopwatch> watches, Stopwatch master, boolean isCumulative) {
        Intent intent = new Intent(act, GraphActivity.class);
        intent.putExtra(String.valueOf(R.integer.extra_graph_title), act.getString(R.string.all_stopwatches_graph));

        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        for (Stopwatch watch : watches) {
            double[][] data = StatCalculator.calculateAverages(master, watch, isCumulative);
            if (data != null) {
                String id = Util.getGraphPrefix(watch.getId());
                ids.add(id);
                names.add(watch.getName());
                intent.putExtra(Util.getGraphXExtra(id), data[0]);
                intent.putExtra(Util.getGraphYExtra(id), data[1]);
            }
        }
        intent.putExtra(String.valueOf(R.integer.extra_graph_series_ids), ids);
        intent.putExtra(String.valueOf(R.integer.extra_graph_series_names), names);

        act.startActivity(intent);
    }

    public static void graphStopwatch(Activity act, Stopwatch watch, Stopwatch master, boolean isCumulative) {
        Intent graphIntent = new Intent(act, GraphActivity.class);
        double[][] data = StatCalculator.calculateAverages(master, watch, isCumulative);
        graphIntent.putExtra(String.valueOf(R.integer.extra_graph_title), watch.getName());

        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();

        ids.add(Util.getGraphPrefix(watch.getId()));
        names.add(watch.getName());

        graphIntent.putExtra(String.valueOf(R.integer.extra_graph_series_ids), ids);
        graphIntent.putExtra(String.valueOf(R.integer.extra_graph_series_names), names);
        graphIntent.putExtra(Util.getGraphXExtra(ids.get(0)), data[0]);
        graphIntent.putExtra(Util.getGraphYExtra(ids.get(0)), data[1]);

        act.startActivity(graphIntent);
    }
}
