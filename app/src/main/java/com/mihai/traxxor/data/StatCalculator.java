package com.mihai.traxxor.data;

import java.util.ArrayList;

import com.mihai.traxxor.R;

public class StatCalculator {
    private static final int GRAPH_STEPS = 100;
    private static final double GRAPH_STEP_SIZE = 30*60*1000.0;

    public static String calculateActivePercentString(Stopwatch master, Stopwatch slave) {
        double percent = calculateActivePercentage(master, slave);
        return percent < 0 ? "N/A" : (Math.round(percent * 100) + "%");
    }

    private static double calculateActivePercentage(Stopwatch master, Stopwatch slave) {
        return (double) slave.getCurrentDuration() / master.getCurrentDuration();
    }

    /**
     * @return array of active percentages (cumulative)
     */
    public static double[][] calculateAverages(Stopwatch master, Stopwatch slave, boolean isCummulative) {
        int steps = -1;
        double stepSize = -1;

        if (isCummulative) {
            steps = GRAPH_STEPS;
        } else {
            stepSize = GRAPH_STEP_SIZE;
        }

        return calculateAverages(master, slave, isCummulative, steps, stepSize);
    }

    /**
     * @return array of active percentages (cumulative)
     */
    public static double[][] calculateAverages(Stopwatch master, Stopwatch slave, boolean isCummulative, double stepSize) {
        return calculateAverages(master, slave, isCummulative, -1, stepSize);
    }

    /**
     * @return array of active percentages (cumulative)
     */
    public static double[][] calculateAverages(Stopwatch master, Stopwatch slave, boolean isCummulative, int steps) {
        return calculateAverages(master, slave, isCummulative, steps, -1.0);
    }

    /**
     * @return array of active percentages (cumulative)
     */
    private static double[][] calculateAverages(Stopwatch master, Stopwatch slave,
                                                boolean isCummulative, int steps, double stepSize) {
        ArrayList<StopwatchAction> masterActions = master.getStopwatchActions();
        ArrayList<StopwatchAction> slaveActions = slave.getStopwatchActions();

        if (masterActions == null || slaveActions == null || masterActions.size() == 0 || slaveActions.size() == 0) {
            return null;
        }

        // The extra slot is for the current result since this may not be reflected
        // in the StopwatchActions if either stopwatch is still active.
        boolean extra = (slave.isStarted() || master.isStarted());
        long first = Math.min(masterActions.get(0).getTimestamp(),
                slaveActions.get(0).getTimestamp());
        long last = extra ? Stopwatch.getSystemTimeInMs() :
                Math.max(masterActions.get(masterActions.size() - 1).getTimestamp(),
                        slaveActions.get(slaveActions.size() - 1).getTimestamp());
        long step = first;

        if (steps < 0 && stepSize > 0) {
            steps = (int) Math.ceil((last - first) / stepSize);
        } else if (stepSize < 0 && steps > 0) {
            stepSize = (last - first) / steps;
        } else {
            return null;
        }

        double[][] data = new double[2][steps];

        ActionStepper masterStepper = new ActionStepper(masterActions, isCummulative);
        ActionStepper slaveStepper = new ActionStepper(slaveActions, isCummulative);
        final int size = data[0].length;
        for (int d = 0; d < size; d++) {
            step += stepSize;
            data[0][d] = step;
            data[1][d] = getRatio(
                    masterStepper.getDurationAt(step),
                    slaveStepper.getDurationAt(step),
                    (matched == slaveStepper));
        }

        return data;
    }

    private static ActionStepper matched;
    private static class ActionStepper {
        ArrayList<StopwatchAction> actions;
        int index = 0;
        boolean isCumulative;
        long lastDuration = 0;

        ActionStepper(ArrayList<StopwatchAction> actions, boolean isCumulative) {
            this.actions = actions;
            this.isCumulative = isCumulative;
        }

        long getDurationAt(long timestamp) {
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

            if (!isCumulative) {
                long original = duration;
                duration -= lastDuration;
                lastDuration = original;
            }

            return duration;
        }

        long getTimestamp() {
            return actions.get(index).getTimestamp();
        }

        boolean hasNext() {
            return index + 1 < actions.size();
        }

        long getNextTimestamp() {
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
        double data;

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
