package com.mihai.traxxor.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.mihai.traxxor.data.BundleHelper;
import com.mihai.traxxor.data.DbHelper;
import com.mihai.traxxor.data.Stopwatch;
import com.mihai.traxxor.util.Util;


public class MainActivity extends Activity implements OnClickListener {
    public static final int ANIMATION_DURATION = 100; // MS
    public static final int MASTER_ID = -1;

    private static int sMasterOnColor;
    private static int sMasterOffColor;

    private Stopwatch mMasterWatch;
    private StopwatchAdapter mAdapter;
    private DbHelper mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new DbHelper(this);
        initWatches(savedInstanceState);
        initActionBar();

        setContentView(R.layout.main_activity);
        GridView grid = ((GridView) findViewById(R.id.stopwatch_grid));
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
        mDbHelper.writeToTable(mAdapter.getStopwatches(), mMasterWatch, DbHelper.TABLE_TYPE_TEMP);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addMenutItem(menu, R.string.action_manage);
        addMenutItem(menu, R.string.action_graph_discrete);
        addMenutItem(menu, R.string.action_graph_cumulative);
        addMenutItem(menu, R.string.action_day_toggle);
        addMenutItem(menu, R.string.action_day_advance);
        addMenutItem(menu, R.string.action_day_reset);
        return true;
    }

    private void addMenutItem(Menu menu, int resId) {
        menu.add(resId).setTitleCondensed(String.valueOf(resId));
    }

    private int getMenuItemResId(MenuItem item) {
        return Integer.parseInt(item.getTitleCondensed().toString());
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        boolean selected = true;

        switch (getMenuItemResId(item)) {
            case R.string.action_graph_discrete:
                Util.graphStopwatches(this, mAdapter.getStopwatches(), mMasterWatch, false);
                break;
            case R.string.action_graph_cumulative:
                Util.graphStopwatches(this, mAdapter.getStopwatches(), mMasterWatch, true);
                break;
            case R.string.action_manage:
                startActivityForResult(getManagerIntent(), R.integer.manager_request_code);
                break;
            case R.string.action_day_toggle:
                toggleMaster();
                break;
            case R.string.action_day_advance:
                if (mMasterWatch.isStarted()) {
                    toggleMaster();
                }
                mDbHelper.writeToTable(mAdapter.getStopwatches(), mMasterWatch, DbHelper.TABLE_TYPE_PERM);
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
        if (resultCode == Activity.RESULT_OK && requestCode == R.integer.manager_request_code) {
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

    public void resetMaster() {
        if (mMasterWatch.reset()) {
            updateMaster();
            mAdapter.resetAllStopwatches();
        }
    }

    public void startMaster() {
        if (mMasterWatch.start()) {
            updateMaster();
            mAdapter.startRefresh();
        }
    }

    public void toggleMaster() {
        if (mMasterWatch.stop()) {
            updateMaster();
            mAdapter.stopAllStopwatches();
            mAdapter.stopRefresh();
        } else {
            startMaster();
        }
    }

    public StopwatchAdapter getAdapter() {
        return mAdapter;
    }

    public void updateMasterColor() {
        final int color = mMasterWatch.isStarted() ? sMasterOnColor : sMasterOffColor;
        findViewById(R.id.content).setBackgroundColor(color);
    }

    public void updateMasterTime() {
        ((TextView) findViewById(R.id.master_stopwatch))
                .setText(Util.calculateTimeString(mMasterWatch));
    }

    public void updateMaster() {
        updateMasterColor();
        updateMasterTime();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.ic_action_bar_logo);
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflator.inflate(R.layout.action_bar, null);
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
    private void initWatches(Bundle savedInstanceState) {
        mAdapter = new StopwatchAdapter(this);
        mMasterWatch = null;
        if (savedInstanceState != null) {
            mAdapter.addStopwatches(BundleHelper.readStopwatchesFromBundle(savedInstanceState, mMasterWatch));
        } else {
            mAdapter.setNextId(mDbHelper.getNextStopwatchId());
            Pair<Stopwatch, ArrayList<Stopwatch>> pair = mDbHelper.readFromTable(Util.getToday(), DbHelper.TABLE_TYPE_TEMP);
            mMasterWatch = pair.first;
            mAdapter.addStopwatches(pair.second);
        }

        if (mMasterWatch == null) {
            mMasterWatch = new Stopwatch(MASTER_ID, getString(R.string.master_stopwatch_description));
        }

        if (mMasterWatch.isStarted()) {
            mAdapter.startRefresh();
        }

        final Resources res = getResources();
        sMasterOnColor = res.getColor(R.color.master_stopwatch_on);
        sMasterOffColor = res.getColor(R.color.master_stopwatch_off);
    }

    private Intent getManagerIntent() {
        Intent intent = new Intent(this, ManagerActivity.class);
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
        if (v.getId() == R.id.manage) {
            startActivityForResult(getManagerIntent(), R.integer.manager_request_code);
        } else if (v.getId() == R.id.toggle_day) {
            toggleMaster();
        }
    }
}
