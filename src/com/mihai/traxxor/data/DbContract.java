package com.mihai.traxxor.data;

import android.provider.BaseColumns;

public final class DbContract {

    public DbContract() {
    }

    public static String[] ID_PROJECTION = new String[] {StopwatchesTable._ID};
    public static String[] NAMES_PROJECTION = new String[] {StopwatchesTable._ID, StopwatchesTable.COL_NAME};

    public static abstract class StopwatchesTable implements BaseColumns {
        public static final String TABLE_NAME = "stopwatches";
        public static final String TEMP_TABLE_NAME = "temp_stopwatches";

        public static final String COL_NAME = "name";
        public static final String COL_IS_WORK = "is_work";
    }

    public static abstract class StopwatchDaysTable implements BaseColumns {
        public static final String TABLE_NAME = "stopwatch_days";
        public static final String TEMP_TABLE_NAME = "temp_stopwatch_days";

        public static final String COL_STOPWATCH_ID = "stopwatch_id";
        public static final String COL_DAY = "day";
        public static final String COL_RAW_DURATION = "duration";
        public static final String COL_IS_STARTED = "is_started"; // used only by TEMP
        public static final String COL_LAST_TIME = "last_time"; // used only by TEMP
    }

    public static abstract class ActionsTable implements BaseColumns {
        public static final String TABLE_NAME = "actions";
        public static final String TEMP_TABLE_NAME = "temp_actions";

        public static final String COL_STOPWATCH_ID = "stopwatch_id";
        public static final String COL_DAY = "day";
        public static final String COL_ACTION_ID = "action_id";
        public static final String COL_TIMESTAMP = "timestamp";
        public static final String COL_DURATION = "duration";
        public static final String COL_TYPE = "type";
    }
}
