package activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.liehrit.dliehr.fahrtenbuch.R;

import java.util.Locale;

import database.DatabaseHelper;
import implementations.Car;
import implementations.CarArrayAdapter;
import implementations.CarList;
import implementations.Driver;
import implementations.DriverArrayAdapter;
import implementations.DriverList;

public class activityConfig extends AppCompatActivity {
    // region local fields
    private DriverList listDrivers = new DriverList();
    private CarList listCars = new CarList();
    // endregion

    // region listeners
    public void buttonInsertDriverOnClick(View view) {
        // insert into driver table
        String firstName = ((EditText) this.findViewById(R.id.editTextFirstName)).getText().toString();
        String lastName = ((EditText) this.findViewById(R.id.editTextLastName)).getText().toString();

        if(firstName.equals("") || lastName.equals("")) {
            (Toast.makeText(this, this.getString(R.string.activityConfigMessageInsertNames), Toast.LENGTH_SHORT)).show();
            return;
        }

        try {
            DatabaseHelper helper = new DatabaseHelper(this);
            helper.getReadableDatabase().execSQL(String.format(Locale.US, "INSERT INTO Driver (firstName, lastName) VALUES ('%s', '%s');", firstName, lastName));
            helper.close();
            (Toast.makeText(this, this.getString(R.string.activityConfigMessageNewDriverInserted), Toast.LENGTH_SHORT)).show();
            this.displayDrivers();
        } catch (SQLException sqle) {
            (Toast.makeText(this, this.getString(R.string.activityConfigMessageNewDriverInsertedFailed), Toast.LENGTH_SHORT)).show();
        }
    }

    private ListView.OnItemClickListener driverItemClickedListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                // get item id
                DriverArrayAdapter adapter = (DriverArrayAdapter) adapterView.getAdapter();
                long id = adapter.getItemDatabaseId(i);

                // delete
                DatabaseHelper helper = new DatabaseHelper(adapter.getContext());
                //helper.getReadableDatabase().execSQL(String.format(Locale.US, "DELETE FROM Driver WHERE id = %d", id), null);
                helper.getReadableDatabase().delete(DatabaseHelper.TableNames.DRIVER, "id=?", new String[] {String.valueOf(id)});
                helper.close();

