package com.mihai.traxxor.ui;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;

import com.mihai.traxxor.R;
import com.mihai.traxxor.data.Stopwatch;

public class MainActivity extends Activity {
    private static final String KEY_MASTER_STOPWATCH = "key_master_stopwatch";
    private static final String KEY_STOPWATCH_LIST = "key_stopwatch_list";

    private StopwatchAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new StopwatchAdapter(this);
        restoreIfPossible(savedInstanceState);
        if (mAdapter.getMasterStopwatch() == null) {
            mAdapter.setMasterStopwatch(new Stopwatch(0, getString(R.string.master_stopwatch)));
        }
        setContentView(R.layout.main_activity);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        bar.addTab(bar.newTab()
                .setText(getString(R.string.tab_main))
                .setTabListener(new TabListener<StopwatchFragment>(
                        this, getString(R.string.tab_main), StopwatchFragment.class)));
        bar.addTab(bar.newTab()
                .setText(getString(R.string.tab_manage))
                .setTabListener(new TabListener<ManagerFragment>(
                        this, getString(R.string.tab_manage), ManagerFragment.class)));

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
                break;
            default:
                selected = false;
                break;
        }

        return selected;
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}
