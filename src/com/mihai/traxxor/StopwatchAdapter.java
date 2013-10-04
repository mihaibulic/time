package com.mihai.traxxor;

import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

public class StopwatchAdapter extends BaseAdapter implements OnTouchListener, OnClickListener, OnLongClickListener {
    private static final int REFRESH_TIME_MS = 500;
    private static final int MS_PER_SEC = 1000;
    private static final int MS_PER_MIN = MS_PER_SEC * 60;
    private static final int MS_PER_HOUR = MS_PER_MIN * 60;

    private static Context mContext;
    private static int mTimeOnColor;
    private static int mTimeOffColor;
    private static int mMode = R.integer.mode_grid;

    private Vector<Holder> mStopwatches = new Vector<Holder>();
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

    public StopwatchAdapter(Context ctx) {
        mContext = ctx;
        final Resources res = ctx.getResources();
        mTimeOnColor = res.getColor(R.color.stopwatch_on);
        mTimeOffColor = res.getColor(R.color.stopwatch_off);
    }

    public int getCount() {
        android.util.Log.v("bulic", "size " + mStopwatches.size());
        return mStopwatches.size();
    }

    public Stopwatch getItem(int position) {
        return mStopwatches.get(position).watch;
    }

    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public synchronized Stopwatch createStopwatch(boolean shouldActive, String name) {
        Stopwatch watch = null;
        if (!TextUtils.isEmpty(name)) {
            watch = new Stopwatch(mNextId++, name);
            watch.dEPstart(shouldActive);

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
            mHandler.postDelayed(new RefreshTask(), REFRESH_TIME_MS);
            if (mMode == R.integer.mode_grid) {
                getTimeTextView(holder).setText(calculateTimeString(holder.watch));
                getActivePercentTextView(holder).setText(calculateActivePercentString(holder.watch));
            }
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.stopwatch_for_dialog, null);
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.stopwatch, null);
            }
            holder.gridView = convertView;

            TextView time = getTimeTextView(holder);
            time.setText(calculateTimeString(holder.watch));
            time.setTextColor(calculateTimeColor(holder.watch));

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
                    getTimeTextView(holder).setText(calculateTimeString(holder.watch));
                    getActivePercentTextView(holder).setText(calculateActivePercentString(holder.watch));
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
            Stopwatch watch = holder.watch;
            switch ((Integer) v.getTag(R.integer.tag_type)) {
            case R.integer.type_toggle:
                watch.toggle();
                getTimeTextView(holder).setTextColor(calculateTimeColor(watch));
                break;
            case R.integer.type_delete:
                watch.DEPstop();
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

        final Resources res = mContext.getResources();
        final String[] choices = res.getStringArray(R.array.long_press_stopwatch_actions);
        new AlertDialog.Builder(mContext).setTitle(holder.watch.getName()).setItems(choices,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Stopwatch watch = holder.watch;

                        // Doing this rather than comparing indices ensures that the order
                        // of the dialog options can be changed just in the array.xml without
                        // worrying about having to change arbitrary ints (indices) elsewhere.
                        if (choices[which].equals(res.getString(R.string.stopwatch_action_toggle))) {
                            watch.toggle();
                            getTimeTextView(holder).setTextColor(calculateTimeColor(watch));
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_reset))) {
                            watch.reset();
                            getTimeTextView(holder).setTextColor(calculateTimeColor(watch));
                        } else if (choices[which].equals(res.getString(R.string.stopwatch_action_delete))) {
                            watch.DEPstop();
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

    private String calculateActivePercentString(Stopwatch watch) {
        return ((int) (watch.getActivePercentage() * 100)) + "%";
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

    private int calculateTimeColor(Stopwatch watch) {
        return (watch.isStarted() && watch.isActive() ? mTimeOnColor : mTimeOffColor);
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
