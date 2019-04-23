package com.mihai.traxxor.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import android.util.SparseArray;

import com.mihai.traxxor.activities.MainActivity;
import com.mihai.traxxor.data.DbContract.ActionsTable;
import com.mihai.traxxor.data.DbContract.StopwatchDaysTable;
import com.mihai.traxxor.data.DbContract.StopwatchesTable;
import com.mihai.traxxor.util.Util;


public class StopwatchDataProvider extends SQLiteOpenHelper {

    private static final String DB_NAME = "Traxxor.db";
    private static final int DB_VERSION = 1; // IF INCREMENTING THIS, IMPLEMENT UPGRADE!
    public static final int TABLE_TYPE_TEMP = 0;
    public static final int TABLE_TYPE_PERM = 1;

    private static final String CREATE = " CREATE TABLE ";
    private static final String CLEAR = " DELETE FROM ";
    private static final String OPEN = " ( ";
    private static final String CLOSE = " ) ";
    private static final String END = " ; ";
    private static final String COMMA = " , ";
    private static final String TEXT = " TEXT ";
    private static final String INTEGER = " INTEGER ";
    private static final String FOREIGN_KEY = " FOREIGN KEY ";
    private static final String REF = " REFERENCES ";
    private static final String PRIMARY_KEY = " PRIMARY KEY ";
    private static final String NOT_NULL = " NOT NULL ";

    private static String getCreateStopwatchesTable(int type) {
        String tableName = (type == TABLE_TYPE_TEMP) ? StopwatchesTable.TEMP_TABLE_NAME : StopwatchesTable.TABLE_NAME;

        return (CREATE + tableName
            + OPEN
                    + StopwatchesTable._ID + INTEGER + PRIMARY_KEY + COMMA
                    + StopwatchesTable.COL_NAME + TEXT + NOT_NULL
            + CLOSE + END);
    }

    private static String getCreateStopwatchDaysTable(int type) {
        String tableName = (type == TABLE_TYPE_TEMP) ? StopwatchDaysTable.TEMP_TABLE_NAME : StopwatchDaysTable.TABLE_NAME;

        return (CREATE + tableName
            + OPEN
                    + StopwatchDaysTable.COL_STOPWATCH_ID + INTEGER + COMMA
                    + StopwatchDaysTable.COL_DAY + INTEGER + NOT_NULL + COMMA
                    + StopwatchDaysTable.COL_RAW_DURATION + INTEGER + COMMA
                    + StopwatchDaysTable.COL_IS_STARTED + INTEGER + COMMA
                    + StopwatchDaysTable.COL_LAST_TIME + INTEGER + COMMA
                    + FOREIGN_KEY
                        + OPEN
                            + StopwatchDaysTable.COL_STOPWATCH_ID
                        + CLOSE + REF + StopwatchesTable.TEMP_TABLE_NAME
                        + OPEN
                            + StopwatchesTable._ID
                        + CLOSE + COMMA
                    + PRIMARY_KEY
                        + OPEN
                            + StopwatchDaysTable.COL_STOPWATCH_ID + COMMA
                            + StopwatchDaysTable.COL_DAY
                        + CLOSE
            + CLOSE + END);
    }

    private static String getCreateActionsTable(int type) {
        String tableName = (type == TABLE_TYPE_TEMP) ? ActionsTable.TEMP_TABLE_NAME : ActionsTable.TABLE_NAME;

        return (CREATE + tableName
            + OPEN
                    + ActionsTable.COL_STOPWATCH_ID + INTEGER + NOT_NULL + COMMA
                    + ActionsTable.COL_DAY + INTEGER + NOT_NULL + COMMA
                    + ActionsTable.COL_ACTION_ID + INTEGER + NOT_NULL + COMMA
                    + ActionsTable.COL_TIMESTAMP + INTEGER + NOT_NULL + COMMA
                    + ActionsTable.COL_DURATION + INTEGER + COMMA
                    + ActionsTable.COL_TYPE + INTEGER + NOT_NULL + COMMA
                    + FOREIGN_KEY
                        + OPEN
                            + ActionsTable.COL_STOPWATCH_ID + COMMA
                            + ActionsTable.COL_DAY
                        + CLOSE + REF + StopwatchDaysTable.TEMP_TABLE_NAME
                        + OPEN
                            + StopwatchDaysTable.COL_STOPWATCH_ID + COMMA
                            + StopwatchDaysTable.COL_DAY
                        + CLOSE + COMMA
                    + PRIMARY_KEY + OPEN
                        + ActionsTable.COL_STOPWATCH_ID + COMMA
                        + ActionsTable.COL_DAY + COMMA
                        + ActionsTable.COL_ACTION_ID
                    + CLOSE
            + CLOSE + END);
    }

