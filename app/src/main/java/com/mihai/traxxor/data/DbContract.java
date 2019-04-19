package com.mihai.traxxor.data;

import android.provider.BaseColumns;

final class DbContract {

    public DbContract() {
    }

//    public static String[] ID_PROJECTION = new String[] {StopwatchesTable._ID};
//    public static String[] NAMES_PROJECTION = new String[] {StopwatchesTable._ID, StopwatchesTable.COL_NAME};

    static abstract class StopwatchesTable implements BaseColumns {
        static final String TABLE_NAME = "stopwatches";
        static final String TEMP_TABLE_NAME = "temp_stopwatches";

        static final String COL_NAME = "name";
    }

    static abstract class StopwatchDaysTable implements BaseColumns {
        static final String TABLE_NAME = "stopwatch_days";
        static final String TEMP_TABLE_NAME = "temp_stopwatch_days";

        static final String COL_STOPWATCH_ID = "stopwatch_id";
        static final String COL_DAY = "day";
        static final String COL_RAW_DURATION = "duration";
        static final String COL_IS_STARTED = "is_started"; // used only by TEMP
        static final String COL_LAST_TIME = "last_time"; // used only by TEMP
    }

    static abstract class ActionsTable implements BaseColumns {
        static final String TABLE_NAME = "actions";
        static final String TEMP_TABLE_NAME = "temp_actions";

        static final String COL_STOPWATCH_ID = "stopwatch_id";
        static final String COL_DAY = "day";
        static final String COL_ACTION_ID = "action_id";
        static final String COL_TIMESTAMP = "timestamp";
        static final String COL_DURATION = "duration";
        static final String COL_TYPE = "type";
    }
}
