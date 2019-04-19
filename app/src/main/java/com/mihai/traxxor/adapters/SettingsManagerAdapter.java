package com.mihai.traxxor.adapters;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mihai.traxxor.R;

public class SettingsManagerAdapter extends BaseAdapter implements OnClickListener {
    private Activity mActivity;
    private ArrayList<String> mStopwatchNames = new ArrayList<>();
    private ArrayList<Integer> mStopwatchIds = new ArrayList<>();
    private ArrayList<Integer> mToDelete = new ArrayList<>();

    private int mTempId = 0;

    public SettingsManagerAdapter(Activity act) {
        mActivity = act;
    }

    public int getCount() {
        return mStopwatchNames.size();
    }

    public String getItem(int position) {
        return mStopwatchNames.get(position);
    }

    public long getItemId(int position) {
        return mStopwatchIds.get(position);
    }

    public synchronized boolean addStopwatches(String[] names, int[] ids) {
        boolean success = false;
        if (names != null && ids != null && names.length == ids.length) {
            for (int i = 0; i < names.length; i++) {
                mStopwatchNames.add(names[i]);
                mStopwatchIds.add(ids[i]);
            }
            success = true;
        }

        notifyDataSetChanged();

        return success;
    }

    public synchronized void addStopwatch(CharSequence name) {
        mStopwatchNames.add(formatName(name));
        mStopwatchIds.add(--mTempId);

        notifyDataSetChanged();
    }

    public synchronized void removeStopwatch(int id) {
        for (int i = mStopwatchIds.size() - 1; i >=0; i--) {
            if (mStopwatchIds.get(i) == id) {
                mStopwatchIds.remove(i);
                mStopwatchNames.remove(i);
                if (id >= 0) {
                    mToDelete.add(id);
                }
            }
        }
    }

    public void onClick(final View v) {
        Object id = v.getTag();
        if (id instanceof Integer) {
            removeStopwatch(/* id= */ (Integer) id);
            notifyDataSetChanged();
        }
    }

    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        String name = mStopwatchNames.get(position);
        Integer id = mStopwatchIds.get(position);
        if (name != null && id != null) {
            if (convertView == null || convertView.getId() != R.id.stopwatch_for_dialog) {
                convertView = LayoutInflater.from(mActivity).inflate(R.layout.stopwatch_for_manager, null);
            }

            ((TextView) convertView.findViewById(R.id.name)).setText(name);
            convertView.setOnClickListener(this);
            convertView.setTag(id);
        }
        return convertView;
    }

    public ArrayList<Integer> getDeleted() {
        return mToDelete;
    }

    public boolean isValidName(CharSequence name) {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(name.toString().trim());
    }

    private String formatName(CharSequence name) {
        String in = name.toString().trim();
        String[] split = in.split(" ");

        StringBuilder retValue = new StringBuilder();
        for (String s : split) {
            retValue.append(s.substring(0, 1).toUpperCase(Locale.US))
                    .append(s.substring(1))
                    .append(" ");
        }

        return retValue.toString().trim();
    }
}
