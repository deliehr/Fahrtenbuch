package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Database {
    public static abstract class T_FAHRT implements BaseColumns {
        public static final String TABLE_NAME = "T_FAHRT";

        public static final String COL_START_DATE = "start_date";
        public static final String COL_START_TIME = "start_time";
        public static final String COL_START_TOWN = "start_town";
        public static final String COL_START_ADDRESS = "start_address";
        public static final String COL_START_KMSTAND = "start_kmstand";

        public static final String COL_END_DATE = "end_date";
        public static final String COL_END_TIME = "end_time";
        public static final String COL_END_TOWN = "end_town";
        public static final String COL_END_ADDRESS = "end_address";
        public static final String COL_END_KMSTAND = "end_kmstand";

        public static final String COL_LATITUDE = "latitude";
        public static final String COL_LONGITUDE = "longitude";
        public static final String COL_ORTSZUSATZ = "ortszusatz";
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + T_FAHRT.TABLE_NAME + " (" +
                    T_FAHRT._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    T_FAHRT.COL_START_DATE + " TEXT, " +
                    T_FAHRT.COL_START_TIME + " TEXT, " +
                    T_FAHRT.COL_START_TOWN + " TEXT, " +
                    T_FAHRT.COL_START_ADDRESS + " TEXT, " +
                    T_FAHRT.COL_START_KMSTAND + " TEXT, " +
                    T_FAHRT.COL_END_DATE + " TEXT, " +
                    T_FAHRT.COL_END_TIME + " TEXT, " +
                    T_FAHRT.COL_END_TOWN + " TEXT, " +
                    T_FAHRT.COL_END_ADDRESS + " TEXT, " +
                    T_FAHRT.COL_END_KMSTAND + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + T_FAHRT.TABLE_NAME;

    // helper
    public class FahrtenbuchDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "fahrtenbuch.db";


        public FahrtenbuchDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    // pattern
    private Context context;
    private static Database myInstance;
    private Database(Context context) {
        this.context = context;
    }
    public String databasePath = "";

    // access
    public static Database getInstance(Context context) {
        if(myInstance == null) {
            myInstance = new Database(context);
        }

        return myInstance;
    }

    public long updateRowWithId(int id, FahrtItem item) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        long return_value = -1;

        try {
            ContentValues values = new ContentValues();

            values.put(T_FAHRT.COL_START_DATE, item.getStartDate());
            values.put(T_FAHRT.COL_START_TIME, item.getStartTime());
            values.put(T_FAHRT.COL_START_TOWN, item.getStartTown());
            values.put(T_FAHRT.COL_START_ADDRESS, item.getStartAddress());
            values.put(T_FAHRT.COL_START_KMSTAND, item.getStartKmstand());

            values.put(T_FAHRT.COL_END_DATE, item.getEndDate());
            values.put(T_FAHRT.COL_END_TIME, item.getEndTime());
            values.put(T_FAHRT.COL_END_TOWN, item.getEndTown());
            values.put(T_FAHRT.COL_END_ADDRESS, item.getEndAddress());
            values.put(T_FAHRT.COL_END_KMSTAND, item.getEndKmstand());

            return_value = db.update(T_FAHRT.TABLE_NAME, values, T_FAHRT._ID + " = " + id, null);

            return return_value;
        } catch (Exception exc) {
            Log.d("error", "db error: " + exc.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    public boolean createTable() {
        boolean returnValue = false;

        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            db.execSQL(SQL_CREATE_TABLE);
            returnValue = true;
        } catch (Exception exc) {
        } finally {
            db.close();
        }

        return returnValue;
    }

    public boolean checkIfTableExists(String table) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            ArrayList<FahrtItem> result = new ArrayList<FahrtItem>();
            Cursor c = db.query(T_FAHRT.TABLE_NAME, new String[]{
                    T_FAHRT._ID
            }, null, null, null, null, null);

            try {
                while(c.moveToNext()) {
                    FahrtItem tmpItem = new FahrtItem();
                    tmpItem.setId(c.getInt(0));

                    result.add(tmpItem);
                }

                c.close();

                if(result.size() > 0) {
                    return true;
                }
            } finally {
                c.close();
            }
        } catch (Exception exc) {

        } finally {
            db.close();
        }

        return false;
    }

    public long insertSingleItem(FahrtItem item) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put(T_FAHRT.COL_START_DATE, item.getStartDate());
            values.put(T_FAHRT.COL_START_TIME, item.getStartTime());
            values.put(T_FAHRT.COL_START_TOWN, item.getStartTown());
            values.put(T_FAHRT.COL_START_ADDRESS, item.getStartAddress());
            values.put(T_FAHRT.COL_START_KMSTAND, item.getStartKmstand());

            values.put(T_FAHRT.COL_END_DATE, item.getEndDate());
            values.put(T_FAHRT.COL_END_TIME, item.getEndTime());
            values.put(T_FAHRT.COL_END_TOWN, item.getEndTown());
            values.put(T_FAHRT.COL_END_ADDRESS, item.getEndAddress());
            values.put(T_FAHRT.COL_END_KMSTAND, item.getEndKmstand());

            long return_value = db.insert(T_FAHRT.TABLE_NAME, T_FAHRT.TABLE_NAME, values);

            return return_value;
        } catch (Exception exc) {
            Log.d("error", "db error: " + exc.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    public long insertSingleField(String column, String value) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(column, value);

            long return_value = db.insert(T_FAHRT.TABLE_NAME, T_FAHRT.TABLE_NAME, values);

            return return_value;
        } catch (Exception exc) {
            Log.d("error", "db error: " + exc.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    public long insertList(ArrayList<String> params) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(T_FAHRT.COL_START_ADDRESS, "x");

            return db.insert(T_FAHRT.TABLE_NAME, T_FAHRT.TABLE_NAME, values);
        } finally {
            db.close();
        }
    }

    public boolean deleteEntries(String table) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            db.execSQL(Database.SQL_DELETE_ENTRIES);
            return true;
        } catch (Exception exc) {
            db.close();
            return false;
        }
    }

    public List<FahrtItem> getAll() {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            ArrayList<FahrtItem> result = new ArrayList<FahrtItem>();
            Cursor c = db.query(T_FAHRT.TABLE_NAME, new String[]{
                    T_FAHRT._ID,
                    T_FAHRT.COL_START_DATE,
                    T_FAHRT.COL_START_TIME,
                    T_FAHRT.COL_START_TOWN,
                    T_FAHRT.COL_START_ADDRESS,
                    T_FAHRT.COL_START_KMSTAND,
                    T_FAHRT.COL_END_DATE,
                    T_FAHRT.COL_END_TIME,
                    T_FAHRT.COL_END_TOWN,
                    T_FAHRT.COL_END_ADDRESS,
                    T_FAHRT.COL_END_KMSTAND
            }, null, null, null, null, null);

            try {
                while(c.moveToNext()) {
                    FahrtItem tmpItem = new FahrtItem();
                    tmpItem.setId(c.getInt(0));
                    tmpItem.setStartFields(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5));
                    tmpItem.setEndFields(c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getString(10));

                    result.add(tmpItem);
                }

                return result;
            } finally {
                c.close();
            }

        } finally {
            db.close();
        }


    }
}