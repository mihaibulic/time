package com.mihai.traxxor.ui;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.mihai.traxxor.R;
import com.mihai.traxxor.data.StatCalculator;
import com.mihai.traxxor.data.Stopwatch;

public class StopwatchAdapter extends BaseAdapter implements OnTouchListener, OnClickListener, OnLongClickListener {
    private static final int REFRESH_TIME_MS = 500;
    private static final int MS_PER_SEC = 1000;
    private static final int MS_PER_MIN = MS_PER_SEC * 60;
    private static final int MS_PER_HOUR = MS_PER_MIN * 60;

    private static Activity mActivity;
    private static int mTimeOnColor;
    private static int mTimeOffColor;
    private static int mMode = R.integer.mode_grid;

    private Vector<Holder> mStopwatches = new Vector<Holder>();
    private Stopwatch mMasterWatch;
    private View mClickedView = null;
    private Handler mHandler;
    private int mNextId = 0;

    private class Holder {
        int id;
        Stopwatch watch;
        View gridView;
        View listView;

        public Holder(Stopwatch watch) {
            this.id = watch.getId();
            this.watch = watch;
        }

        @Override
        public boolean equals(Object a) {
            return (a != null) && (a instanceof Holder) && ((Holder) a).id == id;
        }
    }

    public StopwatchAdapter(Activity act) {
        mActivity = act;
        final Resources res = act.getResources();
        mTimeOnColor = res.getColor(R.color.stopwatch_on);
        mTimeOffColor = res.getColor(R.color.stopwatch_off);
    }

    public void setMasterStopwatch(Stopwatch master) {
        mMasterWatch = master;
    }

    public int getCount() {
        return mStopwatches.size();
    }

    public Stopwatch getItem(int position) {
        return mStopwatches.get(position).watch;
    }

    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public synchronized Stopwatch createStopwatch(boolean start, String name) {
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

    public synchronized void addStopwatch(Stopwatch watch) {
        Holder holder = new Holder(watch);
        mStopwatches.add(holder);
        holder.gridView = getGridBasedView(mStopwatches.size() - 1, null, null);
        holder.listView = getListBasedView(mStopwatches.size() - 1, null, null);

        if (mHandler == null) {
            mHandler = new Handler();
        }

        // restart refresher task, since before this we had no stopwatches
        if (mStopwatches.size() == 1) {
            mHandler.post(new RefreshTask());
        }

        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (position >= 0 && position < mStopwatches.size()) {
            switch (mMode) {
            case R.integer.mode_list:
                view = getListBasedView(position, convertView, parent);
                break;
            case R.integer.mode_grid:
                view = getGridBasedView(position, convertView, parent);
                break;
            }
        }

        return view;
    }

    public View getListBasedView(int position, View convertView, ViewGroup parent) {
        Holder holder = mStopwatches.get(position);
        if (holder.listView != null) {
            convertView = holder.listView;
        } else {
            if (convertView == null || convertView.getId() != R.id.stopwatch_for_dialog) {
                android.util.Log.v("bulic", "inflating");
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.stopwatch_for_manager, null);
            }
            holder.listView = convertView;

            getListNameTextView(holder).setText(holder.watch.getName());

            ImageView deleteView = getDeleteImageView(holder);
            deleteView.setTag(R.integer.tag_type, R.integer.type_delete);
            deleteView.setTag(R.integer.tag_holder, holder);
            deleteView.setOnClickListener(this);
        }
        return convertView;
    }

    public View getGridBasedView(int position, View convertView, ViewGroup parent) {
        Holder holder = mStopwatches.get(position);
        if (holder.gridView != null) {
            convertView = holder.gridView;
        } else {
            if (convertView == null || convertView.getId() != R.id.stopwatch) {
                android.util.Log.v("bulic", "G inflating");
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.stopwatch, null);
            }
            holder.gridView = convertView;

            updateAll(holder);
            getGridNameTextView(holder).setText(holder.watch.getName());

            // TextView percent = getActivePercentTextView(holder);
            // percent.setTag(R.integer.tag_type, R.integer.type_graph);
            // percent.setTag(R.integer.tag_holder, holder);
            // percent.setOnClickListener(this);

            convertView.setTag(R.integer.tag_type, R.integer.type_toggle);
            convertView.setTag(R.integer.tag_holder, holder);
            convertView.setOnTouchListener(this);
            convertView.setOnClickListener(this);
            convertView.setOnLongClickListener(this);
        }

