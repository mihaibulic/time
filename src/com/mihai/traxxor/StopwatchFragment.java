package com.mihai.traxxor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class StopwatchFragment extends Fragment {

    private StopwatchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = ((MainActivity) getActivity()).getAdapter();

        View mainView = inflater.inflate(R.layout.stopwatch_fragment, null);
        ((GridView) mainView.findViewById(R.id.stopwatch_list)).setAdapter(mAdapter);

        return mainView;
    }

    @Override
    public void onResume() {
        mAdapter.setMode(R.integer.mode_grid);
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }
}
