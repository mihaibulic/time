package com.mihai.traxxor.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;
import com.mihai.traxxor.R;
import com.mihai.traxxor.util.Util;

public class GraphActivity extends Activity {
    // if graph spans more than this many ms, don't show seconds on X-axis
    private static final int SECONDS_CUTOFF_MS = 5 * 60 * 1000;

    class Formatter implements CustomLabelFormatter {
        Calendar cl = Calendar.getInstance();
        boolean includeSeconds;

        public Formatter(boolean includeSeconds) {
            super();
            this.includeSeconds = includeSeconds;
        }

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
            int h = cl.get(Calendar.HOUR_OF_DAY);
            int m = cl.get(Calendar.MINUTE);
            int s = cl.get(Calendar.SECOND);
            return h + ":" +
                    (m < 10 ? "0" : "") + m +
                    (includeSeconds ? (":" + (s < 10 ? "0" : "") + s) : "");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String title = getIntent().getStringExtra(String.valueOf(R.integer.extra_graph_title));
        ArrayList<String> ids = getIntent().getStringArrayListExtra(String.valueOf(R.integer.extra_graph_series_ids));
        ArrayList<String> names = getIntent().getStringArrayListExtra(String.valueOf(R.integer.extra_graph_series_names));

        if (ids == null || names == null || title == null || ids.size() == 0 ||
                names.size() == 0 || ids.size() != names.size()) {
            return;
        }

        boolean includeSeconds = true;
        GraphView graphView = new LineGraphView(this, title);
        for (int i = 0; i < ids.size(); i++) {
            double[] rawXData = getIntent().getDoubleArrayExtra(Util.getGraphXExtra(ids.get(i)));
            double[] rawYData = getIntent().getDoubleArrayExtra(Util.getGraphYExtra(ids.get(i)));
            if (rawXData == null || rawYData == null || title == null ||
                    rawXData.length == 0 || rawYData.length == 0 || rawXData.length != rawYData.length) {
                return;
            }

            includeSeconds &= (rawXData[rawXData.length - 1] - rawXData[0]) < SECONDS_CUTOFF_MS;

            GraphViewData[] data = new GraphViewData[rawXData.length];
            for (int d = 0; d < rawXData.length; d++) {
                data[d] = new GraphViewData(rawXData[d], rawYData[d]);
            }

            // random color
            int color = Color.rgb(
                    (int) Math.pow(7 * i, 3) % 255,
                    (int) Math.pow(5 * i, 5) % 255,
                    (int) Math.pow(3 * i, 7) % 255);
            GraphViewSeriesStyle style = new GraphViewSeriesStyle(color, 2);
            GraphViewSeries series = new GraphViewSeries(names.get(i), style, data);
            graphView.addSeries(series);
        }

        setContentView(R.layout.graph);
        LinearLayout v = (LinearLayout) findViewById(R.id.graph_holder);
        v.addView(graphView);
        graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
        graphView.setManualYAxisBounds(1, 0);
        graphView.setShowLegend(ids.size() > 1);
        graphView.setLegendWidth(240);
        graphView.setCustomLabelFormatter(new Formatter(includeSeconds));
    }
}
