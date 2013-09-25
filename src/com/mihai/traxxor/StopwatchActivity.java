package com.mihai.traxxor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
//import android.content.SharedPreferences;

public class StopwatchActivity extends Activity {

    // private SharedPreferences mPrefs;

    private GridView mGrid;
    private GridAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mPrefs = Util.getPrefs(this);
        setContentView(R.layout.stopwatch_activity);

        mAdapter = new GridAdapter(this);
        mGrid = (GridView) findViewById(R.id.stopwatch_list);
        mGrid.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stopwatch, menu);
        menu.add(R.string.add_stopwatch);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getString(R.string.add_stopwatch).equals(item.getTitle())) {
            addStopwatch();
        }

        return true;
    }

    private void addStopwatch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.stopwatch_name_dialog, null);
        builder.setView(view).setTitle(R.string.stopwatch_name);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = ((TextView) view.findViewById(R.id.stopwatch_name)).getText().toString();
                mAdapter.createStopwatch(true /* start */, name);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
    }
}
