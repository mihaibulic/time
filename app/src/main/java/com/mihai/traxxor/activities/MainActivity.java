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

import com.mihai.traxxor.R;
import com.mihai.traxxor.adapters.StopwatchAdapter;
import com.mihai.traxxor.data.ActionHandler;
import com.mihai.traxxor.data.BundleHelper;
import com.mihai.traxxor.data.SettingsProvider;
import com.mihai.traxxor.data.Stopwatch;
import com.mihai.traxxor.data.StopwatchDataProvider;
import com.mihai.traxxor.util.Util;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static final int SETTINGS_MANAGER_REQUEST_CODE = 123;

    public static final int MASTER_ID = -1;

    private ActionHandler mActionHandler;

    private StopwatchDataProvider mStopwatchDataProvider;
    private StopwatchAdapter mAdapter;
    private Stopwatch mMasterWatch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStopwatchDataProvider = new StopwatchDataProvider(this);
        SettingsProvider settingsProvider = new SettingsProvider(this);
        // TODO add UI for setting getupfreq
        // TODO persist ActionHandler to DB
        mActionHandler = new ActionHandler(this,null, settingsProvider.getTimeAtWorkHours(), 90);

        initMasterWatchAndAdapter(savedInstanceState);

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
    }

    @Override
    public void onStop() {
        mStopwatchDataProvider.writeToTable(mAdapter.getStopwatches(), mMasterWatch, StopwatchDataProvider.TABLE_TYPE_TEMP);
        super.onStop();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == SETTINGS_MANAGER_REQUEST_CODE) {
            mActionHandler.setTimeAtWork(data.getFloatExtra(String.valueOf(R.integer.extra_time_at_work_hours), 6));
            mActionHandler.setGetUpFrequency(data.getIntExtra(String.valueOf(R.integer.extra_get_up_frequency_minutes), 90));

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

    public Stopwatch getMasterStopwatch() {
        return mMasterWatch;
    }

    private void resetMaster() {
        if (mMasterWatch.reset()) {
            mActionHandler.reset();
            mAdapter.resetAllStopwatches();

            updateMaster();
            updateTimeAtWork();
        }
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

    private void updateTimeAtWork() {
        // Calculate text
        long timeRemaining = mActionHandler.getTimeAtWorkRemainingMs(mMasterWatch.getCurrentDuration());

        // Update text
        TextView timeAtWorRemaining = findViewById(R.id.time_at_work_remaining);
        final int textColor = getResources().getColor(timeRemaining >= 0 ? R.color.timeAtWork_positive: R.color.timeAtWork_negative);
        timeAtWorRemaining.setText(getString(R.string.action_bar_time_at_work_remaining,
                Util.calculateTimeString(timeRemaining)));
        timeAtWorRemaining.setTextColor(textColor);
    }

    public void refresh() {
        mActionHandler.handleActionsIfNeeded(mMasterWatch.getCurrentDuration());

        updateMaster();
        updateTimeAtWork();
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

        View toggle = view.findViewById(R.id.toggle_day);
        if (toggle != null) {
            toggle.setOnClickListener(this);
        }
        View manage = view.findViewById(R.id.manage);
        if (manage != null) {
            manage.setOnClickListener(this);
        }
    }

    /**
     * tries to restore from the savedInstanceState, otherwise queries the temp tables in the DB
     */
    private void initMasterWatchAndAdapter(Bundle savedInstanceState) {
        mAdapter = new StopwatchAdapter(this);
        mMasterWatch = null;
        if (savedInstanceState != null) {
            mMasterWatch = BundleHelper.readMasterStopwatchFromBundle(savedInstanceState);
            mAdapter.addStopwatches(BundleHelper.readStopwatchesFromBundle(savedInstanceState));
        } else {
            // TODO it looks like if the app is opening fresh / isn't being restored, and hasn't been opened today - there will be no stopwatches
            // since we restore them based on the day.  Make day optional? If omitted just find latest days watches and load that?
            mAdapter.setNextId(mStopwatchDataProvider.getNextStopwatchId());
            Pair<Stopwatch, ArrayList<Stopwatch>> pair = mStopwatchDataProvider.readFromTable(Util.getToday(), StopwatchDataProvider.TABLE_TYPE_TEMP);

            mMasterWatch = pair.first;
            mAdapter.addStopwatches(pair.second);

            // TODO this is a bit of a hack to seed the stopwatches
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manage:
                startActivityForResult(getManagerIntent(), SETTINGS_MANAGER_REQUEST_CODE);
                break;
            case R.id.toggle_day:
                toggleMaster();
                break;
        }
    }
}
