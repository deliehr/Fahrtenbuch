package dliehr.com.fahrtenbuch;

import android.*;
import android.Manifest;
import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import dliehr.com.fahrtenbuch.PointOfInterest;

public class activityStart extends AppCompatActivity {
    // app lifetime cycle
    private Boolean paused = false, stopped = false, calledOnRestart = false;

    // permissions
    private static final int PERMISSION_INTERNET = 0;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 3;

    // gps
    static Location mLocation = null;
    static GoogleApiClient mGoogleApiClient = null;
    static LocationRequest mLocationRequest = null;
    static LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;

            Log.i("info", "Location changed (onLocationChanged)");

            updateAddressField();
            updateLocationText();
        }
    };

    private static GoogleApiClient.ConnectionCallbacks mConnectionFallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if(mLocation == null) {
                try {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

                    //Toast.makeText(context, "location update (onConnected)", Toast.LENGTH_LONG).show();
                    Log.i("info", "location update (onConnected)");

                    updateAddressField();
                    updateLocationText();
                } catch (SecurityException se) {
                    Toast.makeText(context, "getting location not allowed", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
        }
    };

    private static GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(context, "connection failed", Toast.LENGTH_LONG).show();
        }
    };

    static final long UPDATE_INTERVAL = 2000;
    static final long FASTEST_UPDATE_INTERVAL = 1000;

    // static views
    static TextView tvLocation = null;
    static EditText etAdressField = null;
    static EditText etTimeDateField = null;
    static EditText etKmStand = null;

    // drive
    boolean driveStarted = false;
    boolean driveEnded = false;
    double lastKmstand = -1.0;

    // bluetooth
    static BluetoothAdapter bluetoothAdapter = null;
    static BluetoothDevice bluetoothObd2Device = null;
    static BluetoothSocket bluetoothSocket = null;

    // threads
    static boolean runningBluetoothThread = false;
    static boolean runningLocationThread = true;
    static Context context = null;

    // region handle bluetooth
    static void startEnableBluetoothConnection() {
        try {
            // only connect if no connection exists
            if(!activityStart.bluetoothSocket.isConnected()) {
                // start bluetooth connection
                activityStart.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if(activityStart.bluetoothAdapter != null && activityStart.bluetoothAdapter.isEnabled()) {
                    activityStart.bluetoothObd2Device = activityStart.bluetoothAdapter.getRemoteDevice("00:00:00:08:89:31");
                    UUID bluetoothDeviceObd2Uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                    try {
                        activityStart.bluetoothSocket = activityStart.bluetoothObd2Device.createInsecureRfcommSocketToServiceRecord(bluetoothDeviceObd2Uuid);
                    } catch (Exception exc) {
                        Log.d("error", "error bluetooth 2: " + exc.getMessage());
                    }
                }
            }
        } catch (Exception exc) {
            Log.d("error", "error bluetooth 1: " + exc.getMessage());
        }
    }
    // endregion

    // region helper methods
    private void start() {
        startCheckingActiveDrive();
        startGetLastData();
        //startEnableBluetoothConnection();
    }

    private void startCheckingActiveDrive() {
        try {
            // check for existing active drive
            List<FahrtItem> resultItems = Database.getInstance(this).getAll();

            // get last item
            int maxIndex = -1;
            for(int i = 0;i < resultItems.size();i += 1) {
                if(resultItems.get(i).getId() >= maxIndex) {
                    maxIndex = i;
                }
            }

            //maxIndex = resultItems.get(maxIndex).getId();   // überflüssig ?
            FahrtItem lastFahrtItem = resultItems.get(maxIndex);

            // check if fahrt ends
            if(lastFahrtItem != null && lastFahrtItem.getEndTime() == null) {
                // drive not ended
                this.driveEnded = false;
                this.driveStarted = true;

                // buttons
                ((Button) findViewById(R.id.btnStartDrive)).setEnabled(false);
                ((Button) findViewById(R.id.btnEndDrive)).setEnabled(true);

                // last km
                //this.lastKmstand = Integer.valueOf(lastFahrtItem.getStartKmstand());
                this.lastKmstand = lastFahrtItem.getStartKmstand();
                ((EditText) findViewById(R.id.etKmStand)).setText(String.valueOf(this.lastKmstand));
            } else {
                // no last drive existing
                Log.d("warning", Errors.warning_checking_on_start_for_existing_drive.getErrorText());
            }
        } catch(Exception exc) {
            Log.d("error", "error: " + exc.getMessage());
        }
    }

    static void updateAddressField() {
        if(getAddressListFromGeocoder() != null && mLocation != null) {
            activityStart.etAdressField.setText(getAddressListFromGeocoder().get(0).getPostalCode() + " " + getAddressListFromGeocoder().get(0).getLocality() + ", " + getAddressListFromGeocoder().get(0).getAddressLine(0));
        }


    }

    static void updateLocationText() {
        activityStart.tvLocation.setText("Letztes GPS-Update: " + (new SimpleDateFormat("HH:mm:ss dd.MM.yyyy")).format(new Date()));
    }

    private void hideSoftwareKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static List<Address> getAddressListFromGeocoder()  {
        List<android.location.Address> addresses;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
        } catch (IOException ioe) {
            Log.d("error", Errors.no_addresses_available.getErrorText());

            Toast.makeText(context, "geocoder -> IOException error", Toast.LENGTH_LONG).show();

            // try to restart the service
            stopGoogleApiClient();
            startGoogleApiClient();

            Toast.makeText(context, "location services restartet", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d("error", e.getMessage());
        }

        return addressList;
    }

    public void checkPermissions() {
        // permission internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.INTERNET)) {
                //
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.INTERNET }, PERMISSION_INTERNET);
            }
        }

        // permission sdcard
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //
            } else {
                // request
                ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }

        // permission fine location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //
            } else {
                // request
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            }
        }

        // permission coarse location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //
            } else {
                // request
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
            }
        }
    }
    // endregion

    // region handle location services
    private void startGetLastData() {
        try {
            // check for existing active drive
            List<FahrtItem> resultItems = Database.getInstance(this).getAll();

            // get last item
            int maxIndex = -1;
            for(int i = 0;i < resultItems.size();i += 1) {
                if(resultItems.get(i).getId() >= maxIndex) {
                    maxIndex = i;
                }
            }

            if(maxIndex >= 0) {
                //maxIndex = resultItems.get(maxIndex).getId();   // überflüssig ?
                FahrtItem lastFahrtItem = resultItems.get(maxIndex);

                if(lastFahrtItem != null) {
                    // last kmstand
                    if(lastFahrtItem.getEndKmstand() != null) {
                        ((EditText) findViewById(R.id.etKmStand)).setText(String.valueOf(lastFahrtItem.getEndKmstand()));
                    } else {
                        // try start km stand
                        if(lastFahrtItem.getStartKmstand() != null) {
                            ((EditText) findViewById(R.id.etKmStand)).setText(String.valueOf(lastFahrtItem.getStartKmstand()));
                        } else {
                            //
                        }
                    }

                    // last car
                    if(lastFahrtItem.getEndCar() != null) {
                        ((EditText) findViewById(R.id.etCar)).setText(String.valueOf(lastFahrtItem.getEndCar()));
                    }else {
                        // try start car
                        if(lastFahrtItem.getStartCar() != null) {
                            ((EditText) findViewById(R.id.etCar)).setText(String.valueOf(lastFahrtItem.getStartCar()));
                        } else {
                            //
                        }
                    }
                } else {
                    // not possible to determine
                }
            } else {
                Log.d("warning", Errors.could_not_load_last_drive.getErrorText());
            }
        } catch (Exception exc) {
            Log.d("error", "error: " + exc.getMessage());
        }
    }

    private static void startGoogleApiClient() {
        try {
            // builder
            GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient.Builder(context);
            googleApiClientBuilder.addConnectionCallbacks(mConnectionFallbacks);
            googleApiClientBuilder.addOnConnectionFailedListener(mOnConnectionFailedListener);
            googleApiClientBuilder.addApi(LocationServices.API);

            // get client
            mGoogleApiClient = googleApiClientBuilder.build();

            // location request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mGoogleApiClient.connect();
            Log.i("info", "google api client connected");

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

            updateLocationText();
            updateAddressField();


        } catch (Exception e) {
            Log.d("error", "cannot connect to google api client");
        }
    }

    private static void stopGoogleApiClient() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();
            Log.i("info", "google api client disconnected");
        } catch (Exception e) {
            Log.d("error", "cannot disconntect form google api client");
        }
    }
    // endregion

    // region button event methods
    public void btnClickStartDrive(View view) {
        // start drive
        // check km stand
        String kmstand = ((EditText) findViewById(R.id.etKmStand)).getText().toString();

        if(!kmstand.matches("")) {
            if(Double.valueOf(kmstand) >= this.lastKmstand) {
                // check if table exists
                if(!Database.getInstance(this).checkIfTableExists(Database.T_FAHRT.TABLE_NAME)) {
                    // create table first
                    Database.getInstance(this).createTable();
                }

                // change buttons editable
                ((Button) findViewById(R.id.btnStartDrive)).setEnabled(false);
                ((Button) findViewById(R.id.btnEndDrive)).setEnabled(true);

                // disable keyboard
                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(EditTextName.getWindowToken(), 0);

                // make entry in the db
                FahrtItem tmpItem = new FahrtItem();

                // date
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");

                // town & adress
                String town = "";
                String address = "";

                if(this.getAddressListFromGeocoder() != null) {
                    address = this.getAddressListFromGeocoder().get(0).getAddressLine(0);
                    town = this.getAddressListFromGeocoder().get(0).getPostalCode() + "," + this.getAddressListFromGeocoder().get(0).getLocality();
                }

                try {
                    String ortszusatz = ((EditText) findViewById(R.id.etOrtszusatz)).getText().toString();
                    Boolean privateFahrt = ((CheckBox) findViewById(R.id.cbPrivateFahrt)).isChecked();
                    String car = ((EditText) findViewById(R.id.etCar)).getText().toString();

                    tmpItem.setStartFields(currentDate.format(new Date()), currentTime.format(new Date()), town, address, Double.valueOf(kmstand), mLocation.getLatitude(), mLocation.getLongitude(), ortszusatz, privateFahrt, car);
                } catch (Exception exc) {
                    Log.d("error", exc.getMessage());
                }

                long returnNumber = -1;

                try {
                    returnNumber = Database.getInstance(this).insertSingleItem(tmpItem);
                    Log.d("info", "return number = " + Long.toString(returnNumber));

                    Toast.makeText(activityStart.this, "Fahrt gestartet ...", Toast.LENGTH_LONG).show();
                    // toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
                    this.driveStarted = true;
                    this.driveEnded = false;
                    this.lastKmstand = Integer.valueOf(kmstand);
                } catch (Exception exc) {
                    Log.d("error", Errors.inserting_fahrtitem_not_possible + " (" + exc.getMessage() + ")");
                }
            } else {
                Toast.makeText(this, "KM Stand zu niedrig!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activityStart.this, "KM Stand eingeben!", Toast.LENGTH_LONG).show();
        }
    }

    public void btnClickEndDrive(View view) {
        String kmstand = ((EditText) findViewById(R.id.etKmStand)).getText().toString();

        if(!kmstand.matches("")) {
            if(Double.valueOf(kmstand) >= this.lastKmstand) {
                // check last row incomplete
                List<FahrtItem> resultItems = Database.getInstance(this).getAll();

                // get max id
                int maxId = -1;
                for(int i = 0;i < resultItems.size();i++) {
                    if(resultItems.get(i).getId() >= maxId) {
                        maxId = i;  // i, not id
                    }
                }

                // max id
                FahrtItem lastDriveItem = resultItems.get(maxId);

                // end drive

                // date
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");

                // town & adress
                List<android.location.Address> addresses;
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                String town = "";
                String address = "";

                try {
                    addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                    address = addresses.get(0).getAddressLine(0);
                    town = addresses.get(0).getPostalCode() + "," + addresses.get(0).getLocality();
                } catch (IOException exc) {
                    Log.d("error", Errors.no_addresses_available.getErrorText());
                }

                String ortszusatz = ((EditText) findViewById(R.id.etOrtszusatz)).getText().toString();
                Boolean privateFahrt = ((CheckBox) findViewById(R.id.cbPrivateFahrt)).isChecked();
                String car = ((EditText) findViewById(R.id.etCar)).getText().toString();

                try {
                    // update item and db
                    //lastDriveItem.setEndFields(currentDate.format(new Date()), currentTime.format(new Date()), town, address, Double.valueOf(kmstand));
                    lastDriveItem.setEndFields(currentDate.format(new Date()), currentTime.format(new Date()), town, address, Double.valueOf(kmstand), mLocation.getLatitude(), mLocation.getLongitude(), ortszusatz, privateFahrt, car);
                    long returnId = Database.getInstance(this).updateRowWithId(lastDriveItem.getId(), lastDriveItem);
                    Toast.makeText(activityStart.this, "Fahrt beendet ...", Toast.LENGTH_LONG).show();

                    // disable button
                    ((Button) findViewById(R.id.btnEndDrive)).setEnabled(false);
                    ((Button) findViewById(R.id.btnStartDrive)).setEnabled(true);

                    //
                    this.driveEnded = true;
                    this.driveStarted = false;
                    this.lastKmstand = Integer.valueOf(kmstand);
                } catch (Exception exc) {
                    Log.d("error", Errors.updating_table_t_fahrt + ": " + exc.getMessage());
                }
            } else {
                Toast.makeText(activityStart.this, "KM Stand zu niedrig", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activityStart.this, "KM Stand eingeben!", Toast.LENGTH_LONG).show();
        }
    }

    // endregion

    // region thread methods
    private void startTimeThread() {
        Thread timeThread = new Thread() {
            public void run() {
                while(true) {

                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SimpleDateFormat currentTime = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                                    activityStart.etTimeDateField.setText(currentTime.format(new Date()));
                                } catch (Exception exc) {
                                    Log.d("error", Errors.time_thread_date_format_error.getErrorText());
                                }
                            }
                        });

                        Thread.sleep(999);
                    } catch (InterruptedException exc) {
                        Log.d("error", Errors.thread_sleep_not_functional.getErrorText());
                    } catch (Exception exc) {
                        Log.d("error", "error in thread: " + exc.getMessage());
                    }
                }
            }
        };
        timeThread.start();
    }

    private void startBluetoothThread() {
        Thread bluetoothThread = new Thread() {
            public void run() {
                while(activityStart.runningBluetoothThread) {
                    // start connection
                    activityStart.startEnableBluetoothConnection();

                    try {
                        // connect to device
                        activityStart.bluetoothSocket.connect();

                        if(activityStart.bluetoothSocket.isConnected()) {
                            Log.d("info", "connection established");
                        } else {
                            Log.d("info", "connection not established");
                        }

                        for(int i=0;i < 10;i++) {
                            try {
                                RPMCommand c = new RPMCommand();
                                c.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                                Log.d("obd2", "rpms: " + c.getFormattedResult());

                                (new Toast(activityStart.context)).makeText(activityStart.context, "rpms: " + c.getFormattedResult(), Toast.LENGTH_LONG).show();

                                RuntimeCommand c2 = new RuntimeCommand();
                                c.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                                Log.d("obd2", "runtime: " + c.getFormattedResult());

                                (new Toast(activityStart.context)).makeText(activityStart.context, "runtime: " + c2.getFormattedResult(), Toast.LENGTH_LONG).show();

                                ThrottlePositionCommand c3 = new ThrottlePositionCommand();
                                c3.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                                Log.d("obd2", "throttle: " + c.getFormattedResult());

                                (new Toast(activityStart.context)).makeText(activityStart.context, "throttle: " + c3.getFormattedResult(), Toast.LENGTH_LONG).show();

                                Thread.sleep(500);
                            } catch (InterruptedException exc) {
                                Log.d("error", Errors.thread_sleep_not_functional + ": " + exc.getMessage());
                            } catch (Exception exc) {
                                Log.d("error", "error bluetooth 6: " + exc.getMessage());
                            }
                        }

                        Log.d("info", "connected to socket");
                    } catch(Exception exc) {
                        Log.d("error", "error bluetooth 3: " + exc.getMessage());
                    } finally {
                        // close connection, if established
                        try {
                            activityStart.bluetoothSocket.close();
                        } catch (IOException ioexc) {
                            Log.d("error", "error bluetooth 4: " + ioexc.getMessage());
                        } catch (Exception exc) {
                            Log.d("error", Errors.could_not_close_bluetooth_connection + ": " + exc.getMessage());
                        }
                    }

                    // wait a little bit
                    try {
                        Thread.sleep(1000);
                    } catch (Exception exc) {
                        Log.d("error", "error bluetooth 5: " + exc.getMessage());
                    }
                }
            }
        };
        bluetoothThread.start();
    }
    // endregion

    // region override methods
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "PERMISSION_INTERNET granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "PERMISSION_INTERNET denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "PERMISSION_WRITE_EXTERNAL_STORAGE granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "PERMISSION_WRITE_EXTERNAL_STORAGE denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "PERMISSION_ACCESS_FINE_LOCATION granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "PERMISSION_ACCESS_FINE_LOCATION denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISSION_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "PERMISSION_ACCESS_COARSE_LOCATION granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "PERMISSION_ACCESS_COARSE_LOCATION denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_start);

        // set context
        context = this;

        // permissions
        this.checkPermissions();

        // static views
        activityStart.tvLocation = (TextView) findViewById(R.id.tvLocationInfo);
        activityStart.etTimeDateField = (EditText) findViewById(R.id.etDatumUhrzeit);
        activityStart.etAdressField = (EditText) findViewById(R.id.etOrtAdresse);
        activityStart.etKmStand = (EditText) findViewById(R.id.etKmStand);

        // bluetooth
        activityStart.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // end stop button
        ((Button) findViewById(R.id.btnEndDrive)).setEnabled(false);

        // main start
        this.start();

        // time thread
        this.startTimeThread();

        // bluetooth thread
        //this.startBluetoothThread();
    }

    @Override
    public void onStop() {
        this.stopped = true;
        this.stopGoogleApiClient();

        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        this.calledOnRestart = true;
    }

    @Override
    public void onPause() {
        this.paused = true;
        this.stopGoogleApiClient();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //this.setAppStatus("back pressed");
    }

    @Override
    public void onResume() {
        super.onResume();

        if(this.paused) {
            //this.setAppStatus("paused, resumed");
            this.paused = false;
        }

        if(this.stopped && this.calledOnRestart) {
            //this.setAppStatus("stopped, restarted and resumed");
            this.stopped = false;
            this.calledOnRestart = false;

            this.start();
        }

        this.startGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();

        this.startGoogleApiClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemActivityStart:
                Intent intentMain = new Intent(this, activityStart.class);
                startActivity(intentMain);

                return true;

            case R.id.menuItemActivityGps:
                Intent intentGps = new Intent(this, activityGps.class);
                startActivity(intentGps);

                return true;

            case R.id.menuItemActivityDatabase:
                Intent intentDb = new Intent(this, activityDatabase.class);
                startActivity(intentDb);

                return true;

            case R.id.menuItemActivityConfig:
                Intent intentConfig = new Intent(this, activityConfig.class);
                startActivity(intentConfig);

                return true;

            case R.id.menuItemActivityPOI:
                Intent intentPoi = new Intent(this, activityPOIs.class);
                startActivity(intentPoi);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // endregion
}