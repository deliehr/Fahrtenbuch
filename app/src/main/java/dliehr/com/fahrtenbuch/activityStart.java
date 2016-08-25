package dliehr.com.fahrtenbuch;

import android.bluetooth.*;
import android.location.Geocoder;
import android.location.Location;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.content.*;
import android.util.Log;
import android.view.*;
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

public class activityStart extends AppCompatActivity {
    // app lifetime cycle
    private Boolean paused = false, stopped = false, calledOnRestart = false;

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
    static final long UPDATE_INTERVAL = 1000;
    static final long FASTEST_UPDATE_INTERVAL = 500;

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
        String addressInfo = "";

        // adress
        List<android.location.Address> addresses;
        Geocoder geocoder = null;

        try {
            if(mLocation != null) {
                geocoder = new Geocoder(context, Locale.getDefault());
                addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);

                addressInfo += addresses.get(0).getPostalCode() + " " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAddressLine(0);

                activityStart.etAdressField.setText(addressInfo);
            } else {
                Log.d("error", "location null");
            }
        } catch (IOException exc) {
            Log.d("error", Errors.no_addresses_available.getErrorText() + ": " + exc.getMessage());
        } catch (Exception exc) {
            Log.d("error", exc.getMessage());
        }
    }

    static void updateLocationText() {
        activityStart.tvLocation.setText("Letztes GPS-Update: " + (new SimpleDateFormat("HH:mm:ss dd.MM.yyyy")).format(new Date()));
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

    static int requestLocationUpdatesCounter = 0;

    static void initLocation() {
        try {
            GoogleApiClient.ConnectionCallbacks connectionFallbacks = new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    if(mLocation == null) {
                        try {
                            requestLocationUpdatesCounter++;
                            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

                            Log.i("info", "requestLocationUpdatesCounter = " + requestLocationUpdatesCounter);

                            //Toast.makeText(context, "location update (onConnected)", Toast.LENGTH_SHORT).show();
                            Log.i("info", "location update (onConnected)");

                            updateAddressField();
                            updateLocationText();
                        } catch (SecurityException se) {
                            Toast.makeText(context, "getting location not allowed", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                    mGoogleApiClient.connect();
                }
            };

            GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Toast.makeText(context, "connection failed", Toast.LENGTH_SHORT).show();
                }
            };



            // builder
            GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient.Builder(context);
            googleApiClientBuilder.addConnectionCallbacks(connectionFallbacks);
            googleApiClientBuilder.addOnConnectionFailedListener(onConnectionFailedListener);
            googleApiClientBuilder.addApi(LocationServices.API);

            // get client
            mGoogleApiClient = googleApiClientBuilder.build();

            // location request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // connect
            mGoogleApiClient.connect();
        } catch (Exception e) {
            Log.d("error", e.getMessage());
        }
    }

    private void startGoogleApiClient() {
        try {
            mGoogleApiClient.connect();
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

            updateLocationText();
            updateAddressField();

            Log.i("info", "google api client connected");
        } catch (Exception e) {
            Log.d("error", "cannot connect to google api client");
        }
    }

    private void stopGoogleApiClient() {
        try {
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

                    Toast.makeText(activityStart.this, "Fahrt gestartet ...", Toast.LENGTH_SHORT).show();
                    // toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
                    this.driveStarted = true;
                    this.driveEnded = false;
                    this.lastKmstand = Integer.valueOf(kmstand);
                } catch (Exception exc) {
                    Log.d("error", Errors.inserting_fahrtitem_not_possible + " (" + exc.getMessage() + ")");
                }
            } else {
                Toast.makeText(this, "KM Stand zu niedrig!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activityStart.this, "KM Stand eingeben!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(activityStart.this, "Fahrt beendet ...", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(activityStart.this, "KM Stand zu niedrig", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activityStart.this, "KM Stand eingeben!", Toast.LENGTH_SHORT).show();
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

    private void startLocationThread() {
        Thread locationThread = new Thread() {
            public void run() {
                // init location
                initLocation();
            }
        };
        locationThread.start();
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

                                (new Toast(activityStart.context)).makeText(activityStart.context, "rpms: " + c.getFormattedResult(), Toast.LENGTH_SHORT).show();

                                RuntimeCommand c2 = new RuntimeCommand();
                                c.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                                Log.d("obd2", "runtime: " + c.getFormattedResult());

                                (new Toast(activityStart.context)).makeText(activityStart.context, "runtime: " + c2.getFormattedResult(), Toast.LENGTH_SHORT).show();

                                ThrottlePositionCommand c3 = new ThrottlePositionCommand();
                                c3.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                                Log.d("obd2", "throttle: " + c.getFormattedResult());

                                (new Toast(activityStart.context)).makeText(activityStart.context, "throttle: " + c3.getFormattedResult(), Toast.LENGTH_SHORT).show();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_start);

        // set context
        context = this;

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

        // location thread
        this.startLocationThread();

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // endregion
}