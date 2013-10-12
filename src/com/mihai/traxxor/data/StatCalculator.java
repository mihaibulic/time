package com.mihai.traxxor.data;

import java.util.ArrayList;

import com.mihai.traxxor.R;

public class StatCalculator {

    public static String calculateActivePercentString(Stopwatch master, Stopwatch slave) {
        double percent = calculateActivePercentage(master, slave);
        return percent < 0 ? "N/A" : (Math.round(percent * 100) + "%");
    }

    public static double calculateActivePercentage(Stopwatch master, Stopwatch slave) {
        return (double) slave.getDuration() / master.getDuration();
    }

    /**
     * @return array of active percentages (cumulative)
     */
    public static double[][] calculateAverages(Stopwatch master, Stopwatch slave) {
        ArrayList<StopwatchAction> masterActions = master.getStopwatchActions();
        ArrayList<StopwatchAction> slaveActions = slave.getStopwatchActions();

        if (masterActions.size() == 0 || slaveActions.size() == 0) {
            return new double[][] { { 0.0 }, { 0.0 } };
        }

        // The extra slot is for the current result since this may not be reflected
        // in the StopwatchActions if either stopwatch is still active.
        boolean extra = (slave.isStarted() || master.isStarted());
        double[][] data = new double[2][masterActions.size() + slaveActions.size() + (extra ? 1 : 0)];

        ActionStepper masterStepper = new ActionStepper(masterActions);
        ActionStepper slaveStepper = new ActionStepper(slaveActions);
        final int size = masterActions.size() + slaveActions.size();
        for (int d = 0; d < size; d++) {
            long step = getNext(masterStepper, slaveStepper, d == 0);
            data[0][d] = step;
            data[1][d] = getRatio(
                    masterStepper.getDurationAt(step),
                    slaveStepper.getDurationAt(step),
                    (matched == slaveStepper));
        }

        if (extra) {
            data[0][data[0].length - 1] = Stopwatch.getSystemTimeInMs();
            data[1][data[1].length - 1] = calculateActivePercentage(master, slave);
        }

        return data;
    }

    private static ActionStepper matched;
    private static class ActionStepper {
        ArrayList<StopwatchAction> actions;
        int index = 0;

        public ActionStepper(ArrayList<StopwatchAction> actions) {
            this.actions = actions;
        }

        public long getDurationAt(long timestamp) {
            long duration;
            if (index >= actions.size()) {
                duration = actions.get(actions.size() - 1).getDuration();
            } else if (timestamp < actions.get(index).getTimestamp()) {
                duration = 0;
            } else {
                while (index + 1 < actions.size() && actions.get(index + 1).getTimestamp() <= timestamp) {
                    index++;
                }

                duration = actions.get(index).getDuration();
                if (timestamp > actions.get(index).getTimestamp() &&
                        actions.get(index).getType() == R.integer.action_type_start) {
                    duration += timestamp - actions.get(index).getTimestamp();
                }
            }

            return duration;
        }

        public long getTimestamp() {
            return actions.get(index).getTimestamp();
        }

        public boolean hasNext() {
            return index + 1 < actions.size();
        }

        public long getNextTimestamp() {
            return hasNext() ? actions.get(index + 1).getTimestamp() : Long.MAX_VALUE;
        }
    }

    private static long getNext(ActionStepper m, ActionStepper s, boolean first) {
        long next;

        if (first) {
            next = getMin(m, s, first);
        } else if (m.getTimestamp() < s.getTimestamp()) {
            if (m == matched) {
                next = s.getTimestamp();
                matched = s;
            } else {
                // next smallest
                next = getMin(m, s, first);
            }
        } else {
            if (s == matched) {
                next = m.getTimestamp();
                matched = m;
            } else {
                // next smallest
                next = getMin(m, s, first);
            }
        }

        return next;
    }

    private static long getMin(ActionStepper m, ActionStepper s, boolean current) {
        long next;
        long mTime = current ? m.getTimestamp() : m.getNextTimestamp();
        long sTime = current ? s.getTimestamp() : s.getNextTimestamp();

        if (mTime < sTime) {
            next = mTime;
            matched = m;
        } else {
            next = sTime;
            matched = s;
        }

        return next;
    }

    private static double getRatio(double master, double slave, boolean slaveFirst) {
        double data = 0;

        if (master == 0) {
            // if slave duration isn't 0 or it occurred first, 1.0, otherwise 0.0
            data = (slaveFirst || slave != 0) ? 1.0 : 0.0;
        } else {
            data = slave / master;
        }

        return data;
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
