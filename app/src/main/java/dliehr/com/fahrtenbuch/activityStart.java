package dliehr.com.fahrtenbuch;

import android.Manifest;
import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.location.Address;
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
import com.github.pires.obd.commands.engine.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

// google geolocation api key
// name: server_key_fahrtenbuch
// key: AIzaSyDFfDvQ-h8XaY3ZqDgooEOW38Aj9oEAf5Q

public class activityStart extends AppCompatActivity {
    // app
    private static final String TAG = activityStart.class.getSimpleName();

    // app lifetime cycle
    private Boolean paused = false, stopped = false, calledOnRestart = false;
    private Boolean overrideVoidOnCreateCalled = false;
    private Boolean overrideVoidOnStartCalled = false;
    private Boolean overrideVoidOnResumeCalled = false;

    // permissions
    private static final int PERMISSIONS_INTERNET_SD_FINE_COARSE_LOCATION = 4;

    // gps
    static Location mLocation = null;
    static GoogleApiClient mGoogleApiClient = null;
    static LocationRequest mLocationRequest = null;
    static LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;

            Log.i(TAG, "Location changed (onLocationChanged)");

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

                    Log.i(TAG, "location update (onConnected)");
                } catch (SecurityException se) {
                    //Toast.makeText(context, "getting location not allowed", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    //Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "ConnectionCallbacks, onConnectionSuspended");
        }
    };

    private static GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e(TAG, "OnConnectionFailedListener, connection failed");
        }
    };

    static final long UPDATE_INTERVAL = 1000;
    static final long FASTEST_UPDATE_INTERVAL = 500;
    private static final String LOCATION_API_KEY = "AIzaSyDFfDvQ-h8XaY3ZqDgooEOW38Aj9oEAf5Q";

    // static views
    static TextView tvLocation = null;
    static EditText etAdressField = null;
    static EditText etTimeDateField = null;
    static EditText etKmStand = null;
    static EditText etAdditionalInfo = null;

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
            List<FahrtItem> resultItems = Database.getInstance(this).getAllFromT_FAHRT();

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

    private static PointOfInterest mPoiFromFoundAddress = null;
    private static Address findAddressInCache() {
        if(mLocation != null) {
            Float delta = 0.0f;
            List<PointOfInterest> points = PointOfInterest.getPoints(context);

            for(int i=0;i < points.size();i++) {
                delta = mLocation.distanceTo(points.get(i).getLocation());

                // within 31 meters
                if(delta < 31) {
                    mPoiFromFoundAddress = points.get(i);
                    return points.get(i).getAddress();
                }
            }
        }

        mPoiFromFoundAddress = null;
        return null;
    }

    static void updateAddressField() {
        // first, look in cache
        Address currentAddress = null;

        try {
            currentAddress = findAddressInCache();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if(currentAddress == null) {
            RetrieveAddress retrieveAddress = new RetrieveAddress(mLocation);
            retrieveAddress.execute(mLocation);
            retrieveAddress.waitForTaskFinish();
            currentAddress = retrieveAddress.getAddress();
        } else {
            Log.i(TAG, "take address from cache");
        }

        if(currentAddress != null) {
            try {
                etAdressField.setText(currentAddress.getPostalCode() + " " + currentAddress.getLocality() + ", " + currentAddress.getAddressLine(0));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                etAdditionalInfo.setText(mPoiFromFoundAddress.getAdditionalInfo());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        } else {
            Log.e(TAG, "void updateAddressField(): currentAddress is null");
        }
    }

    static void updateLocationText() {
        activityStart.tvLocation.setText("Letztes GPS-Update: " + (new SimpleDateFormat("HH:mm:ss dd.MM.yyyy")).format(new Date()));
    }

    private void hideSoftwareKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void checkPermissions() {
        // permissions INTERNET, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
        Boolean permissionCheckInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED;
        Boolean permissionCheckWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        Boolean permissionCheckAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        Boolean permissionCheckAccesCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        Boolean permissionCheckBluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED;

        if (permissionCheckInternet || permissionCheckWriteExternalStorage || permissionCheckAccessFineLocation || permissionCheckAccesCoarseLocation || permissionCheckBluetooth) {
            Boolean permissionRationaleInternet = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.INTERNET);
            Boolean permissionRationaleWriteExternalStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Boolean permissionRationaleAccessFineLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
            Boolean permissionRationaleAccessCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            Boolean permissionRationaleBluetooth = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH);

            if (permissionRationaleInternet || permissionRationaleWriteExternalStorage || permissionRationaleAccessFineLocation || permissionRationaleAccessCoarseLocation || permissionRationaleBluetooth) {
                //
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH }, PERMISSIONS_INTERNET_SD_FINE_COARSE_LOCATION);
            }
        }
    }
    // endregion

    // region handle location services
    private void startGetLastData() {
        try {
            // check for existing active drive
            List<FahrtItem> resultItems = Database.getInstance(this).getAllFromT_FAHRT();

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

            Log.i(TAG, "Google play services version = " + String.valueOf(context.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode));

            // location request
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mGoogleApiClient.connect();
            Log.i("info", "google api client connected");

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        } catch(SecurityException se) {
            Log.d("error", "security exception: " + Errors.gettings_gps_location_not_allowed + ": " + se.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
                    Database.getInstance(this).createTableT_FAHRT();
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
                RetrieveAddress retrieveAddress = new RetrieveAddress(mLocation);
                retrieveAddress.execute(mLocation);
                retrieveAddress.waitForTaskFinish();
                Address currentAddress = retrieveAddress.getAddress();
                String address = "";
                String town = "";

                if(currentAddress != null) {
                    address = currentAddress.getAddressLine(0);
                    town = currentAddress.getPostalCode() + "," + currentAddress.getLocality();
                } else {
                    Log.d("error", "void btnClickStartDrive(): currentAddress is null");
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
                    //returnNumber = Database.getInstance(this).insertSingleItem(tmpItem);
                    returnNumber = Database.getInstance(this).insertSingleItemIntoT_FAHRT(tmpItem);

                    Log.d("info", "return number = " + Long.toString(returnNumber));

                    Toast.makeText(activityStart.this, "Fahrt gestartet ...", Toast.LENGTH_LONG).show();
                    this.hideSoftwareKeyboard(view);
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
                List<FahrtItem> resultItems = Database.getInstance(this).getAllFromT_FAHRT();

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
                RetrieveAddress retrieveAddress = new RetrieveAddress(mLocation);
                retrieveAddress.execute(mLocation);
                retrieveAddress.waitForTaskFinish();
                Address currentAddress = retrieveAddress.getAddress();
                String address = "";
                String town = "";

                if(currentAddress != null) {
                    address = currentAddress.getAddressLine(0);
                    town = currentAddress.getPostalCode() + "," + currentAddress.getLocality();
                } else {
                    Log.d("error", "void btnClickStartDrive(): currentAddress is null");
                }

                String ortszusatz = ((EditText) findViewById(R.id.etOrtszusatz)).getText().toString();
                Boolean privateFahrt = ((CheckBox) findViewById(R.id.cbPrivateFahrt)).isChecked();
                String car = ((EditText) findViewById(R.id.etCar)).getText().toString();

                try {
                    // update item and db
                    //lastDriveItem.setEndFields(currentDate.format(new Date()), currentTime.format(new Date()), town, address, Double.valueOf(kmstand));
                    lastDriveItem.setEndFields(currentDate.format(new Date()), currentTime.format(new Date()), town, address, Double.valueOf(kmstand), mLocation.getLatitude(), mLocation.getLongitude(), ortszusatz, privateFahrt, car);
                    //long returnId = Database.getInstance(this).updateRowWithId(lastDriveItem.getId(), lastDriveItem);
                    long returnId = Database.getInstance(this).updateRowWithIdFromTableT_FAHRT(lastDriveItem.getId(), lastDriveItem);
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

    public void btnClickAddPoi(View view) {
        /*
        Database.getInstance(this).dropTable(Database.T_POI.TABLE_NAME);
        Database.getInstance(this).createTableT_POI();

        List<PointOfInterest> points = PointOfInterest.getPoints(this);

        for(PointOfInterest poi : points) {
            if(Database.getInstance(this).insertSingleItemIntoT_POI(poi) >= 0) {
                Log.i(TAG, "poi eingefügt");
            } else {
                Log.i(TAG, "poi nicht eingefügt");
            }
        }
        */
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
        Thread locationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startGoogleApiClient();
            }
        });

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
            case PERMISSIONS_INTERNET_SD_FINE_COARSE_LOCATION: {
                if(grantResults.length > 0) {
                    for(int i = 0;i < 4;i++) {
                        switch (i) {
                            case 0: { // internet
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    //Toast.makeText(this, "permission INTERNET granted", Toast.LENGTH_LONG).show();
                                } else {
                                    //Toast.makeText(this, "permission INTERNET _not_ granted", Toast.LENGTH_LONG).show();
                                }

                                break;
                            }

                            case 1: { // write external storage
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    //Toast.makeText(this, "permission WRITE_EXTERNAL_STORAGE granted", Toast.LENGTH_LONG).show();
                                } else {
                                    //Toast.makeText(this, "permission WRITE_EXTERNAL_STORAGE _not_ granted", Toast.LENGTH_LONG).show();
                                }

                                break;
                            }

                            case 2: { // access fine location
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    //Toast.makeText(this, "permission ACCESS_FINE_LOCATION granted", Toast.LENGTH_LONG).show();
                                } else {
                                    //Toast.makeText(this, "permission ACCESS_FINE_LOCATION _not_ granted", Toast.LENGTH_LONG).show();
                                }

                                break;
                            }

                            case 3: { // access coarse location
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    //Toast.makeText(this, "permission ACCESS_COARSE_LOCATION granted", Toast.LENGTH_LONG).show();
                                } else {
                                    //Toast.makeText(this, "permission ACCESS_COARSE_LOCATION _not_ granted", Toast.LENGTH_LONG).show();
                                }

                                break;
                            }
                        }
                    }
                }

                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_start);

        // lifetime
        this.overrideVoidOnCreateCalled = true;

        // set context
        context = this;

        // permissions
        this.checkPermissions();

        // gps
        this.startLocationThread();

        // static views
        tvLocation = (TextView) findViewById(R.id.tvLocationInfo);
        etTimeDateField = (EditText) findViewById(R.id.etDatumUhrzeit);
        etAdressField = (EditText) findViewById(R.id.etOrtAdresse);
        etKmStand = (EditText) findViewById(R.id.etKmStand);
        etAdditionalInfo = (EditText) findViewById(R.id.etOrtszusatz);

        // bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // end stop button
        ((Button) findViewById(R.id.btnEndDrive)).setEnabled(false);

        // main start
        this.start();

        // time thread
        this.startTimeThread();

        // bluetooth thread
        //this.startBluetoothThread();

        // try the new method
        PointOfInterest.removePoi(this, 1);
    }

    @Override
    public void onStop() {
        // app lifetime cycle
        this.overrideVoidOnStartCalled = false;
        this.overrideVoidOnCreateCalled = false;
        this.overrideVoidOnResumeCalled = false;
        this.stopped = true;
        this.stopGoogleApiClient();

        super.onStop();
    }

    @Override
    public void onRestart() {
        // app lifetime cycle
        this.overrideVoidOnStartCalled = false;
        this.overrideVoidOnCreateCalled = false;
        this.overrideVoidOnResumeCalled = false;
        this.calledOnRestart = true;

        super.onRestart();
    }

    @Override
    public void onPause() {
        // app lifetime cycle
        this.overrideVoidOnStartCalled = false;
        this.overrideVoidOnCreateCalled = false;
        this.overrideVoidOnResumeCalled = false;

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

        // app lifetime cycle
        this.overrideVoidOnResumeCalled = true;

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

        if(!overrideVoidOnCreateCalled) {
            this.startGoogleApiClient();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // app lifetime cycle
        this.overrideVoidOnStartCalled = true;

        if(!overrideVoidOnCreateCalled) {
            this.startGoogleApiClient();
        }
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