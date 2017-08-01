package activities;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liehrit.dliehr.fahrtenbuch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import database.DatabaseHelper;
import implementations.Car;
import implementations.CarArrayAdapter;
import implementations.CarList;
import implementations.Driver;
import implementations.DriverArrayAdapter;
import implementations.DriverList;
import implementations.ExtEditText;
import implementations.GpsLocationListener;
import implementations.SpinnerOnItemSelectedListener;
import implementations.WayPoint;
import threads.LocationThread;
import threads.NetworkCheckThread;
import threads.TimeThread;
import threads.TripThread;

public class activityMain extends AppCompatActivity {
    // region local fields
    private final String TAG = "activityMain";
    private CarList listCars = new CarList();
    private DriverList listDrivers = new DriverList();
    private LocationManager locationManager;
    private TimeThread timeThread = null;
    private NetworkCheckThread networkCheckThread = null;
    private LocationThread locationThread = null;
    private boolean activeTrip = false;
    private long activeTripDatabaseId = -1;
    private TripThread tripThread = null;
    private GpsLocationListener gpsLocationListener = null;
    // endregion

    // region local methods
    private boolean readCars() {
        try {
            // database data
            DatabaseHelper helper = new DatabaseHelper(this);

            // read cars
            Cursor result = helper.getReadableDatabase().query(DatabaseHelper.TableNames.CAR, new String[] {"id", "model", "mileage"}, null, null, null, null, "model ASC");
            result.moveToFirst();

            this.listCars = new CarList();
            if(result.getCount() > 0) {
                do {
                    this.listCars.add(new Car(result.getInt(0), result.getString(1), result.getDouble(2)));
                } while(result.moveToNext());
            }

            // close database
            helper.close();
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private boolean displayCars() {
        try {
            Spinner spinner = (Spinner) this.findViewById(R.id.spinnerCar);
            CarArrayAdapter adapter = new CarArrayAdapter(this, android.R.layout.simple_list_item_1, this.listCars);
            spinner.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;

    }

    private boolean readDrivers() {
        try {
            // database data
            DatabaseHelper helper = new DatabaseHelper(this);

            // read cars
            Cursor result = helper.getReadableDatabase().query(DatabaseHelper.TableNames.DRIVER, new String[] {"id", "firstName", "lastName"}, null, null, null, null, "firstName ASC");
            result.moveToFirst();

            this.listDrivers = new DriverList();
            if(result.getCount() > 0) {
                do {
                    this.listDrivers.add(new Driver(result.getInt(0), result.getString(1), result.getString(2)));
                } while(result.moveToNext());
            }

            // close database
            helper.close();
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private boolean displayDrivers() {
        try {
            Spinner spinner = (Spinner) this.findViewById(R.id.spinnerDriver);
            DriverArrayAdapter adapter = new DriverArrayAdapter(this, android.R.layout.simple_list_item_1, this.listDrivers);
            spinner.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private boolean tripStarted() {
        try {
            // change button text
            Button buttonStart = (Button) this.findViewById(R.id.buttonStartTrip);
            buttonStart.setText(this.getString(R.string.activityMainStartButtonEndTrip));

            // region collect data
            String pointOfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            String description = ((EditText) this.findViewById(R.id.editTextDescription)).getText().toString();
            String address = ((EditText) this.findViewById(R.id.editTextAddress)).getText().toString();
            String locality = ((EditText) this.findViewById(R.id.editTextPlace)).getText().toString();
            boolean privateTrip = ((CheckBox) this.findViewById(R.id.checkBoxPrivateTrip)).isChecked();

            double mileage = -1.0;
            try {
                mileage = Double.valueOf(((EditText) this.findViewById(R.id.editTextMileage)).getText().toString());
            } catch (NumberFormatException nfe) {
                Log.e(this.TAG, nfe.getMessage());
                mileage = -1.0;
            }

            Spinner spinnerCars = (Spinner) this.findViewById(R.id.spinnerCar);
            int selectedIndexCar = spinnerCars.getSelectedItemPosition();
            CarArrayAdapter carArrayAdapter = (CarArrayAdapter) spinnerCars.getAdapter();
            Car car = carArrayAdapter.getListCars().get(selectedIndexCar);

            Spinner spinnerDrivers = (Spinner) this.findViewById(R.id.spinnerDriver);
            int selectedIndexDriver = spinnerDrivers.getSelectedItemPosition();
            DriverArrayAdapter driverArrayAdapter = (DriverArrayAdapter) spinnerDrivers.getAdapter();
            Driver driver = driverArrayAdapter.getListDrivers().get(selectedIndexDriver);
            // endregion

            // region save collected data to database
            DatabaseHelper helper = new DatabaseHelper(this);

            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("fk_driver_id", driver.getDatabaseId());
                contentValues.put("fk_car_id", car.getDatabaseId());
                contentValues.put("uuid", UUID.randomUUID().toString());
                contentValues.put("description", description);
                contentValues.put("startAddress", address);
                contentValues.put("startPlace", locality);
                contentValues.put("startMileage", mileage);
                contentValues.put("startPointOfTime", pointOfTime);
                contentValues.put("privateTrip", comprehensive.App.convertBooleanToInt(privateTrip));

                activeTripDatabaseId = helper.getReadableDatabase().insertOrThrow(DatabaseHelper.TableNames.TRIP, null, contentValues);
                (Toast.makeText(this, this.getString(R.string.activityMain_Message_TripStarted), Toast.LENGTH_SHORT)).show();
                helper.close();
            } catch (android.database.SQLException sqle) {
                Log.e(this.TAG, sqle.getMessage());
                (Toast.makeText(this, this.getString(R.string.activityMain_Message_TripNotStarted), Toast.LENGTH_LONG)).show();
                helper.close();
                return false;
            }
            // endregion

            // start trip thread
            this.tripThread = new TripThread(1000, this.gpsLocationListener, this);
            this.tripThread.start();

            // region ui elements
            try {
                // hide ui elements
                for(int id:this.getUiElementIdsForStart()) {
                    ((View) this.findViewById(id)).setVisibility(View.GONE);
                }

                for(int id:this.getUiElementIdsForEnd()) {
                    ((View) this.findViewById(id)).setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Log.e(this.TAG, e.getMessage());
                return false;
            }
            // endregion
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private boolean tripEnded() {
        try {
            // stop trip thread
            this.tripThread.interrupt();

            // disable stop button
            (this.findViewById(R.id.buttonStartTrip)).setEnabled(false);

            // enable save button
            (this.findViewById(R.id.buttonSaveTrip)).setVisibility(View.VISIBLE);

            // update mileage
            ExtEditText editTextMileage = this.findViewById(R.id.editTextMileage);

            // get old mileage
            Spinner spinnerCar = this.findViewById(R.id.spinnerCar);
            CarArrayAdapter adapter = (CarArrayAdapter) spinnerCar.getAdapter();
            int selectedCarIndex = spinnerCar.getSelectedItemPosition();
            Car selectedCar = adapter.getListCars().get(selectedCarIndex);

            // distance
            double traveledDistance = this.tripThread.getTraveledDistance();
            double newMileage = selectedCar.getMileage() + (traveledDistance / 1000.0);
            editTextMileage.setText(String.format(Locale.GERMAN, "%6.0f", newMileage));

            Log.i(this.TAG, "trip ended (not saved). traveled distance: " + String.valueOf(traveledDistance));

            // save new mileage in hidden edit text field
            // todo eventuelle Fehlerquelle
            editTextMileage.setHiddenValues(new Object[] { newMileage });
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private List<Integer> getUiElementIdsForStart() {
        List<Integer> uiElements = new ArrayList<Integer>();

        uiElements.add(R.id.labelDescription);
        uiElements.add(R.id.editTextDescription);
        // uiElements.add(R.id.labelMileage);
        // uiElements.add(R.id.editTextMileage);
        uiElements.add(R.id.labelCar);
        uiElements.add(R.id.spinnerCar);
        uiElements.add(R.id.labelPrivateTrip);
        uiElements.add(R.id.checkBoxPrivateTrip);
        uiElements.add(R.id.labelDriver);
        uiElements.add(R.id.spinnerDriver);

        return uiElements;
    }

    private List<Integer> getUiElementIdsForEnd() {
        List<Integer> uiElements = new ArrayList<Integer>();
        uiElements.add(R.id.labelTraveledDistance);
        uiElements.add(R.id.textViewTraveledDistance);
        uiElements.add(R.id.labelTravelSpeed);
        uiElements.add(R.id.textViewTravelSpeed);

        return uiElements;
    }
    // endregion

    // region listeners
    public void checkBoxAddressOnClick(View view) {
        CheckBox me = (CheckBox) view;
        CheckBox checkBoxPlace = (CheckBox) this.findViewById(R.id.checkBoxAutoPlace);

        if(!me.isChecked() || !checkBoxPlace.isChecked()) {
            this.locationThread.setSearchForLocation(true);
        }

        if(me.isChecked() && checkBoxPlace.isChecked()) {
            this.locationThread.setSearchForLocation(false);
        }
    }

    public void buttonStartTrip(View view) {
        // check state
        if(this.activeTrip) {
            // trip already startet
            this.activeTrip = false;
            this.tripEnded();
        } else {
            // start new trip
            this.activeTrip = true;
            this.tripStarted();
        }
    }

    public void buttonSaveTrip(View view) {
        // region collect data
        List<WayPoint> collectedWayPoints = new ArrayList<WayPoint>(this.tripThread.getWaypointList());
        String pointOfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String address = ((EditText) this.findViewById(R.id.editTextAddress)).getText().toString();
        String locality = ((EditText) this.findViewById(R.id.editTextPlace)).getText().toString();
        double distance = this.tripThread.getTraveledDistance() * 1.0025;   // deviation correction
        // endregion

        // region save collected data to database
        DatabaseHelper helper = new DatabaseHelper(this);

        try {
            // insert trip data
            ContentValues contentValues = new ContentValues();
            contentValues.put("endPointOfTime", pointOfTime);
            contentValues.put("endAddress", address);
            contentValues.put("endPlace", locality);
            contentValues.put("distance", Double.valueOf(String.format(Locale.US, "%6.3f", distance)));

            helper.getReadableDatabase().update(DatabaseHelper.TableNames.TRIP, contentValues, "id=?", new String[] { String.valueOf(this.activeTripDatabaseId) });

            // insert waypoints
            for(WayPoint w: collectedWayPoints) {
                contentValues = new ContentValues();
                contentValues.put("fk_trip_id", this.activeTripDatabaseId);
                contentValues.put("latitude", w.getLatitude());
                contentValues.put("longitude", w.getLongitude());
                contentValues.put("accuracy", w.getAccuracy());
                contentValues.put("elevation", w.getAltitude());
                contentValues.put("pointOfTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(w.getPointOfTime()));

                helper.getReadableDatabase().insertOrThrow(DatabaseHelper.TableNames.TRIPWAYPOINT, null, contentValues);
            }

            (Toast.makeText(this, this.getString(R.string.activityMain_Message_TripEnded), Toast.LENGTH_SHORT)).show();
            helper.close();
            this.activeTripDatabaseId = -1;
        } catch (android.database.SQLException sqle) {
            Log.e(this.TAG, sqle.getMessage());
            (Toast.makeText(this, this.getString(R.string.activityMain_Message_TripNotEnded), Toast.LENGTH_LONG)).show();
            helper.close();
        }
        // endregion

        // region save mileage to car
        Spinner spinnerCar = (Spinner) this.findViewById(R.id.spinnerCar);
        CarArrayAdapter adapter = (CarArrayAdapter) spinnerCar.getAdapter();
        int selectedIndexCar = spinnerCar.getSelectedItemPosition();
        Car selectedCar = adapter.getListCars().get(selectedIndexCar);
        ExtEditText editTextMileage = (ExtEditText) this.findViewById(R.id.editTextMileage);

        Log.i(this.TAG, "save trip: car from spinner list. mileage: " + String.valueOf(selectedCar.getMileage()));

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("mileage", Double.valueOf(String.format(Locale.US, "%6.5f", (double) editTextMileage.getHiddenValues()[0])));

            Log.i(this.TAG, "save new mileage of car to db: " + String.format(Locale.US, "%6.5f", (double) editTextMileage.getHiddenValues()[0]));

            helper.getReadableDatabase().update(DatabaseHelper.TableNames.CAR, contentValues, "id=?", new String[] { String.valueOf(selectedCar.getDatabaseId()) });
        } catch (android.database.SQLException sqlE) {
            Log.e(this.TAG, sqlE.getMessage());
        }
        // endregion

        // enable start button
        (this.findViewById(R.id.buttonStartTrip)).setEnabled(true);
        ((Button) this.findViewById(R.id.buttonStartTrip)).setText(this.getString(R.string.activityMainStartButtonStartTrip));

        // hide save button
        (this.findViewById(R.id.buttonSaveTrip)).setVisibility(View.GONE);

        // text views distance & speed
        ((TextView) this.findViewById(R.id.textViewTraveledDistance)).setText("");
        ((TextView) this.findViewById(R.id.textViewTravelSpeed)).setText("");

        // region ui elements
        // disable stop button
        ((Button) this.findViewById(R.id.buttonStartTrip)).setEnabled(true);

        try {
            // show ui elements
            for(int id:this.getUiElementIdsForStart()) {
                ((View) this.findViewById(id)).setVisibility(View.VISIBLE);
            }

            // hide
            for(int id:this.getUiElementIdsForEnd()) {
                ((View) this.findViewById(id)).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(this.TAG, e.getMessage());
        }

        // endregion

        // update car spinner
        this.readCars();
        this.displayCars();
    }
    // endregion

    // region activity start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout file
        //this.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        setContentView(R.layout.activity_main);

        // service
        //Intent service = new Intent(this, BackgroundService.class);
        //this.startService(service);

        // date and time
        ((TextView) this.findViewById(R.id.textViewTimeOfDay)).setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));

        // read from database
        this.readCars();
        this.readDrivers();

        // display data
        this.displayCars();
        this.displayDrivers();

        // configure spinners
        ((Spinner) this.findViewById(R.id.spinnerCar)).setOnItemSelectedListener(new SpinnerOnItemSelectedListener(new Object[] {this}) {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // spinner init, get mileage
                CarArrayAdapter adapter = (CarArrayAdapter) adapterView.getAdapter();
                double mileage = adapter.getListCars().get(i).getMileage();

                // activity
                activityMain activity = (activityMain) this.objects[0];
                ((ExtEditText) activity.findViewById(R.id.editTextMileage)).setText(String.format(Locale.US, "%6.0f", mileage));

                // save raw mileage value in hidden field
                ((ExtEditText) activity.findViewById(R.id.editTextMileage)).setHiddenValues(new Object[] { mileage });

                Log.i("activityMain", "spinner init: car mileage: " + String.valueOf(mileage));
            }
        });

        // location manager & listener
        this.locationManager = (LocationManager) this.getSystemService(Service.LOCATION_SERVICE);

        if(this.locationManager == null) {
            (Toast.makeText(this, this.getString(R.string.activityMain_Message_LocationManagerNotAvailable), Toast.LENGTH_LONG)).show();
            return;
        }

        // gps criteria
        Criteria gpsCriteria = new Criteria();
        gpsCriteria.setAccuracy(Criteria.ACCURACY_FINE);

        // location listener
        this.gpsLocationListener = new GpsLocationListener();

        try {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.gpsLocationListener);
        } catch (SecurityException se) {
            Log.e(this.TAG, se.getMessage());
            (Toast.makeText(this, this.getString(R.string.activityMain_Message_GPSProviderNotEnabled), Toast.LENGTH_LONG)).show();
            return;
        }

        // first location
        try {
            this.gpsLocationListener.onLocationChanged(this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } catch (SecurityException se) {
            Log.e(this.TAG, se.getMessage());
            (Toast.makeText(this, this.getString(R.string.activityMain_Message_GPSProviderNotEnabled), Toast.LENGTH_LONG)).show();
            return;
        }

        // threads
        this.timeThread = new TimeThread(this);
        this.timeThread.start();

        this.networkCheckThread = new NetworkCheckThread(this);
        this.networkCheckThread.start();

        this.locationThread = new LocationThread(500, this.gpsLocationListener, this, this.networkCheckThread);
        this.locationThread.start();
        this.locationThread.setSearchForLocation(true);
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
                return true;
            }
            case R.id.menuConfig: {
                Intent intent = new Intent(this, activityConfig.class);
                this.startActivity(intent);

                return true;
            }
            case R.id.menuDatabase: {
                Intent intent = new Intent(this, activityDatabase.class);
                this.startActivity(intent);

                return true;
            }
        }


        //return super.onOptionsItemSelected(item);
    }
    // endregion
}