                // show new listview
                activityConfig activity = (activityConfig) adapter.getContext();
                activity.displayDrivers();
                (Toast.makeText(adapter.getContext(), R.string.activityConfigMessageDriverDeleted, Toast.LENGTH_SHORT)).show();
            } catch (Exception e) {
                Log.e("activityConfig", e.getMessage());
                (Toast.makeText(adapterView.getContext(), R.string.activityConfigMessageDriverDeleteFailed, Toast.LENGTH_SHORT)).show();
            }
        }
    };

    public void buttonInsertCarOnClick(View view) {
        // insert into driver table
        String model = ((EditText) this.findViewById(R.id.editTextModel)).getText().toString();
        Double mileage = Double.valueOf(((EditText) this.findViewById(R.id.editTextMileage)).getText().toString());

        if(model.equals("") || mileage < 0.0) {
            (Toast.makeText(this, this.getString(R.string.activityConfigMessageInsertNames), Toast.LENGTH_SHORT)).show();
            return;
        }

        try {
            DatabaseHelper helper = new DatabaseHelper(this);
            helper.getReadableDatabase().execSQL(String.format(Locale.US, "INSERT INTO Car (model, mileage) VALUES ('%s', %6.1f);", model, mileage));
            helper.close();
            (Toast.makeText(this, this.getString(R.string.activityConfig_Message_NewCarInserted), Toast.LENGTH_SHORT)).show();
            this.displayCars();
        } catch (SQLException sqle) {
            (Toast.makeText(this, this.getString(R.string.activityConfig_Message_NewCarInsertFailed), Toast.LENGTH_SHORT)).show();
        }
    }

    private ListView.OnItemClickListener carItemClickedListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                // get item id
                CarArrayAdapter adapter = (CarArrayAdapter) adapterView.getAdapter();
                long id = adapter.getItemDatabaseId(i);

                // delete
                DatabaseHelper helper = new DatabaseHelper(adapter.getContext());
                //helper.getReadableDatabase().execSQL(String.format(Locale.US, "DELETE FROM Driver WHERE id = %d", id), null);
                helper.getReadableDatabase().delete(DatabaseHelper.TableNames.CAR, "id=?", new String[] {String.valueOf(id)});
                helper.close();

                // show new spinner list
                activityConfig activity = (activityConfig) adapter.getContext();
                activity.displayCars();
                (Toast.makeText(adapter.getContext(), R.string.activityConfig_Message_CarDeleted, Toast.LENGTH_SHORT)).show();
            } catch (Exception e) {
                Log.e("activityConfig", e.getMessage());
                (Toast.makeText(adapterView.getContext(), R.string.activityConfig_Message_CarDeleteFailed, Toast.LENGTH_SHORT)).show();
            }
        }
    };

    private View.OnTouchListener listViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.getParent().requestDisallowInterceptTouchEvent(true);

            return false;
        }
    };
    // endregion

    // region local methods
    private boolean displayDrivers() {
        try {
            DatabaseHelper helper = new DatabaseHelper(this);

            // get drivers
            Cursor result = helper.getReadableDatabase().rawQuery("SELECT * FROM Driver ORDER BY firstName", null);
            result.moveToFirst();

            this.listDrivers = new DriverList();
            if(result.getCount() > 0) {
                do {
                    this.listDrivers.add(new Driver(result.getLong(0), result.getString(1), result.getString(2)));
                } while(result.moveToNext());
            }

            result.close();
            helper.close();

            // list view
            ListView listViewDrivers = (ListView) this.findViewById(R.id.listViewDrivers);
            listViewDrivers.setAdapter(new DriverArrayAdapter(this, android.R.layout.simple_list_item_1, this.listDrivers));
            listViewDrivers.setOnItemClickListener(this.driverItemClickedListener);
        } catch (Exception e) {
            Log.e("activityConfig", e.getMessage());
            return false;
        }

        return true;
    }

    private boolean displayCars() {
        try {
            DatabaseHelper helper = new DatabaseHelper(this);

            // get drivers
            Cursor result = helper.getReadableDatabase().rawQuery("SELECT * FROM Car ORDER BY model", null);
            result.moveToFirst();

            this.listCars = new CarList();
            if(result.getCount() > 0) {
                do {
                    this.listCars.add(new Car(result.getInt(0), result.getString(1), result.getDouble(2)));
                } while(result.moveToNext());
            }

            result.close();
            helper.close();

            // list view
            ListView listViewCars = (ListView) this.findViewById(R.id.listViewCars);
            listViewCars.setAdapter(new CarArrayAdapter(this, android.R.layout.simple_list_item_1, this.listCars));
            listViewCars.setOnItemClickListener(this.carItemClickedListener);
        } catch (Exception e) {
            Log.e("activityConfig", e.getMessage());
            return false;
        }

        return true;
    }
    // endregion

    // region activity start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout file
        setContentView(R.layout.activity_config);

        // display drivers and cars
        this.displayDrivers();
        this.displayCars();

        // configure scroll views
        ((ListView) this.findViewById(R.id.listViewDrivers)).setOnTouchListener(this.listViewOnTouchListener);
        ((ListView) this.findViewById(R.id.listViewCars)).setOnTouchListener(this.listViewOnTouchListener);
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
                /*
                Intent intent = new Intent(this, activityConfig.class);
                this.startActivity(intent);
                */

                return true;
            }
            case R.id.menuDatabase: {
                Intent intent = new Intent(this, activityDatabase.class);
                this.startActivity(intent);

                return true;
            }
        }
    }
    // endregion
}