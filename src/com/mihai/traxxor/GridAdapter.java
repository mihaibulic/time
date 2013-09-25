package com.mihai.traxxor;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter implements OnClickListener {
    private static final int REFRESH_TIME_MS = 500;
    private static final int MS_PER_SEC = 1000;
    private static final int MS_PER_MIN = MS_PER_SEC * 60;
    private static final int MS_PER_HOUR = MS_PER_MIN * 60;

    private static final int TYPE_RESET = 0;
    private static final int TYPE_DELETE = 1;
    private static final int TYPE_TOGGLE = 2;

    private static Context mContext;
    private static int mTimeOnColor;
    private static int mTimeOffColor;

    private Vector<Holder> mStopwatches = new Vector<Holder>();
    private Handler mHandler;
    private int mNextId = 0;

    private class Holder {
        int id;
        Stopwatch watch;
        View view;

        public Holder(Stopwatch watch) {
            this.id = watch.getId();
            this.watch = watch;
        }

        @Override
        public boolean equals(Object a) {
            return (a != null) && (a instanceof Holder) && ((Holder) a).id == id;
        }
    }

    public GridAdapter(Context ctx) {
        mContext = ctx;
        final Resources res = ctx.getResources();
        mTimeOnColor = res.getColor(R.color.stopwatch_on);
        mTimeOffColor = res.getColor(R.color.stopwatch_off);
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
        Stopwatch watch = new Stopwatch(mNextId++, name);
        if (start) {
            watch.start();
        }

        Holder holder = new Holder(watch);
        synchronized (mStopwatches) {
            mStopwatches.add(holder);
        }
        holder.view = getView(mStopwatches.size() - 1, null, null);

        if (mHandler == null) {
            mHandler = new Handler();
        }

        // restart refresher task, since before this we had no stopwatches
        if (mStopwatches.size() == 1) {
            mHandler.postDelayed(new RefreshTask(), REFRESH_TIME_MS);
        }

        notifyDataSetChanged();

        return watch;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < 0 || position >= mStopwatches.size()) {
            return null;
        }

        Holder holder = mStopwatches.get(position);
        if (holder.view != null) {
            convertView = holder.view;
        } else {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.stopwatch, null);
            }

            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText(calculateTimeString(holder.watch));
            time.setTextColor(calculateTimeColor(holder.watch));
            time.setTag(R.integer.tag_type, TYPE_TOGGLE);
            time.setTag(R.integer.tag_holder, holder);
            time.setOnClickListener(this);

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText(holder.watch.getName());

            ImageView delete = (ImageView) convertView.findViewById(R.id.delete);
            delete.setTag(R.integer.tag_type, TYPE_DELETE);
            delete.setTag(R.integer.tag_holder, holder);
            delete.setOnClickListener(this);

            ImageView reset = (ImageView) convertView.findViewById(R.id.reset);
            reset.setTag(R.integer.tag_type, TYPE_RESET);
            reset.setTag(R.integer.tag_holder, holder);
            reset.setOnClickListener(this);

            holder.view = convertView;
        }

        return convertView;
    }

    class RefreshTask implements Runnable {
        public void run() {
            synchronized (mStopwatches) {
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

    public void onClick(View v) {
        Holder holder = (Holder) v.getTag(R.integer.tag_holder);
        if (holder != null) {
            Stopwatch watch = holder.watch;
            switch ((Integer) v.getTag(R.integer.tag_type)) {
            case TYPE_TOGGLE:
                watch.toggle();
                getTimeTextView(holder).setTextColor(calculateTimeColor(watch));
                break;
            case TYPE_RESET:
                watch.reset();
                getTimeTextView(holder).setTextColor(calculateTimeColor(watch));
                break;
            case TYPE_DELETE:
                watch.stop();
                synchronized (mStopwatches) {
                    mStopwatches.remove(holder);
                }
                notifyDataSetChanged();
                break;
            }
        }
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


    private TextView getTimeTextView(Holder holder) {
        return (TextView) holder.view.findViewById(R.id.time);
    }

    private TextView getActivePercentTextView(Holder holder) {
        return (TextView) holder.view.findViewById(R.id.active_percent);
    }
}
