package com.mihai.traxxor.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.mihai.traxxor.R;
import com.mihai.traxxor.data.Stopwatch;

public class StopwatchFragment extends Fragment {
    private static final int REFRESH_TIME_MS = 500;
    private static final int MS_PER_SEC = 1000;
    private static final int MS_PER_MIN = MS_PER_SEC * 60;
    private static final int MS_PER_HOUR = MS_PER_MIN * 60;

    private StopwatchAdapter mAdapter;
    private Handler mHandler;
    private TextView mMaster;

    class RefreshTask implements Runnable {
        public void run() {
            mMaster.setText(calculateTimeString(mAdapter.getMasterStopwatch()));
            mHandler.postDelayed(new RefreshTask(), REFRESH_TIME_MS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = ((MainActivity) getActivity()).getAdapter();
        mHandler = new Handler();
        mHandler.post(new RefreshTask());

        View mainView = inflater.inflate(R.layout.stopwatch_fragment, null);
        ((GridView) mainView.findViewById(R.id.stopwatch_grid)).setAdapter(mAdapter);

        mMaster = (TextView) mainView.findViewById(R.id.master_stopwatch);

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setMode(R.integer.mode_grid);
        mAdapter.notifyDataSetChanged();
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
}
