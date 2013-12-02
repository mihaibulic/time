package com.mihai.traxxor.adapters;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mihai.traxxor.R;
import com.mihai.traxxor.activities.MainActivity;
import com.mihai.traxxor.data.StatCalculator;
import com.mihai.traxxor.data.Stopwatch;
import com.mihai.traxxor.util.Util;

public class StopwatchAdapter extends BaseAdapter implements OnTouchListener, OnClickListener, OnLongClickListener {
    private static final int REFRESH_TIME_MS = 500;

    private static MainActivity sActivity;
    private static int sTimeOnColor;
    private static int sTimeOffColor;

    private ArrayList<Stopwatch> mStopwatches = new ArrayList<Stopwatch>();
    private AtomicBoolean mRun = new AtomicBoolean(false);
    private View mClickedView = null;
    private Handler mHandler;
    private int mNextId = 0;

    public StopwatchAdapter(MainActivity act) {
        final Resources res = act.getResources();
        sTimeOnColor = res.getColor(R.color.stopwatch_on);
        sTimeOffColor = res.getColor(R.color.stopwatch_off);
        sActivity = act;
    }

    public synchronized int getCount() {
        return mStopwatches.size();
    }

    public synchronized Stopwatch getItem(int position) {
        return mStopwatches.get(position);
    }

    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        if (position < 0 || position >= mStopwatches.size()) {
            return null;
        }
        Stopwatch watch = mStopwatches.get(position);
        if (convertView == null || convertView.getId() != R.id.stopwatch) {
            convertView = LayoutInflater.from(sActivity).inflate(R.layout.stopwatch_for_main, null);
        }

        updateAll(convertView, watch);
        getGridNameTextView(convertView).setText(watch.getName());

        convertView.setTag(R.integer.tag_type, R.integer.type_toggle);
        convertView.setTag(R.integer.tag_stopwatch, watch);
        convertView.setOnTouchListener(this);
        convertView.setOnClickListener(this);
        convertView.setOnLongClickListener(this);

