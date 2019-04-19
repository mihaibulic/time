package com.mihai.traxxor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mihai.traxxor.R;
import com.mihai.traxxor.adapters.SettingsManagerAdapter;
import com.mihai.traxxor.data.SettingsProvider;

import java.util.ArrayList;

import androidx.annotation.FloatRange;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

public class SettingsManagerActivity extends AppCompatActivity implements OnClickListener {

    private TextView mTimeAtWorkLabel;
    private AppCompatSeekBar mTimeAtWorkSeekBar;
    private EditText mNameView;

    private SettingsProvider mSettingsProvider;

    private SettingsManagerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SettingsManagerAdapter(this);
        if (!readIntent()) {
            Toast toast = Toast.makeText(this, getString(R.string.settings_manager_error), Toast.LENGTH_LONG);
            toast.show();
            finish();
        }

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        mSettingsProvider = new SettingsProvider(this);

        setContentView(R.layout.settings_manager_activity);

        mTimeAtWorkLabel = findViewById(R.id.time_at_work_label);
        mTimeAtWorkSeekBar = findViewById(R.id.time_at_work_seek_bar);

        float timeAtWorkHours = mSettingsProvider.getTimeAtWorkHours();
        int timeAtWorkProgress = convertHoursToSeekbar(timeAtWorkHours);
        mTimeAtWorkSeekBar.setProgress(timeAtWorkProgress);
        updateTimeAtWorkLabel(timeAtWorkProgress);
        mTimeAtWorkSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int timeAtWorkProgress, boolean fromUser) {
                updateTimeAtWorkLabel(timeAtWorkProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // noop
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // noop
            }
        });

        ListView list = findViewById(R.id.stopwatch_list);
        list.setAdapter(mAdapter);
        list.setEmptyView(findViewById(R.id.empty_view));
        findViewById(R.id.add_stopwatch).setOnClickListener(this);
        mNameView = findViewById(R.id.stopwatch_name);
        mNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addStopwatch();
                    handled = true;
                }
                return handled;
            }
        });
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
        setResult(AppCompatActivity.RESULT_OK, getResultIntent());

        mSettingsProvider.setTimeAtWork(getTimeAtWorkHours());

        super.finish();
    }

    public void onClick(final View v) {
        if (v.getId() == R.id.add_stopwatch) {
            addStopwatch();
        }
    }

    private void addStopwatch() {
        CharSequence name = mNameView.getText();
        if (mAdapter.isValidName(name)) {
            mAdapter.addStopwatch(name);
        }
        mNameView.setText("");
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

        return mAdapter.addStopwatches(stopwatchNames, stopwatchIds);
    }

    private Intent getResultIntent() {
        ArrayList<String> toAdd = new ArrayList<>();
        final int size = mAdapter.getCount();
        for (int i = 0; i < size; i++) {
            if (mAdapter.getItemId(i) < 0) {
                toAdd.add(mAdapter.getItem(i));
            }
        }

        Intent data = new Intent();
        data.putExtra(String.valueOf(R.integer.extra_time_at_work_hours), getTimeAtWorkHours());
        data.putExtra(String.valueOf(R.integer.extra_stopwatches_to_add), toAdd);
        data.putExtra(String.valueOf(R.integer.extra_stopwatches_to_delete), mAdapter.getDeleted());

        return data;
    }

    private void updateTimeAtWorkLabel(int progress) {
        mTimeAtWorkLabel.setText(getString(R.string.settings_manager_time_at_work_label, convertSeekbarToHours(progress)));
    }

    private static final int TIME_AT_WORK_MIN_HOURS = 6;
    private static final int TIME_AT_WORK_MAX_HOURS = 12;

    /**
     * min is 6 hours, max is 12 hours.
     */
    private static int convertHoursToSeekbar(@FloatRange(from = TIME_AT_WORK_MIN_HOURS, to = TIME_AT_WORK_MAX_HOURS) float hours) {
        return (int) (100 * (hours - TIME_AT_WORK_MIN_HOURS) / (TIME_AT_WORK_MAX_HOURS - TIME_AT_WORK_MIN_HOURS));
    }

    /**
     * min is 6 hours, max is 12 hours.
     */
    private static float convertSeekbarToHours(int seekBarProgress) {
        return TIME_AT_WORK_MIN_HOURS + (seekBarProgress / 100.0f) * (TIME_AT_WORK_MAX_HOURS - TIME_AT_WORK_MIN_HOURS);
    }

    private float getTimeAtWorkHours() {
        return convertSeekbarToHours(mTimeAtWorkSeekBar.getProgress());
    }
}
