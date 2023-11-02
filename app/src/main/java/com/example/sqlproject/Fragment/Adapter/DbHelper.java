package com.example.sqlproject.Fragment.Adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sqlproject.Fragment.modelSql.EventModel;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DEMO_db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DESCRIPTION =  "description";


    //for compeletedTable
    private static final String TABLE_NAME1 = "myCptReminder";
    public static final String ID_COL_CPT = "id";
    public static final String DATE_COL_CPT = "date";
    public static final String TIME_COL_CPT = "time";
    public static final String DESCRIPTION_COL_CPT = "description";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT)";
        db.execSQL(CREATE_EVENTS_TABLE);

        //For compelete table

        String query1 = "CREATE TABLE " + TABLE_NAME1 + " ("
                + ID_COL_CPT + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE_COL_CPT + " TEXT, "
                + TIME_COL_CPT + " TEXT, "
                + DESCRIPTION_COL_CPT + " TEXT)";

        db.execSQL(query1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public long insertEvent(String date, String time, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_DESCRIPTION, description);
        long rowId = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return rowId;
    }
    public void addNewCptReminder(String date, String time, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DATE_COL_CPT, date);
        values.put(TIME_COL_CPT, time);
        values.put(DESCRIPTION_COL_CPT, desc);

        db.insert(TABLE_NAME1, null, values);
        db.close();
    }

    public Cursor getAllEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EVENTS, null, null, null, null, null, null);
    }




    public int deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = COLUMN_ID + "=?";
        String[] whereArgs = {String.valueOf(eventId)};

        int rowsAffected = db.delete(TABLE_EVENTS, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }
    public int updateEvent(int eventId, String date, String time, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_DESCRIPTION, description);

        String whereClause = COLUMN_ID + "=?";
        String[] whereArgs = {String.valueOf(eventId)};

        int rowsAffected = db.update(TABLE_EVENTS, values, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }
    public int updateData(int id, String date, String time, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        return db.update(TABLE_EVENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});


    }

    public ArrayList<EventModel> getAllCompletedData() {
        ArrayList<EventModel> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME1, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(ID_COL_CPT));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(DATE_COL_CPT));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(TIME_COL_CPT));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(DESCRIPTION_COL_CPT));

                EventModel data = new EventModel(id, date, time, description);
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dataList;
    }

    public int deleteCompletedEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = ID_COL_CPT + "=?";
        String[] whereArgs = {String.valueOf(eventId)};

        int rowsAffected = db.delete(TABLE_NAME1, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }



}
