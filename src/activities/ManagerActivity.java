package activities;

import java.util.ArrayList;

import adapters.ManagerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mihai.traxxor.R;

public class ManagerActivity extends Activity implements OnClickListener {
    public static final String EXTRA_STOPWATCH_NAMES = "extra_stopwatch_names";
    public static final String EXTRA_STOPWATCH_IDS = "extra_stopwatch_ids";

    public static final String EXTRA_STOPWATCHES_TO_ADD = "extra_stopwatches_to_add";
    public static final String EXTRA_STOPWATCHES_TO_DELETE = "extra_stopwatches_to_delete";

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
        bar.setIcon(R.drawable.ic_action_bar_logo);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        setContentView(R.layout.manager_activity);
        ((ListView) findViewById(R.id.stopwatch_list)).setAdapter(mAdapter);
        findViewById(R.id.add_stopwatch).setOnClickListener(this);
        mNameView = (EditText) findViewById(R.id.stopwatch_name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Activity.RESULT_OK, getResultIntent());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(final View v) {
        if (v.getId() == R.id.add_stopwatch) {
            CharSequence name = mNameView.getText();
            if (!TextUtils.isEmpty(name)) {
                mAdapter.addStopwatch(name);
                mNameView.setText("");
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private boolean readIntent() {
        String[] stopwatchNames = getIntent().getStringArrayExtra(EXTRA_STOPWATCH_NAMES);
        int[] stopwatchIds = getIntent().getIntArrayExtra(EXTRA_STOPWATCH_IDS);

        return mAdapter.addStopwatches(stopwatchNames, stopwatchIds);
    }

    private Intent getResultIntent() {
        ArrayList<String> toAdd = new ArrayList<String>();
        final int size = mAdapter.getCount();
        for (int i = 0; i < size; i++) {
            if (mAdapter.getItemId(i) == -1) {
                toAdd.add(mAdapter.getItem(i));
            }
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_STOPWATCHES_TO_ADD, toAdd);
        data.putExtra(EXTRA_STOPWATCHES_TO_DELETE, mAdapter.getDeleted());

        return data;
    }
}
