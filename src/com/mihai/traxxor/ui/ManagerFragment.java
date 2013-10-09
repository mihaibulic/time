package com.mihai.traxxor.ui;

import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.mihai.traxxor.R;

public class ManagerFragment extends Fragment implements OnClickListener {

    private EditText mNameView;
    private StopwatchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = ((MainActivity) getActivity()).getAdapter();

        View mainView = inflater.inflate(R.layout.manager_fragment, null);
        ((ListView) mainView.findViewById(R.id.stopwatch_list)).setAdapter(mAdapter);
        mainView.findViewById(R.id.add_stopwatch).setOnClickListener(this);
        mNameView = (EditText) mainView.findViewById(R.id.stopwatch_name);

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setMode(R.integer.mode_list);
        mAdapter.notifyDataSetChanged();
    }

    public void onClick(final View v) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);

        mAdapter.createStopwatch(false /* shouldActivate */, formatName(mNameView.getText()));
        mAdapter.notifyDataSetChanged();
        mNameView.setText("");
    }

    private String formatName(CharSequence name) {
        String in = name.toString().trim();
        String[] split = in.split(" ");

        String retValue = "";
        for(String s : split) {
            retValue += s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1) + " ";
        }

        return retValue.trim();
    }
}
