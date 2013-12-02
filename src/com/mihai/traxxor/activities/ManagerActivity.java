package com.mihai.traxxor.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mihai.traxxor.R;
import com.mihai.traxxor.adapters.ManagerAdapter;

public class ManagerActivity extends Activity implements OnClickListener {
    private EditText mNameView;
    private ManagerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ManagerAdapter(this);
        if (!readIntent()) {
            Toast toast = Toast.makeText(this, getString(R.string.manager_error), Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        ActionBar bar = getActionBar();
        bar.setIcon(R.drawable.ic_settings);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        setContentView(R.layout.manager_activity);
        ListView list = (ListView) findViewById(R.id.stopwatch_list);
        list.setAdapter(mAdapter);
        list.setEmptyView(findViewById(R.id.empty_view));
        findViewById(R.id.add_stopwatch).setOnClickListener(this);
        mNameView = (EditText) findViewById(R.id.stopwatch_name);
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

    @Override
    public void finish() {
        setResult(Activity.RESULT_OK, getResultIntent());
        super.finish();
    }

    public void onClick(final View v) {
        if (v.getId() == R.id.add_stopwatch) {
            CharSequence name = mNameView.getText();
            if (mAdapter.isValidName(name)) {
                mAdapter.addStopwatch(name);
            }
            mNameView.setText("");
        }
        mAdapter.notifyDataSetChanged();
    }

    private boolean readIntent() {
        String[] stopwatchNames = getIntent().getStringArrayExtra(String.valueOf(R.integer.extra_stopwatch_names));
        int[] stopwatchIds = getIntent().getIntArrayExtra(String.valueOf(R.integer.extra_stopwatch_ids));

        // If the activity is recreated when a user rotates the screen, we need to replay the changes so far
        ArrayList<String> toAdd = getIntent().getStringArrayListExtra(String.valueOf(R.integer.extra_stopwatches_to_add));
        ArrayList<Integer> toDelete = getIntent().getIntegerArrayListExtra(String.valueOf(R.integer.extra_stopwatches_to_delete));

        if (toAdd != null) {
            for (String name : toAdd) {
                if (mAdapter.isValidName(name)) {
                    mAdapter.addStopwatch(name);
                }
            }
        }

        if (toDelete != null) {
            for (int id : toDelete) {
                if (id >= 0) {
                    mAdapter.removeStopwatch(id);
                }
            }
        }

        boolean result = mAdapter.addStopwatches(stopwatchNames, stopwatchIds);

        return result;
    }

    private Intent getResultIntent() {
        ArrayList<String> toAdd = new ArrayList<String>();
        final int size = mAdapter.getCount();
        for (int i = 0; i < size; i++) {
            if (mAdapter.getItemId(i) < 0) {
                toAdd.add(mAdapter.getItem(i));
            }
        }

        Intent data = new Intent();
        data.putExtra(String.valueOf(R.integer.extra_stopwatches_to_add), toAdd);
        data.putExtra(String.valueOf(R.integer.extra_stopwatches_to_delete), mAdapter.getDeleted());

        return data;
    }
}
