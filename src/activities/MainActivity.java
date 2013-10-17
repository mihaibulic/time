package activities;

import java.util.ArrayList;

import adapters.StopwatchAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mihai.traxxor.R;
import com.mihai.traxxor.data.Stopwatch;
import com.mihai.traxxor.util.Util;

public class MainActivity extends Activity implements OnClickListener {
    public static final String KEY_MASTER_STOPWATCH = "key_master_stopwatch";
    public static final String KEY_STOPWATCH_LIST = "key_stopwatch_list";
    public static final int ANIMATION_DURATION = 100; // MS

    private static int sMasterOnColor;
    private static int sMasterOffColor;

    private Stopwatch mMasterWatch;
    private StopwatchAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWatches(savedInstanceState);
        initActionBar();

        setContentView(R.layout.main_activity);
        ((GridView) findViewById(R.id.stopwatch_grid)).setAdapter(mAdapter);

        updateMaster();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle onInstanceState) {
        super.onSaveInstanceState(onInstanceState);
        onInstanceState.putParcelable(KEY_MASTER_STOPWATCH, getMasterStopwatch());
        onInstanceState.putParcelableArrayList(KEY_STOPWATCH_LIST, mAdapter.getStopwatches());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addMenutItem(menu, R.string.action_manage);
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
            case R.string.action_day_toggle:
                toggleMaster();
                break;
            case R.string.action_day_advance:
                resetMaster();
                break;
            case R.string.action_day_reset:
                resetMaster();
                break;
            case R.string.action_manage:
                startActivityForResult(getManagerIntent(), R.integer.manager_request_code);
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
            ArrayList<String> names = data.getStringArrayListExtra(ManagerActivity.EXTRA_STOPWATCHES_TO_ADD);
            ArrayList<Integer> ids = data.getIntegerArrayListExtra(ManagerActivity.EXTRA_STOPWATCHES_TO_DELETE);

            if (names != null) {
                for (String name : names) {
                    mAdapter.createStopwatch(false, name);
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
     * tries to restore from the savedInstanceState, otherwise initializes what needs to be
     */
    private void initWatches(Bundle savedInstanceState) {
        mAdapter = new StopwatchAdapter(this);
        Parcelable master = null;
        if (savedInstanceState != null) {
            master = savedInstanceState.getParcelable(KEY_MASTER_STOPWATCH);
            ArrayList<Parcelable> watches = savedInstanceState.getParcelableArrayList(KEY_STOPWATCH_LIST);
            if (watches != null) {
                mAdapter.clearStopwatches();
                for (Parcelable watch : watches) {
                    if (watch instanceof Stopwatch) {
                        mAdapter.addStopwatch((Stopwatch) watch);
                    }
                }
            }
        }

        if (master instanceof Stopwatch) {
            mMasterWatch = (Stopwatch) master;
        } else if (mMasterWatch == null) {
            mMasterWatch = new Stopwatch(-1, getString(R.string.master_stopwatch_description));
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
        intent.putExtra(ManagerActivity.EXTRA_STOPWATCH_NAMES, stopwatchNames);
        intent.putExtra(ManagerActivity.EXTRA_STOPWATCH_IDS, stopwatchIds);

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
