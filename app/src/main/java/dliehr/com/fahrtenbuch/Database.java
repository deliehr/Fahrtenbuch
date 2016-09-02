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
    private static final String TAG = Database.class.getSimpleName();

    public static abstract class T_FAHRT implements BaseColumns {
        public static final String TABLE_NAME = "T_FAHRT";

        public static final String COL_START_DATE = "start_date";   // text 1
        public static final String COL_START_TIME = "start_time";   // text 2
        public static final String COL_START_TOWN = "start_town";   // text 3
        public static final String COL_START_ADDRESS = "start_address"; //text  4
        public static final String COL_START_KMSTAND = "start_kmstand"; // real 5
        public static final String COL_START_LATITUDE = "start_latitude";   // real 6
        public static final String COL_START_LONGITUDE = "start_longitude"; // real 7
        public static final String COL_START_ORTSZUSATZ = "start_ortszusatz";   // text 8
        public static final String COL_START_PRIVATE_FAHRT = "start_private_fahrt"; // integer  9
        public static final String COL_START_CAR = "start_car"; // text 10

        public static final String COL_END_DATE = "end_date";   // text 11
        public static final String COL_END_TIME = "end_time";   // text 12
        public static final String COL_END_TOWN = "end_town";   // text 13
        public static final String COL_END_ADDRESS = "end_address"; // text 14
        public static final String COL_END_KMSTAND = "end_kmstand"; // real 15
        public static final String COL_END_LATITUDE = "end_latitude";   // real 16
        public static final String COL_END_LONGITUDE = "end_longitude"; // real 17
        public static final String COL_END_ORTSZUSATZ = "end_ortszusatz";   // text 18
        public static final String COL_END_PRIVATE_FAHRT = "end_private_fahrt"; // integer  19
        public static final String COL_END_CAR = "end_car"; // text 20
    }

    public static abstract class T_POI implements BaseColumns {
        public static final String TABLE_NAME = "T_POI";

        public static final String COL_POSTAL_CODE = "postal_code";   // int 1
        public static final String COL_LOCALITY = "locality";   // text 2
        public static final String COL_ADDRESS = "address";   // text 3
        public static final String COL_ADDITIONAL_INFO = "additional_info"; //text  4
        public static final String COL_LATITUDE = "latitude"; // real 5
        public static final String COL_LONGITUDE = "longitude";   // real 6
        public static final String COL_PRIVATE_DRIVE = "private_drive"; // int 7
    }

    private static final String SQL_CREATE_TABLE_T_FAHRT =
            "CREATE TABLE " + T_FAHRT.TABLE_NAME + " (" +
                    T_FAHRT._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    T_FAHRT.COL_START_DATE + " TEXT, " +
                    T_FAHRT.COL_START_TIME + " TEXT, " +
                    T_FAHRT.COL_START_TOWN + " TEXT, " +
                    T_FAHRT.COL_START_ADDRESS + " TEXT, " +
                    T_FAHRT.COL_START_KMSTAND + " REAL, " +
                    T_FAHRT.COL_START_LATITUDE + " REAL, " +
                    T_FAHRT.COL_START_LONGITUDE + " REAL, " +
                    T_FAHRT.COL_START_ORTSZUSATZ + " TEXT, " +
                    T_FAHRT.COL_START_PRIVATE_FAHRT + " INTEGER, " +
                    T_FAHRT.COL_START_CAR + " TEXT, " +

                    T_FAHRT.COL_END_DATE + " TEXT, " +
                    T_FAHRT.COL_END_TIME + " TEXT, " +
                    T_FAHRT.COL_END_TOWN + " TEXT, " +
                    T_FAHRT.COL_END_ADDRESS + " TEXT, " +
                    T_FAHRT.COL_END_KMSTAND + " REAL, " +
                    T_FAHRT.COL_END_LATITUDE + " REAL, " +
                    T_FAHRT.COL_END_LONGITUDE + " REAL, " +
                    T_FAHRT.COL_END_ORTSZUSATZ + " TEXT, " +
                    T_FAHRT.COL_END_PRIVATE_FAHRT + " INTEGER, " +
                    T_FAHRT.COL_END_CAR + " TEXT)";

    private static final String SQL_CREATE_TABLE_T_POI =
            "CREATE TABLE " + T_POI.TABLE_NAME + "(" +
                    T_POI._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    T_POI.COL_POSTAL_CODE + " INTEGER, " +
                    T_POI.COL_LOCALITY + " TEXT, " +
                    T_POI.COL_ADDRESS + " TEXT, " +
                    T_POI.COL_ADDITIONAL_INFO + " TEXT, " +
                    T_POI.COL_LATITUDE + " REAL, " +
                    T_POI.COL_LONGITUDE + " REAL, " +
                    T_POI.COL_PRIVATE_DRIVE + " INTEGER)";

    private static final String SQL_DROP_TABLE_T_FAHRT = "DROP TABLE IF EXISTS " + T_FAHRT.TABLE_NAME;

    private static final String SQL_DROP_TABLE_T_POI = "DROP TABLE IF EXISTS " + T_POI.TABLE_NAME;

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
            db.execSQL(SQL_CREATE_TABLE_T_FAHRT);
            db.execSQL(SQL_CREATE_TABLE_T_POI);
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

    public long updateRowWithIdFromTableT_FAHRT(int id, FahrtItem item) {
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
            values.put(T_FAHRT.COL_START_LATITUDE, item.getStartLatitude());
            values.put(T_FAHRT.COL_START_LONGITUDE, item.getStartLongitude());
            values.put(T_FAHRT.COL_START_ORTSZUSATZ, item.getStartOrtszusatz());
            values.put(T_FAHRT.COL_START_PRIVATE_FAHRT, item.getStartPrivateFahrt());
            values.put(T_FAHRT.COL_START_CAR, item.getStartCar());

            values.put(T_FAHRT.COL_END_DATE, item.getEndDate());
            values.put(T_FAHRT.COL_END_TIME, item.getEndTime());
            values.put(T_FAHRT.COL_END_TOWN, item.getEndTown());
            values.put(T_FAHRT.COL_END_ADDRESS, item.getEndAddress());
            values.put(T_FAHRT.COL_END_KMSTAND, item.getEndKmstand());
            values.put(T_FAHRT.COL_END_LATITUDE, item.getEndLatitude());
            values.put(T_FAHRT.COL_END_LONGITUDE, item.getEndLongitude());
            values.put(T_FAHRT.COL_END_ORTSZUSATZ, item.getEndOrtszusatz());
            values.put(T_FAHRT.COL_END_PRIVATE_FAHRT, item.getEndPrivateFahrt());
            values.put(T_FAHRT.COL_END_CAR, item.getEndCar());

            return_value = db.update(T_FAHRT.TABLE_NAME, values, T_FAHRT._ID + " = " + id, null);

            return return_value;
        } catch (Exception exc) {
            Log.d("error", "db error: " + exc.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    public boolean createTableT_FAHRT() {
        boolean returnValue = false;

        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            db.execSQL(SQL_CREATE_TABLE_T_FAHRT);
            returnValue = true;
        } catch (Exception exc) {
        } finally {
            db.close();
        }

        return returnValue;
    }

    public boolean createTableT_POI() {
        boolean returnValue = false;

        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        try {
            db.execSQL(SQL_CREATE_TABLE_T_POI);
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
            Cursor c = db.query(table, new String[]{
                    table + "._ID"
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

    public long insertSingleItemIntoT_FAHRT(FahrtItem item) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put(T_FAHRT.COL_START_DATE, item.getStartDate());
            values.put(T_FAHRT.COL_START_TIME, item.getStartTime());
            values.put(T_FAHRT.COL_START_TOWN, item.getStartTown());
            values.put(T_FAHRT.COL_START_ADDRESS, item.getStartAddress());
            values.put(T_FAHRT.COL_START_KMSTAND, item.getStartKmstand());
            values.put(T_FAHRT.COL_START_LATITUDE, item.getStartLatitude());
            values.put(T_FAHRT.COL_START_LONGITUDE, item.getStartLongitude());
            values.put(T_FAHRT.COL_START_ORTSZUSATZ, item.getStartOrtszusatz());
            values.put(T_FAHRT.COL_START_PRIVATE_FAHRT, item.getStartPrivateFahrt());
            values.put(T_FAHRT.COL_START_CAR, item.getStartCar());

            values.put(T_FAHRT.COL_END_DATE, item.getEndDate());
            values.put(T_FAHRT.COL_END_TIME, item.getEndTime());
            values.put(T_FAHRT.COL_END_TOWN, item.getEndTown());
            values.put(T_FAHRT.COL_END_ADDRESS, item.getEndAddress());
            values.put(T_FAHRT.COL_END_KMSTAND, item.getEndKmstand());
            values.put(T_FAHRT.COL_END_LATITUDE, item.getEndLatitude());
            values.put(T_FAHRT.COL_END_LONGITUDE, item.getEndLongitude());
            values.put(T_FAHRT.COL_END_ORTSZUSATZ, item.getEndOrtszusatz());
            values.put(T_FAHRT.COL_END_PRIVATE_FAHRT, item.getEndPrivateFahrt());
            values.put(T_FAHRT.COL_END_CAR, item.getEndCar());

            long return_value = db.insert(T_FAHRT.TABLE_NAME, T_FAHRT.TABLE_NAME, values);

            return return_value;
        } catch (Exception exc) {
            Log.d("error", "db error: " + exc.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }

    public long insertSingleItemIntoT_POI(PointOfInterest poi) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        long returnNumber = -1;

        try {
            ContentValues values = new ContentValues();

            values.put(T_POI.COL_POSTAL_CODE, Integer.valueOf(poi.getPostalCode()));
            values.put(T_POI.COL_LOCALITY, poi.getLocality());
            values.put(T_POI.COL_ADDRESS, poi.getAddressLine());
            values.put(T_POI.COL_ADDITIONAL_INFO, poi.getAdditionalInfo());
            values.put(T_POI.COL_LATITUDE, poi.getLatitude());
            values.put(T_POI.COL_LONGITUDE, poi.getLongitude());
            values.put(T_POI.COL_PRIVATE_DRIVE, poi.getPrivateDrive());

            returnNumber = db.insert(T_POI.TABLE_NAME, T_POI.TABLE_NAME, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.close();
        }

        return returnNumber;
    }

    public long insertSingleFieldIntoT_FAHRT(String column, String value) {
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

    public boolean dropTable(String table) {
        FahrtenbuchDbHelper helper = new FahrtenbuchDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        try {

            switch (table) {
                case T_FAHRT.TABLE_NAME: {
                    // first delete
                    db.execSQL(Database.SQL_DROP_TABLE_T_FAHRT);

                    // recreate table
                    db.execSQL(Database.SQL_CREATE_TABLE_T_FAHRT);

                    break;
                }

                case T_POI.TABLE_NAME: {
                    // first delete
                    db.execSQL(Database.SQL_DROP_TABLE_T_POI);

                    // recreate table
                    db.execSQL(Database.SQL_CREATE_TABLE_T_POI);

                    break;
                }
            }


            return true;
        } catch (Exception exc) {
            db.close();
            return false;
        }
    }

    public List<FahrtItem> getAllFromT_FAHRT() {
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
                    T_FAHRT.COL_START_LATITUDE,
                    T_FAHRT.COL_START_LONGITUDE,
                    T_FAHRT.COL_START_ORTSZUSATZ,
                    T_FAHRT.COL_START_PRIVATE_FAHRT,
                    T_FAHRT.COL_START_CAR,
                    T_FAHRT.COL_END_DATE,
                    T_FAHRT.COL_END_TIME,
                    T_FAHRT.COL_END_TOWN,
                    T_FAHRT.COL_END_ADDRESS,
                    T_FAHRT.COL_END_KMSTAND,
                    T_FAHRT.COL_END_LATITUDE,
                    T_FAHRT.COL_END_LONGITUDE,
                    T_FAHRT.COL_END_ORTSZUSATZ,
                    T_FAHRT.COL_END_PRIVATE_FAHRT,
                    T_FAHRT.COL_END_CAR
            }, null, null, null, null, null);

            try {
                while(c.moveToNext()) {
                    FahrtItem tmpItem = new FahrtItem();

                    // first field
                    tmpItem.setId(c.getInt(0));

                    // start fields
                    tmpItem.setStartFields(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getDouble(7), c.getString(8), (c.getInt(9) != 0), c.getString(10));

                    // end fields
                    tmpItem.setEndFields(c.getString(11), c.getString(12), c.getString(13), c.getString(14), c.getDouble(15), c.getDouble(16), c.getDouble(17), c.getString(18), (c.getInt(19) != 0), c.getString(20));

                    //tmpItem.setStartFields(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5));
                    //tmpItem.setEndFields(c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getDouble(10));

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