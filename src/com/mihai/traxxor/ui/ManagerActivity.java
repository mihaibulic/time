package com.mihai.traxxor.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.mihai.traxxor.R;

public class ManagerActivity extends Activity implements OnClickListener {

    private EditText mNameView;
    private StopwatchAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = StopwatchAdapter.getInstance(this);
        mAdapter.setMode(R.integer.mode_list);

        ActionBar bar = getActionBar();
        bar.setIcon(R.drawable.ic_action_bar_logo);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        setContentView(R.layout.manager_activity);
        ((ListView) findViewById(R.id.stopwatch_list)).setAdapter(mAdapter);
        findViewById(R.id.add_stopwatch).setOnClickListener(this);
        mNameView = (EditText) findViewById(R.id.stopwatch_name);
    }

    @Override
    public void onResume() {
        mAdapter.setMode(R.integer.mode_list);
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(final View v) {
        mAdapter.createStopwatch(false /* shouldActivate */, mNameView.getText());
        mAdapter.notifyDataSetChanged();
        mNameView.setText("");
    }

    @Override
    public void onPause() {
        // Hide keyboard
        /*        InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        */
        super.onPause();
    }
}
