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

public class ManagerAdapter extends BaseAdapter implements OnClickListener {
    private static Activity sActivity;
    private ArrayList<String> mStopwatchNames = new ArrayList<String>();
    private ArrayList<Integer> mStopwatchIds = new ArrayList<Integer>();
    private ArrayList<Integer> mToDelete = new ArrayList<Integer>();

    private int mTempId = 0;

    public ManagerAdapter(Activity act) {
        sActivity = act;
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
        for (int i = 0; i < mStopwatchIds.size(); i++) {
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
        if (v.getTag() instanceof Integer) {
            removeStopwatch((Integer) v.getTag());
            notifyDataSetChanged();
        }
    }

    public synchronized View getView(int position, View convertView, ViewGroup parent) {
        String name = mStopwatchNames.get(position);
        Integer id = mStopwatchIds.get(position);
        if (name != null && id != null) {
            if (convertView == null || convertView.getId() != R.id.stopwatch_for_dialog) {
                convertView = LayoutInflater.from(sActivity).inflate(R.layout.stopwatch_for_manager, null);
            }

            ((TextView) convertView.findViewById(R.id.name)).setText(name);
            ImageView deleteView = (ImageView) convertView.findViewById(R.id.delete);
            deleteView.setTag(R.integer.tag_type, R.integer.type_delete);
            deleteView.setOnClickListener(this);
            deleteView.setTag(id);
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

        String retValue = "";
        for (String s : split) {
            retValue += s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1) + " ";
        }

        return retValue.trim();
    }
}
