package com.mihai.traxxor.sandbox;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.mihai.traxxor.R;

public class GraphTestActivity extends Activity {
    public static final String EXTRA_GRAPH_DATA = "graph_data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        LinearLayout v = (LinearLayout) findViewById(R.id.graph_holder);

        double[] rawData = getIntent().getDoubleArrayExtra(EXTRA_GRAPH_DATA);
        GraphViewData[] data = new GraphViewData[rawData.length];
        for (int i = 0; i < rawData.length; i++) {
            data[i] = new GraphViewData(1, rawData[i]);
        }

        GraphViewSeries exampleSeries = new GraphViewSeries(data);

        GraphView graphView = new LineGraphView(this, "GraphViewDemo");
        graphView.addSeries(exampleSeries); // data

        v.addView(graphView);

    }
}
