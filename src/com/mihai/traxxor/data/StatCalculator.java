package com.mihai.traxxor.data;

import java.util.LinkedList;

import com.mihai.traxxor.R;

public class StatCalculator {

    public static String calculateActivePercentString(Stopwatch master, Stopwatch slave) {
        double percent = calculateActivePercentage(master, slave);
        return percent < 0 ? "N/A" : ((int) (percent * 100) + "%");
    }

    public static double calculateActivePercentage(Stopwatch master, Stopwatch slave) {
        return (double) slave.getDuration() / master.getDuration();
    }

    /**
     * @return array of active percentages (cumulative)
     */
    public static double[][] calculateAverages(Stopwatch master, Stopwatch slave) {
        LinkedList<StopwatchAction> masterStopwatchActions = master.getStopwatchActions();
        LinkedList<StopwatchAction> slaveStopwatchActions = slave.getStopwatchActions();

        if (masterStopwatchActions.size() == 0 || slaveStopwatchActions.size() == 0) {
            return null;
        }

        int masterIndex = 0;
        int slaveIndex = 0;
        int dataIndex = 0;

        // The extra slot is for the current result since this may not be reflected in the StopwatchActions
        // if either stopwatch is still active.
        double[][] data = new double[2][masterStopwatchActions.size() + slaveStopwatchActions.size()];
        while (masterIndex < masterStopwatchActions.size() && slaveIndex < slaveStopwatchActions.size()) {
            StopwatchAction masterStopwatchAction = masterStopwatchActions.get(masterIndex);
            StopwatchAction slaveStopwatchAction = slaveStopwatchActions.get(slaveIndex);
            long step = Math.min(masterStopwatchAction.mTimestamp, slaveStopwatchAction.mTimestamp);

            // Since one of the stopwatches is ahead of the other, we must subtract any duration
            // that may have been accumulated beyond the step.
            // If the StopwatchAction was a start, then the stopwatch was stopped before and did not accumulate
            // duration so we don't need to compensate.
            long masterTemp = masterStopwatchAction.mDuration -
                    (masterStopwatchAction.mType == R.integer.action_type_stop ? masterStopwatchAction.mTimestamp - step : 0);
            long slaveTemp = slaveStopwatchAction.mDuration -
                    (slaveStopwatchAction.mType == R.integer.action_type_stop ? slaveStopwatchAction.mTimestamp - step : 0);


            data[0][dataIndex] = step;
            data[1][dataIndex++] = (masterTemp == 0 ? 1 : (double) slaveTemp / masterTemp);

            if (masterStopwatchAction.mTimestamp < slaveStopwatchAction.mTimestamp &&
                    (masterIndex + 1) < masterStopwatchActions.size()) {
                masterIndex++;
            } else {
                slaveIndex++;
            }
        }
        data[0][data[0].length - 1] = Stopwatch.getSystemTimeInMs();
        data[1][data[1].length - 1] = calculateActivePercentage(master, slave);

        return data;
    }

    /**
     * @param winddowSizeMs - size of the window in milliseconds
     * @param master
     * @param slave
     * @return array of active percentages (not cumulative, uses window size)
     */
    public static double[] calculateAverages(final int winddowSizeMs, Stopwatch master, Stopwatch slave) {

        return new double[] {};
    }

    //    public double getActivePercentage() {
    //        long curTime = getSystemTimeInMs();
    //        long duration = getDuration();
    //        if (DEBUG) {
    //            android.util.Log.d(TAG, "Time active calculation: " +
    //                    duration + " / (" + curTime + " - " + mCreationTime + ")");
    //        }
    //        return ((double) duration) / (curTime - mCreationTime);
    //    }
    //
    //    public double[] getAverages(final int windowSizeInMs) {
    //        int length = (int) Math.ceil((double) getTimeSinceCreation() / windowSizeInMs);
    //        double[] times = new double[length];
    //
    //        if (DEBUG) {
    //            android.util.Log.d(TAG, "Slots needed: " + length);
    //            android.util.Log.d(TAG, "%Active | activeWindowLength | windowLength | overflow:");
    //        }
    //
    //        long window = 0;
    //        long activeWindow = 0;
    //        boolean running = false;
    //        int cur = 0;
    //        for (int time : mStartStopTimes) {
    //            window += time;
    //            if (running) {
    //                activeWindow += time;
    //            }
    //
    //            long overflow = window - windowSizeInMs;
    //            if (overflow >= 0) {
    //                window -= overflow;
    //                if (running) {
    //                    activeWindow -= overflow;
    //                }
    //
    //                times[cur++] = (double) activeWindow / window;
    //                if (DEBUG) {
    //                    android.util.Log.d(TAG,
    //                            times[cur - 1] + " | " + activeWindow + " | " + window + " | " + overflow);
    //                }
    //
    //                window = overflow;
    //                activeWindow = (running ? overflow : 0);
    //            }
    //
    //            running = !running;
    //        }
    //
    //        if (window > 0) {
    //            times[cur++] = (double) activeWindow / window;
    //            if (DEBUG) {
    //                android.util.Log.d(TAG, times[cur - 1] + " | " + activeWindow + " | " + window);
    //            }
    //        }
    //
    //        return times;
    //    }
}
