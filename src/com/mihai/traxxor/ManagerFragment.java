package com.mihai.traxxor;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class ManagerFragment extends Fragment implements OnClickListener {

    private EditText mNameView;
    private StopwatchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = ((MainActivity) getActivity()).getAdapter();

        View mainView = inflater.inflate(R.layout.manager_fragment, null);
        ((ListView) mainView.findViewById(R.id.list)).setAdapter(mAdapter);
        mainView.findViewById(R.id.add).setOnClickListener(this);
        mNameView = (EditText) mainView.findViewById(R.id.name);

        return mainView;
    }

    @Override
    public void onResume() {
        mAdapter.setMode(R.integer.mode_list);
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public void onClick(final View v) {
        mAdapter.createStopwatch(false /* shouldActivate */, mNameView.getText().toString());
        mAdapter.notifyDataSetChanged();
        mNameView.setText("");
    }
}