        return convertView;
    }

    public Stopwatch getMasterStopwatch() {
        return mMasterWatch;
    }

    public Stopwatch[] getStopwatches() {
        Stopwatch[] watches = new Stopwatch[mStopwatches.size()];
        for (int x = 0; x < watches.length; x++) {
            watches[x] = mStopwatches.get(x).watch;
        }

        return watches;
    }

    public View[] getStopwatchGridViews() {
        View[] views = new View[mStopwatches.size()];
        for (int x = 0; x < views.length; x++) {
            views[x] = mStopwatches.get(x).gridView;
        }

        return views;
    }

    public View[] getStopwatchListViews() {
        View[] views = new View[mStopwatches.size()];
        for (int x = 0; x < views.length; x++) {
            views[x] = mStopwatches.get(x).listView;
        }

        return views;
    }

    class RefreshTask implements Runnable {
        public void run() {
            if (mClickedView == null && mMode == R.integer.mode_grid) {
                for (Holder holder : mStopwatches) {
                    updateTimeText(holder);
                    updateAverageActivePercent(holder);
                }
            }

            if (mStopwatches.size() > 0) {
                mHandler.postDelayed(new RefreshTask(), REFRESH_TIME_MS);
            }
        }
    }

    // This is used to stop the refresh of UI elements,
    // which interrupt long clicks.
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mClickedView = v;
            break;
        case MotionEvent.ACTION_UP:
            mClickedView = null;
            break;
        }
        return false;
    }

    public void onClick(final View v) {
        Holder holder = (Holder) v.getTag(R.integer.tag_holder);
        if (holder != null) {
            switch ((Integer) v.getTag(R.integer.tag_type)) {
            case R.integer.type_toggle:
                toggle(holder);
                break;
            case R.integer.type_delete:
                mStopwatches.remove(holder);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public boolean onLongClick(final View v) {
        final Holder holder = (Holder) v.getTag(R.integer.tag_holder);
        if (holder == null) {
            return false;
        }

        final Resources res = mActivity.getResources();
        final String[] choices = res.getStringArray(R.array.long_press_stopwatch_actions);
        new AlertDialog.Builder(mActivity).setTitle(holder.watch.getName()).setItems(choices,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Doing this rather than comparing indices ensures that the order
                        // of the dialog options can be changed just in the array.xml without
                        // worrying about having to change arbitrary ints (indices) elsewhere.
                        if (choices[which].equals(res.getString(R.string.stopwatch_action_toggle))) {
                            toggle(holder);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_reset))) {
                            holder.watch.reset();
                            updateAll(holder);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_graph))) {
                            Intent graphIntent = new Intent(mActivity, GraphActivity.class);
                            double[][] data = StatCalculator.calculateAverages(mMasterWatch, holder.watch);
                            graphIntent.putExtra(String.valueOf(R.integer.graph_title), holder.watch.getName());
                            graphIntent.putExtra(String.valueOf(R.integer.graph_raw_x_data), data[0]);
                            graphIntent.putExtra(String.valueOf(R.integer.graph_raw_y_data), data[1]);
                            mActivity.startActivity(graphIntent);
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_delete))) {
                            mStopwatches.remove(holder);
                            notifyDataSetChanged();
                        }
                    }
                }).create().show();

        return true;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    public void toggleMaster() {
        if (mMasterWatch.stop()) {
            for (Holder holder : mStopwatches) {
                if (holder.watch.stop()) {
                    updateTimeColor(holder);
                }
            }
            updateMasterColor();
        } else {
            mMasterWatch.start();
            updateMasterColor();
        }
    }

    public void resetMaster() {
        if (mMasterWatch.reset()) {
            for (Holder holder : mStopwatches) {
                if (holder.watch.reset()) {
                    updateAll(holder);
                }
            }
        }
    }

    private void toggle(Holder holderToToggle) {
        if (holderToToggle.watch.isStarted()) {
            holderToToggle.watch.stop();
        } else {
            for (Holder holder : mStopwatches) {
                if (holder.watch.stop()) {
                    updateTimeColor(holder);
                }
            }
            holderToToggle.watch.start();
            mMasterWatch.start();
            updateMasterColor();
        }
        updateTimeColor(holderToToggle);
    }

    private String calculateTimeString(Stopwatch watch) {
        long durationMs = watch.getDuration();
        long hours = durationMs / MS_PER_HOUR;
        durationMs -= hours * MS_PER_HOUR;
        long minutes = durationMs / MS_PER_MIN;
        durationMs -= minutes * MS_PER_MIN;
        long seconds = durationMs / MS_PER_SEC;
        durationMs -= seconds * MS_PER_SEC;

        return String.format("%s:%s:%s", getTimeSubString(hours), getTimeSubString(minutes), getTimeSubString(seconds));
    }

    private String getTimeSubString(long substring) {
        return (substring == 0 ? "00" : (substring < 10 ? "0" + substring : "" + substring));
    }

    private void updateMasterColor() {
        mActivity.findViewById(R.id.content).setBackgroundColor(
                mActivity.getResources().getColor(mMasterWatch.isStarted() ?
                        R.color.master_stopwatch_on : R.color.master_stopwatch_off));
    }

    private void updateTimeColor(Holder holder) {
        getTimeTextView(holder).setTextColor(calculateTimeColor(holder.watch));
    }

    private void updateTimeText(Holder holder) {
        getTimeTextView(holder).setText(calculateTimeString(holder.watch));
    }

    private void updateAverageActivePercent(Holder holder) {
        getActivePercentTextView(holder).setText(
                StatCalculator.calculateActivePercentString(mMasterWatch, holder.watch));
    }

    private void updateAll(Holder holder) {
        updateTimeText(holder);
        updateTimeColor(holder);
        updateAverageActivePercent(holder);
    }

    private int calculateTimeColor(Stopwatch watch) {
        return (watch.isStarted() ? mTimeOnColor : mTimeOffColor);
    }

    private TextView getGridNameTextView(Holder holder) {
        return (TextView) holder.gridView.findViewById(R.id.name);
    }

    private TextView getListNameTextView(Holder holder) {
        return (TextView) holder.listView.findViewById(R.id.name);
    }

    private ImageView getDeleteImageView(Holder holder) {
        return (ImageView) holder.listView.findViewById(R.id.delete);
    }

    private TextView getTimeTextView(Holder holder) {
        return (TextView) holder.gridView.findViewById(R.id.time);
    }

    private TextView getActivePercentTextView(Holder holder) {
        return (TextView) holder.gridView.findViewById(R.id.active_percent);
    }
}