    public StopwatchDataProvider(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db, TABLE_TYPE_TEMP);
        createTables(db, TABLE_TYPE_PERM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearTempTables(db);
    }

    private void createTables(SQLiteDatabase db, int type) {
        db.execSQL(getCreateStopwatchesTable(type));
        db.execSQL(getCreateStopwatchDaysTable(type));
        db.execSQL(getCreateActionsTable(type));
    }

    private void clearTempTables(SQLiteDatabase db) {
        db.execSQL(CLEAR + StopwatchesTable.TEMP_TABLE_NAME + END);
        db.execSQL(CLEAR + StopwatchDaysTable.TEMP_TABLE_NAME + END);
        db.execSQL(CLEAR + ActionsTable.TEMP_TABLE_NAME + END);
    }

    private void writeToTable(SQLiteDatabase db, Stopwatch watch, int writeType) {
        final String today = Util.getToday();
        final int id = watch.getId();
        final boolean temp = (writeType == TABLE_TYPE_TEMP);
        final String stopwatchTable = (temp ? StopwatchesTable.TEMP_TABLE_NAME : StopwatchesTable.TABLE_NAME);
        final String stopwatchDaysTable = (temp ? StopwatchDaysTable.TEMP_TABLE_NAME : StopwatchDaysTable.TABLE_NAME);
        final String actionsTable = (temp ? ActionsTable.TEMP_TABLE_NAME : ActionsTable.TABLE_NAME);

        ContentValues values = new ContentValues();
        values.put(StopwatchesTable._ID, id);
        values.put(StopwatchesTable.COL_NAME, watch.getName());
        db.insert(stopwatchTable, null, values);

        values = new ContentValues();
        values.put(StopwatchDaysTable.COL_STOPWATCH_ID, id);
        values.put(StopwatchDaysTable.COL_DAY, today);
        values.put(StopwatchDaysTable.COL_RAW_DURATION, watch.getRawDuration());
        values.put(StopwatchDaysTable.COL_IS_STARTED, watch.isStarted());
        values.put(StopwatchDaysTable.COL_LAST_TIME, watch.getLastTime());
        db.insert(stopwatchDaysTable, null, values);

        ArrayList<StopwatchAction> actions = watch.getStopwatchActions();
        for (int i = 0; i < actions.size(); i++) {
            StopwatchAction action = actions.get(i);
            values = new ContentValues();
            values.put(ActionsTable.COL_STOPWATCH_ID, id);
            values.put(ActionsTable.COL_DAY, today);
            values.put(ActionsTable.COL_ACTION_ID, i);
            values.put(ActionsTable.COL_DURATION, action.getDuration());
            values.put(ActionsTable.COL_TIMESTAMP, action.getTimestamp());
            values.put(ActionsTable.COL_TYPE, action.getType());
            db.insert(actionsTable, null, values);
        }
    }

    public void writeToTable(ArrayList<Stopwatch> watches, Stopwatch masterWatch, int writeType) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            clearTempTables(db);

