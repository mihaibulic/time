package com.mihai.traxxor.ui;

import java.util.Calendar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.mihai.traxxor.R;

public class GraphActivity extends Activity {

    class Formatter implements CustomLabelFormatter {
        Calendar cl = Calendar.getInstance();

        public String formatLabel(double value, boolean isX) {
            String retValue = "";
            if (isX) {
                retValue = getTimeString(cl, (long) value);
            } else {
                retValue = (int) (value * 100) + "%";
            }
            return retValue;
        }

        private String getTimeString(Calendar cl, long milliseconds) {
            cl.setTimeInMillis(milliseconds);
            return "" + cl.get(Calendar.HOUR_OF_DAY) + ":" +
                    cl.get(Calendar.MINUTE) + ":" + cl.get(Calendar.SECOND);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        LinearLayout v = (LinearLayout) findViewById(R.id.graph_holder);

        // ADD X VALUES TO INTENT
        double[] rawXData = getIntent().getDoubleArrayExtra(String.valueOf(R.integer.graph_raw_x_data));
        double[] rawYData = getIntent().getDoubleArrayExtra(String.valueOf(R.integer.graph_raw_y_data));
        GraphViewData[] data = new GraphViewData[rawXData.length];
        for (int i = 0; i < rawXData.length; i++) {
            data[i] = new GraphViewData(rawXData[i], rawYData[i]);
            android.util.Log.v("bulic", "plotting: " + rawXData[i] + ", " + rawYData[i]);
        }

        GraphViewSeries series = new GraphViewSeries(data);

        GraphView graphView = new LineGraphView(this, "GraphViewDemo");
        graphView.setTitle(getIntent().getStringExtra(String.valueOf(R.integer.graph_title)));
        graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
        graphView.setManualYAxisBounds(1, 0);
        graphView.setCustomLabelFormatter(new Formatter());
        graphView.addSeries(series);

        v.addView(graphView);
    }
}
