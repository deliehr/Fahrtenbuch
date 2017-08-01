package activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.liehrit.dliehr.fahrtenbuch.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.DatabaseHelper;
import implementations.DatabaseTripArrayAdapter;
import threads.HttpThread;

public class activityDatabase extends AppCompatActivity {
    // region local fields
    private final String TAG = "activityDatabase";
    // endregion

    // region listeners
    private ListView.OnItemClickListener onItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            // adapter
            DatabaseTripArrayAdapter adapter = (DatabaseTripArrayAdapter) adapterView.getAdapter();
            long tripDatabaseId = adapter.getItemDatabaseId(position);

            // string builder
            StringBuilder builder = new StringBuilder();

            // database
            DatabaseHelper helper = new DatabaseHelper(adapter.getContext());
            Cursor result = helper.getReadableDatabase().query(DatabaseHelper.TableNames.TRIPWAYPOINT, new String[] {"latitude", "longitude", "accuracy", "elevation", "pointOfTime"}, "fk_trip_id=?", new String[] { String.valueOf(tripDatabaseId) }, null, null, "id ASC");

            if(result.getCount() > 0) {
                result.moveToFirst();

                // 2011-12-31T23:59:59Z
                String dateString = "", dateStringPart1 = "", dateStringPart2 = "";
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(result.getString(4));
                    dateStringPart1 = new SimpleDateFormat("yyyy-MM-dd").format(date);
                    dateStringPart2 = new SimpleDateFormat("HH:mm:ss").format(date);

                    dateString = String.format(Locale.GERMAN, "%sT%sZ", dateStringPart1, dateStringPart2);
                } catch (ParseException pe) {
                    Log.e("activityDatabase", pe.getMessage());
                    dateString = result.getString(4);
                }

                do {
                    builder.append(String.format(Locale.US, "%s,%s,%d,%3.1f,%s;", result.getString(0), result.getString(1), result.getInt(2), result.getDouble(3), dateString));
                } while(result.moveToNext());
            }

            result.close();
            helper.close();

            // send via http
            HttpThread httpThread = new HttpThread(builder.toString());
            httpThread.start();

            // toast
            (Toast.makeText(adapter.getContext(), "Data sent", Toast.LENGTH_SHORT)).show();
        }
    };
    // endregion

    //region activity start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout file
        setContentView(R.layout.activity_database);

        // list view click listener
        ListView listViewTrips = (ListView) this.findViewById(R.id.listViewTrips);
        listViewTrips.setOnItemClickListener(this.onItemClickListener);
        List<String> listTrips = new ArrayList<String>();
        List<Long> listTripDatabaseIds = new ArrayList<Long>();

        // database
        DatabaseHelper helper = new DatabaseHelper(this);
        Cursor result = helper.getReadableDatabase().query(DatabaseHelper.TableNames.TRIP, new String[] { "id", "description", "startPointOfTime", "distance"}, null, null, null, null, "id ASC");
        result.moveToFirst();

        if(result.getCount() > 0) {
            do {
                String dateString = "";
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(result.getString(2));
                    dateString = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
                } catch (ParseException pe) {
                    dateString = result.getString(2);
                }

                String description = result.getString(1);
                if(description.equals("")) {
                    description = "descr.";
                }

                listTripDatabaseIds.add(result.getLong(0));
                listTrips.add(String.format(Locale.GERMAN, "id: %d, %s, %s, %6.1f km", result.getInt(0), description, dateString, (result.getDouble(3) / 1000.0)));
            } while(result.moveToNext());
        }

        result.close();
        helper.close();

        // list view set values
        DatabaseTripArrayAdapter adapter = new DatabaseTripArrayAdapter(this, android.R.layout.simple_list_item_1, listTrips, listTripDatabaseIds);
        listViewTrips.setAdapter(adapter);
    }
    // endregion

    // region activity menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
            case R.id.menuStart: {
                Intent intent = new Intent(this, activityMain.class);
                this.startActivity(intent);

                return true;
            }
            case R.id.menuConfig: {
                Intent intent = new Intent(this, activityConfig.class);
                this.startActivity(intent);

                return true;
            }
            case R.id.menuDatabase: {
                /*
                Intent intent = new Intent(this, activityDatabase.class);
                this.startActivity(intent);

                return true;
                */

                return true;
            }
        }
    }
    // endregion
}