            writeToTable(db, masterWatch, writeType);
            for (Stopwatch watch : watches) {
                writeToTable(db, watch, writeType);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Pair<Stopwatch, ArrayList<Stopwatch>> readFromTable(String day, int readType) {
        SparseArray<Stopwatch> watchLookup = new SparseArray<>();
        Stopwatch masterWatch = null;
        ArrayList<Stopwatch> watches = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        final boolean temp = (readType == TABLE_TYPE_TEMP);
        final String stopwatchTable = (temp ? StopwatchesTable.TEMP_TABLE_NAME : StopwatchesTable.TABLE_NAME);
        final String stopwatchDaysTable = (temp ? StopwatchDaysTable.TEMP_TABLE_NAME : StopwatchDaysTable.TABLE_NAME);
        final String actionsTable = (temp ? ActionsTable.TEMP_TABLE_NAME : ActionsTable.TABLE_NAME);

        // Query for stopwatches
        String sql = String.format(" SELECT * FROM %s s INNER JOIN %s d ON s.%s = d.%s WHERE d.%s = ?",
                stopwatchTable, stopwatchDaysTable,
                StopwatchesTable._ID, StopwatchDaysTable.COL_STOPWATCH_ID,
                StopwatchDaysTable.COL_DAY);
        String[] selectionArgs = new String[] {day};
        Cursor c = db.rawQuery(sql, selectionArgs);

        int idIndex = c.getColumnIndex(StopwatchesTable._ID);
        int nameIndex = c.getColumnIndex(StopwatchesTable.COL_NAME);
        int rawDurationIndex = c.getColumnIndex(StopwatchDaysTable.COL_RAW_DURATION);
        int isStartedIndex = c.getColumnIndex(StopwatchDaysTable.COL_IS_STARTED);
        int lastTimeIndex = c.getColumnIndex(StopwatchDaysTable.COL_LAST_TIME);
        while(c.moveToNext()) {
            int id = c.getInt(idIndex);
            String name = c.getString(nameIndex);
            long rawDuration = c.getLong(rawDurationIndex);
            boolean isStarted = c.getInt(isStartedIndex) == 1;
            long lastTime = c.getLong(lastTimeIndex);

            Stopwatch watch = new Stopwatch(
                    id,
                    name,
                    new ArrayList<StopwatchAction>(),
                    isStarted,
                    lastTime,
                    rawDuration);

            if (id == MainActivity.MASTER_ID) {
                masterWatch = watch;
            } else {
                watches.add(watch);
            }
            watchLookup.put(watch.getId(), watch);
        }
        c.close();

        // Query for actions
        sql = String.format(" SELECT * FROM %s WHERE %s = ? ORDER BY %s ASC ",
                actionsTable, ActionsTable.COL_DAY, ActionsTable.COL_ACTION_ID);
        selectionArgs = new String[] {day};
        c = db.rawQuery(sql, selectionArgs);

        int watchIdIndex = c.getColumnIndex(ActionsTable.COL_STOPWATCH_ID);
        int timestampIndex = c.getColumnIndex(ActionsTable.COL_TIMESTAMP);
        rawDurationIndex = c.getColumnIndex(ActionsTable.COL_DURATION);
        int typeIndex = c.getColumnIndex(ActionsTable.COL_TYPE);
        while(c.moveToNext()) {
            int watchId = c.getInt(watchIdIndex);
            long timestamp = c.getLong(timestampIndex);
            long duration = c.getLong(rawDurationIndex);
            int type = c.getInt(typeIndex);

            Stopwatch watch = watchLookup.get(watchId);
            if (watch != null) {
                watch.addAction(timestamp, duration, type);
            }
        }
        c.close();
        db.close();

        return new Pair<>(masterWatch, watches);
    }

    public int getNextStopwatchId() {
        int maxId = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT MAX(" + StopwatchesTable._ID + ") AS max_id FROM ";

        // Find max ID in perm table
        Cursor c = db.rawQuery(query + StopwatchesTable.TABLE_NAME, null);
        if (c.moveToFirst() && c.getColumnCount() == 1) {
            maxId = Math.max(maxId, c.getInt(0));
        }
        c.close();

        // Find max ID in temp table
        c = db.rawQuery(query + StopwatchesTable.TEMP_TABLE_NAME, null);
        if (c.moveToFirst() && c.getColumnCount() == 1) {
            maxId = Math.max(maxId, c.getInt(0));
        }
        c.close();

        return maxId+1;
    }
}