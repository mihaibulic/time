package com.mihai.traxxor.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mihai.traxxor.R;
import com.mihai.traxxor.data.Stopwatch;

public class MainActivity extends Activity {
    private static final String KEY_MASTER_STOPWATCH = "key_master_stopwatch";
    private static final String KEY_STOPWATCH_LIST = "key_stopwatch_list";

    private StopwatchAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = StopwatchAdapter.getInstance(this);
        mAdapter.setMode(R.integer.mode_grid);
        restoreIfPossible(savedInstanceState);
        initActionBar();

        setContentView(R.layout.main_activity);
        ((GridView) findViewById(R.id.stopwatch_grid)).setAdapter(mAdapter);
        mAdapter.setMasterStopwatchViews((TextView) findViewById(R.id.master_stopwatch),
                findViewById(R.id.content));
    }

    @Override
    public void onResume() {
        mAdapter.setMode(R.integer.mode_grid);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle onInstanceState) {
        super.onSaveInstanceState(onInstanceState);
        onInstanceState.putParcelable(KEY_MASTER_STOPWATCH, mAdapter.getMasterStopwatch());
        onInstanceState.putParcelableArray(KEY_STOPWATCH_LIST, mAdapter.getStopwatches());
    }

    public StopwatchAdapter getAdapter() {
        return mAdapter;
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
    }

    /**
     * @return true iff restoration was successful (there was a master stopwatch)
     */
    private void restoreIfPossible(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Parcelable masterWatch = savedInstanceState.getParcelable(KEY_MASTER_STOPWATCH);
            if (masterWatch != null && masterWatch instanceof Stopwatch) {
                mAdapter.setMasterStopwatch((Stopwatch) masterWatch);
            }
            Parcelable[] watches =
                    savedInstanceState.getParcelableArray(KEY_STOPWATCH_LIST);
            if (watches != null) {
                mAdapter.clearStopwatches();
                for (Parcelable watch : watches) {
                    if (watch instanceof Stopwatch) {
                        mAdapter.addStopwatch((Stopwatch) watch);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addMenutItem(menu, R.string.action_day_toggle);
        addMenutItem(menu, R.string.action_day_advance);
        addMenutItem(menu, R.string.action_day_reset);
        addMenutItem(menu, R.string.action_manage);
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
            case R.string.action_day_toggle:
                mAdapter.toggleMaster();
                break;
            case R.string.action_day_advance:
                mAdapter.resetMaster();
                break;
            case R.string.action_day_reset:
                mAdapter.resetMaster();
                break;
            case R.string.action_manage:
                startActivity(new Intent(this, ManagerActivity.class));
                break;
            default:
                selected = false;
                break;
        }

        return selected;
    }
}
