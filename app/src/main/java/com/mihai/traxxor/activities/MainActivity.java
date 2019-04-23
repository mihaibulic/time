package com.mihai.traxxor.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mihai.traxxor.R;
import com.mihai.traxxor.adapters.StopwatchAdapter;
import com.mihai.traxxor.data.BundleHelper;
import com.mihai.traxxor.data.EventHandler;
import com.mihai.traxxor.data.SettingsProvider;
import com.mihai.traxxor.data.Stopwatch;
import com.mihai.traxxor.data.StopwatchDataProvider;
import com.mihai.traxxor.util.Util;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnClickListener, EventHandler.Listener {
    private static final int SETTINGS_MANAGER_REQUEST_CODE = 123;
    public static final int MASTER_ID = -1;

    private StopwatchDataProvider mStopwatchDataProvider;
    private SettingsProvider mSettingsProvider;
    private EventHandler mEventHandler;
    private StopwatchAdapter mAdapter;
    private Stopwatch mMasterWatch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStopwatchDataProvider = new StopwatchDataProvider(this);
        mSettingsProvider = new SettingsProvider(this);
        initMasterWatchAndAdapter(savedInstanceState, mStopwatchDataProvider);
        initEventHandler(savedInstanceState, mSettingsProvider);
        initActionBar();

        setContentView(R.layout.main_activity);

        GridView grid = findViewById(R.id.stopwatch_grid);
        grid.setAdapter(mAdapter);
        grid.setEmptyView(findViewById(R.id.empty_view));

        updateMaster();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle onInstanceState) {
        super.onSaveInstanceState(onInstanceState);

        BundleHelper.writeStopwatchesToBundle(onInstanceState, mAdapter.getStopwatches(), mMasterWatch);
        BundleHelper.writeEventHandlerToBundle(onInstanceState, mEventHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addMenuItem(menu, R.string.action_manage);
        addMenuItem(menu, R.string.action_graph_discrete);
        addMenuItem(menu, R.string.action_graph_cumulative);
        addMenuItem(menu, R.string.action_day_toggle);
        addMenuItem(menu, R.string.action_day_advance);
        addMenuItem(menu, R.string.action_day_reset);
        return true;
    }

    private void addMenuItem(Menu menu, int resId) {
        menu.add(resId).setTitleCondensed(String.valueOf(resId));
    }

    private int getMenuItemResId(MenuItem item) {
        return Integer.parseInt(item.getTitleCondensed().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selected = true;

        switch (getMenuItemResId(item)) {
            case R.string.action_graph_discrete:
                Util.graphStopwatches(this, mAdapter.getStopwatches(), mMasterWatch, false);
                break;
            case R.string.action_graph_cumulative:
                Util.graphStopwatches(this, mAdapter.getStopwatches(), mMasterWatch, true);
                break;
            case R.string.action_manage:
                startActivityForResult(getManagerIntent(), SETTINGS_MANAGER_REQUEST_CODE);
                break;
            case R.string.action_day_toggle:
                toggleMaster();
                break;
            case R.string.action_day_advance:
                if (mMasterWatch.isStarted()) {
                    toggleMaster();
                }
                mStopwatchDataProvider.writeToTable(mAdapter.getStopwatches(), mMasterWatch, StopwatchDataProvider.TABLE_TYPE_PERM);
                resetMaster();
                break;
            case R.string.action_day_reset:
                resetMaster();
                break;
            default:
                selected = false;
                break;
        }

        return selected;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_up:
                mEventHandler.recordGetUpEvent(mMasterWatch.getCurrentDuration());
                break;
            case R.id.manage:
                startActivityForResult(getManagerIntent(), SETTINGS_MANAGER_REQUEST_CODE);
                break;
            case R.id.toggle_day:
                toggleMaster();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == SETTINGS_MANAGER_REQUEST_CODE) {
            mEventHandler.setTimeToLeave(mSettingsProvider.getTimeToLeaveHours());
            mEventHandler.setTimeToGetUp(mSettingsProvider.getTimeToGetUpMinutes());

            // TODO(P3): use StopWatchDataProvider as source of truth instead of result intent.
            ArrayList<String> names = data.getStringArrayListExtra(String.valueOf(R.integer.extra_stopwatches_to_add));
            ArrayList<Integer> ids = data.getIntegerArrayListExtra(String.valueOf(R.integer.extra_stopwatches_to_delete));

            if (names != null) {
                for (String name : names) {
                    mAdapter.createStopwatch(name);
                }
            }

            if (ids != null) {
                for (int id : ids) {
                    mAdapter.removeStopwatch(id);
                }
            }
        }
    }

    @Override
    public void onStop() {
        // TODO(P2) persist EventHandler to DB? (fix this and P2 in initMasterWatchAndAdapter at same time / switch to Room?)
        mStopwatchDataProvider.writeToTable(mAdapter.getStopwatches(), mMasterWatch, StopwatchDataProvider.TABLE_TYPE_TEMP);
        super.onStop();
    }

    @Override
    public void onTimeToLeaveEvent() {
        Toast.makeText(this, R.string.on_time_to_leave_event_toast, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTimeToGetUpEvent() {
        Toast.makeText(this, R.string.on_time_to_get_up_event_toast, Toast.LENGTH_LONG).show();
    }

    public void refresh() {
        mEventHandler.handleEventsIfNeeded(mMasterWatch.getCurrentDuration());

        updateMaster();
        updateTimeToLeave();
        updateTimeToGetUp();
    }

    public Stopwatch getMasterStopwatch() {
        return mMasterWatch;
    }

    public void startMaster() {
        if (mMasterWatch.start()) {
            updateMaster();
            mAdapter.startRefresh();
        }
    }

    private void toggleMaster() {
        if (mMasterWatch.stop()) {
            updateMaster();
            mAdapter.stopAllStopwatches();
            mAdapter.stopRefresh();
        } else {
            startMaster();
        }
    }

    private void updateMaster() {
        // Update text
        ((TextView) findViewById(R.id.master_stopwatch))
                .setText(Util.calculateTimeString(mMasterWatch));

        // Update color
        final int color = getResources().getColor(mMasterWatch.isStarted()
                ? R.color.master_stopwatch_on
                : R.color.master_stopwatch_off);
        findViewById(R.id.content).setBackgroundColor(color);
    }

    private void resetMaster() {
        if (mMasterWatch.reset()) {
            mEventHandler.reset();
            mAdapter.resetAllStopwatches();

            updateMaster();
            updateTimeToLeave();
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_action_bar_logo);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.action_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        actionBar.setCustomView(view, params);

        view.findViewById(R.id.toggle_day).setOnClickListener(this);
        view.findViewById(R.id.manage).setOnClickListener(this);
        view.findViewById(R.id.get_up).setOnClickListener(this);
    }

    /**
     * tries to restore from the savedInstanceState, otherwise queries the temp tables in the DB
     */
    private void initMasterWatchAndAdapter(Bundle savedInstanceState, StopwatchDataProvider stopwatchDataProvider) {
        mAdapter = new StopwatchAdapter(this);
        mMasterWatch = null;
        if (savedInstanceState != null) {
            mMasterWatch = BundleHelper.readMasterStopwatchFromBundle(savedInstanceState);
            mAdapter.addStopwatches(BundleHelper.readStopwatchesFromBundle(savedInstanceState));
        } else {
            // TODO(P2) it looks like if the app is opening fresh / isn't being restored, and hasn't been opened today - there will be no stopwatches
            // since we restore them based on the day.  Make day optional? If omitted just find latest days watches and load that?
            // Fix this and P2 in onStop at same time / switch to Room?
            mAdapter.setNextId(stopwatchDataProvider.getNextStopwatchId());
            Pair<Stopwatch, ArrayList<Stopwatch>> pair = stopwatchDataProvider.readFromTable(Util.getToday(), StopwatchDataProvider.TABLE_TYPE_TEMP);

            mMasterWatch = pair.first;
            mAdapter.addStopwatches(pair.second);

            // TODO(P4) this is a bit of a hack to seed the stopwatches
            if (mAdapter.getCount() == 0) {
                mAdapter.createStopwatch("Online Comms");
                mAdapter.createStopwatch("Offline Comms");
                mAdapter.createStopwatch("IC Work");
                mAdapter.createStopwatch("IC Review");
                mAdapter.createStopwatch("Meetings");
                mAdapter.createStopwatch("Organization");
                mAdapter.createStopwatch("Non-Work");
            }
        }

        if (mMasterWatch == null) {
            mMasterWatch = new Stopwatch(MASTER_ID, getString(R.string.action_bar_master_stopwatch_description));
        }

        if (mMasterWatch.isStarted()) {
            mAdapter.startRefresh();
        }
    }

    private void initEventHandler(Bundle savedInstanceState, SettingsProvider settingsProvider) {
        if (savedInstanceState != null) {
            mEventHandler = BundleHelper.readEventHandlerFromBundle(savedInstanceState, this, this);
        }

        if (mEventHandler == null) {
            mEventHandler = new EventHandler(this, this, settingsProvider.getTimeToLeaveHours(), settingsProvider.getTimeToGetUpMinutes());
        }
    }

    private void updateTimeToLeave() {
        // Calculate text
        long timeRemaining = mEventHandler.getTimeToLeaveRemainingMs(mMasterWatch.getCurrentDuration());

        // Update text
        TextView timeAtWorRemaining = findViewById(R.id.time_to_leave_remaining);
        final int textColor = getResources().getColor(timeRemaining >= 0 ? R.color.timeToX_positive : R.color.timeToX_negative);
        timeAtWorRemaining.setText(getString(R.string.action_bar_time_to_leave_remaining,
                Util.calculateTimeString(timeRemaining)));
        timeAtWorRemaining.setTextColor(textColor);
    }

    private void updateTimeToGetUp() {
        // Calculate text
        long timeRemaining = mEventHandler.getTimeToGetUpRemainingMs(mMasterWatch.getCurrentDuration());

        // Update text
        TextView timeToGetUpRemaining = findViewById(R.id.time_to_get_up_remaining);
        final int textColor = getResources().getColor(timeRemaining >= 0 ? R.color.timeToX_positive : R.color.timeToX_negative);
        timeToGetUpRemaining.setText(getString(R.string.action_bar_time_to_get_up_remaining,
                Util.calculateTimeString(timeRemaining)));
        timeToGetUpRemaining.setTextColor(textColor);
    }

    private Intent getManagerIntent() {
        Intent intent = new Intent(this, SettingsManagerActivity.class);
        String[] stopwatchNames = new String[mAdapter.getCount()];
        int[] stopwatchIds = new int[mAdapter.getCount()];
        for (int i = 0; i < stopwatchNames.length; i++) {
            stopwatchNames[i] = mAdapter.getItem(i).getName();
            stopwatchIds[i] = mAdapter.getItem(i).getId();
        }
        intent.putExtra(String.valueOf(R.integer.extra_stopwatch_names), stopwatchNames);
        intent.putExtra(String.valueOf(R.integer.extra_stopwatch_ids), stopwatchIds);

        return intent;
    }

}

