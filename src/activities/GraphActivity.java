package activities;

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
        double[] rawXData = getIntent().getDoubleArrayExtra(String.valueOf(R.integer.graph_raw_x_data));
        double[] rawYData = getIntent().getDoubleArrayExtra(String.valueOf(R.integer.graph_raw_y_data));
        final String title = getIntent().getStringExtra(String.valueOf(R.integer.graph_title));

        if (rawXData == null || rawYData == null || title == null ||
                rawXData.length == 0 || rawYData.length == 0 || rawXData.length != rawYData.length) {
            return;
        }

        setContentView(R.layout.graph);
        boolean includeSeconds = (rawXData[rawXData.length - 1] - rawXData[0]) < SECONDS_CUTOFF_MS;

        LinearLayout v = (LinearLayout) findViewById(R.id.graph_holder);
        GraphViewData[] data = new GraphViewData[rawXData.length];
        for (int i = 0; i < rawXData.length; i++) {
            data[i] = new GraphViewData(rawXData[i], rawYData[i]);
        }

        GraphViewSeries series = new GraphViewSeries(data);
        GraphView graphView = new LineGraphView(this, title);
        graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
        graphView.setManualYAxisBounds(1, 0);
        graphView.setCustomLabelFormatter(new Formatter(includeSeconds));
        graphView.addSeries(series);

        v.addView(graphView);
    }
}
