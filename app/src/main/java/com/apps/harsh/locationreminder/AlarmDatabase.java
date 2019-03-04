package com.apps.harsh.locationreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class AlarmDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "AlarmDatabase.DB";
    public static final String TABLE_NAME = "GeoAlarm";
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_RING_NAME = "RING_NAME";
    public static final String COL_VIB = "VIB";
    public static final String COL_LONG = "LONG";
    public static final String COL_LATI = "LATI";
    public static final String COL_RAD = "RAD";
    public static final String COL_RING_URI = "RING_URI";
    public static final String COL_ONOFF = "ONOFF";
    public static final String COL_MESSAGE = "MESSAGE";

    public AlarmDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "//0
                + COL_NAME + " TEXT, "//1
                + COL_RING_NAME + " TEXT, "//2
                + COL_VIB + " BOOL, "//3
                + COL_LATI + " DOUBLE, "//4
                + COL_LONG + " DOUBLE, "//5
                + COL_RAD + " INTEGER, "//6
                + COL_RING_URI + " TEXT, "//7
                + COL_ONOFF + " BOOL, "//8
                + COL_MESSAGE + " TEXT"//9
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(GeoAlarm geoAlarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, geoAlarm.getName());
        contentValues.put(COL_RING_NAME, geoAlarm.getRingtoneName());
        contentValues.put(COL_VIB, geoAlarm.getVibration());
        LocationCoordiante locationCoordiante = geoAlarm.getmLocationCoordinate();
        contentValues.put(COL_LATI, locationCoordiante.getLatitude());
        contentValues.put(COL_LONG, locationCoordiante.getLongitude());
        contentValues.put(COL_RAD, geoAlarm.getRadius());
        contentValues.put(COL_RING_URI, geoAlarm.getRingtoneUri());
        contentValues.put(COL_ONOFF, geoAlarm.getStatus());
        contentValues.put(COL_MESSAGE, geoAlarm.getMessage());
        long result = db.insert(TABLE_NAME, null, contentValues);
        Log.d("add data", "" + result);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<GeoAlarm> getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + ";", null);
        if (cursor.getCount() == 0) {
            //show message
            return null;
        }

        ArrayList<GeoAlarm> alarms = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    GeoAlarm geoAlarm = new GeoAlarm();
                    geoAlarm.setmId(cursor.getInt(0));
                    geoAlarm.setName(cursor.getString(1));
                    geoAlarm.setVibration(Integer.parseInt(cursor.getString(3)) == 1);
                    geoAlarm.setLocationCoordinate(new LocationCoordiante(cursor.getDouble(4), cursor.getDouble(5)));
                    geoAlarm.setRadius(cursor.getInt(6));
                    geoAlarm.setRingtone(cursor.getString(2), cursor.getString(7));
                    geoAlarm.setStatus(Integer.parseInt(cursor.getString(8)) == 1);
                    geoAlarm.setMessage(cursor.getString(9));
                    alarms.add(geoAlarm);
                }
                while (cursor.moveToNext());
            }
        }
        // show all data
        return alarms;
    }

    public boolean updateData(GeoAlarm geoAlarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, geoAlarm.getName());
        contentValues.put(COL_RING_NAME, geoAlarm.getRingtoneName());
        contentValues.put(COL_VIB, geoAlarm.getVibration());
        LocationCoordiante locationCoordiante = geoAlarm.getmLocationCoordinate();
        contentValues.put(COL_LATI, locationCoordiante.getLatitude());
        contentValues.put(COL_LONG, locationCoordiante.getLongitude());
        contentValues.put(COL_RAD, geoAlarm.getRadius());
        contentValues.put(COL_RING_URI, geoAlarm.getRingtoneUri());
        contentValues.put(COL_ONOFF, geoAlarm.getStatus());
        contentValues.put(COL_MESSAGE, geoAlarm.getMessage());
        long result = db.update(TABLE_NAME, contentValues, "id = ?", new String[]{"" + geoAlarm.getmId()});
        if (result != -1) {
            return true;
        }
        return false;
    }

    public Integer delete(int Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{"" + Id});
    }

    public int getId() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY ID DESC LIMIT 1;", null);
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}