        return convertView;
    }

    // This is used to stop the refresh of UI elements,
    // which interrupt long clicks.
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                mClickedView = v;
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mClickedView = null;
            break;
        }
        return false;
    }

    public void onClick(final View v) {
        Stopwatch watch = (Stopwatch) v.getTag(R.integer.tag_stopwatch);
        if (watch != null) {
            switch ((Integer) v.getTag(R.integer.tag_type)) {
            case R.integer.type_toggle:
                toggleStopwatch(watch);
                break;
            case R.integer.type_delete:
                mStopwatches.remove(watch);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public boolean onLongClick(final View v) {
        final Stopwatch watch = (Stopwatch) v.getTag(R.integer.tag_stopwatch);
        if (watch == null) {
            return false;
        }

        final Resources res = sActivity.getResources();
        final String[] choices = res.getStringArray(R.array.long_press_stopwatch_actions);
        new AlertDialog.Builder(sActivity).setTitle(watch.getName()).setItems(choices,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Doing this rather than comparing indices ensures that the order
                        // of the dialog options can be changed just in the array.xml without
                        // worrying about having to change arbitrary ints (indices) elsewhere.
                        if (choices[which].equals(res.getString(R.string.stopwatch_action_toggle))) {
                            toggleStopwatch(watch);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_reset))) {
                            resetStopwatch(watch);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_graph_discrete))) {
                            Util.graphStopwatch(sActivity, watch, sActivity.getMasterStopwatch(), false);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_graph_cumulative))) {
                            Util.graphStopwatch(sActivity, watch, sActivity.getMasterStopwatch(), true);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_delete))) {
                            removeStopwatch(watch);
                        }
                    }
                }).create().show();

        return true;
    }

    public synchronized void startRefresh() {
        if (mRun.get()) {
            return;
        }
        mRun.set(true);

        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.post(new RefreshTask());
    }

    class RefreshTask implements Runnable {
        public void run() {
            if (mRun.get()) {
                if (mClickedView == null) {
                    notifyDataSetChanged();
                    sActivity.updateMaster();
                }

                mHandler.postDelayed(new RefreshTask(), REFRESH_TIME_MS);
            }
        }
    }

    public synchronized void stopRefresh() {
        if (!mRun.get()) {
            return;
        }
        mRun.set(false);
    }

    public void setNextId(int next) {
        mNextId = next;
    }

    public synchronized Stopwatch createStopwatch(String name) {
        return createStopwatch(name, false);
    }

    public synchronized Stopwatch createStopwatch(String name, boolean start) {
        Stopwatch watch = null;
        if (!TextUtils.isEmpty(name)) {
            watch = new Stopwatch(mNextId++, name);
            if (start) {
                watch.start();
            }

            addStopwatch(watch);
        }

        return watch;
    }


    public synchronized void addStopwatch(String name) {
        addStopwatch(new Stopwatch(mNextId++, name));
    }

    public synchronized void addStopwatch(Stopwatch watch) {
        if (watch.getId() > mNextId) {
            mNextId = watch.getId()+1;
        }
        mStopwatches.add(watch);
        notifyDataSetChanged();
    }

    public synchronized void addStopwatches(ArrayList<Stopwatch> watches) {
        for (Stopwatch watch : watches) {
            addStopwatch(watch);
        }
    }

    public synchronized void removeStopwatch(Stopwatch watch) {
        if (mStopwatches.remove(watch)) {
            notifyDataSetChanged();
        }
    }

    public synchronized void removeStopwatch(int id) {
        boolean found = false;
        for (int i = mStopwatches.size()-1; i >= 0; --i) {
            if (mStopwatches.get(i).getId() == id) {
                found = true;
                mStopwatches.remove(i);
            }
        }

        if (found) {
            notifyDataSetChanged();
        }
    }

    public synchronized void clearStopwatches() {
        mStopwatches.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Stopwatch> getStopwatches() {
        return mStopwatches;
    }

    public void startStopwatch(Stopwatch watch) {
        if (!watch.isStarted()) {
            for (Stopwatch w : mStopwatches) {
                w.stop();
            }
            watch.start();
            sActivity.startMaster();
            notifyDataSetChanged();
        }
    }

    public void toggleStopwatch(Stopwatch watch) {
        if (watch.stop()) {
            notifyDataSetChanged();
        } else {
            startStopwatch(watch);
        }
    }

    public void resetStopwatch(Stopwatch watch) {
        if (watch.reset()) {
            notifyDataSetChanged();
        }
    }

    public synchronized void resetAllStopwatches() {
        for (Stopwatch watch : mStopwatches) {
            watch.reset();
        }
        notifyDataSetChanged();
    }

    public synchronized void stopAllStopwatches() {
        for (Stopwatch watch : mStopwatches) {
            watch.stop();
        }
        notifyDataSetChanged();
    }

    private void updateTimeColor(View view, Stopwatch watch) {
        final int color = watch.isStarted() ? sTimeOnColor : sTimeOffColor;
        getTimeTextView(view).setTextColor(color);
    }

    private void updateTimeText(View view, Stopwatch watch) {
        getTimeTextView(view).setText(Util.calculateTimeString(watch));
    }

    private void updateAverageActivePercent(View view, Stopwatch watch) {
        getActivePercentTextView(view).setText(
                StatCalculator.calculateActivePercentString(sActivity.getMasterStopwatch(), watch));
    }

    private void updateAll(View view, Stopwatch watch) {
        updateTimeText(view, watch);
        updateTimeColor(view, watch);
        updateAverageActivePercent(view, watch);
    }

    private TextView getGridNameTextView(View view) {
        return (TextView) view.findViewById(R.id.name);
    }

    private TextView getTimeTextView(View view) {
        return (TextView) view.findViewById(R.id.time);
    }

    private TextView getActivePercentTextView(View view) {
        return (TextView) view.findViewById(R.id.active_percent);
    }
}
