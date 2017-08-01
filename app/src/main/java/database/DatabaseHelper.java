package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.liehrit.dliehr.fahrtenbuch.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // region class fields
    public static final String DATABASE_NAME = "database.db";
    // endregion

    // region object fields
    private Context context = null;
    // endregion

    // region init
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }
    // endregion

    // region sqlite methods
    @Override
    public void onCreate(SQLiteDatabase database) {
        List<String> queryList = new ArrayList<String>();
        queryList.add(comprehensive.App.getStringContentFromRawFile(this.context, R.raw.create_table_trip));
        queryList.add(comprehensive.App.getStringContentFromRawFile(this.context, R.raw.create_table_trip_waypoint));
        queryList.add(comprehensive.App.getStringContentFromRawFile(this.context, R.raw.create_table_car));
        queryList.add(comprehensive.App.getStringContentFromRawFile(this.context, R.raw.create_table_driver));

        for(String q:queryList) {
            try {
                database.execSQL(q);
            } catch (SQLException sqle) {
                Log.e("DatabaseHelper", sqle.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        try {
            String dropAllTables = "";

            database.execSQL(dropAllTables);
        } catch (Exception e) {
            Log.e("DatabaseHelper", e.getMessage());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase database) {
        super.onOpen(database);

        if(!database.isReadOnly()) {
            database.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
    // endregion

    // region database operations
    public long insert(String table, ContentValues values) throws SQLException {
        return this.getReadableDatabase().insertOrThrow(table, null, values);
    }
    // endregion

    // region table names
    public static class TableNames {
        public final static String TRIP = "Trip";
        public final static String TRIPWAYPOINT = "TripWaypoint";
        public final static String DRIVER = "Driver";
        public final static String CAR = "Car";
    }
    // endregion
}