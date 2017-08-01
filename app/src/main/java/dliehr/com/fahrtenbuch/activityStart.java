package dliehr.com.fahrtenbuch;

import android.Manifest;
import android.bluetooth.*;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.*;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.*;
import android.util.Log;

import android.view.inputmethod.InputMethodManager;
import android.view.*;
import android.widget.*;
import com.github.pires.obd.commands.engine.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

// google geolocation api key
// name: androidApp
// key: AIzaSyAXmHMgXAxcA62ZHztzoUs5AHlOtTwxVzI

// from another mac os systems :-)

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
            Log.e(TAG, "connection failed: " + connectionResult.getErrorMessage());
        }
    };

    static final long UPDATE_INTERVAL = 1000;
    static final long FASTEST_UPDATE_INTERVAL = 500;
    private static final String LOCATION_API_KEY = "AIzaSyAXmHMgXAxcA62ZHztzoUs5AHlOtTwxVzI";

    // static views
    static TextView tvLocation = null;
    static EditText etPlaceAddress = null;
    static EditText etTimeDateField = null;
    static EditText etKilometers = null;
    static EditText etAdditionalInfo = null;
    static CheckBox cbPrivateDrive = null;
    static CheckBox cbPlaceAddress = null;
    static CheckBox cbAdditionalInfo = null;

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
                ((EditText) findViewById(R.id.etKilometers)).setText(String.valueOf(this.lastKmstand));
            } else {
                // no last drive existing
                Log.d("warning", Errors.warning_checking_on_start_for_existing_drive.getErrorText());
            }
        } catch(Exception exc) {
            Log.e(TAG, "error: " + exc.getMessage());
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
        Address currentAddress = null;

        // check if checkboxes are checked
        if(!cbPlaceAddress.isChecked()) {
            // first, look in cache
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
            }

            if(currentAddress != null) {
                try {
                    String placeAddressToSet = currentAddress.getPostalCode()
                            + " " + currentAddress.getLocality() +
                            ", " + currentAddress.getAddressLine(0);

                    etPlaceAddress.setText(placeAddressToSet);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                Log.e(TAG, "void updateAddressField(): currentAddress is null");
            }
        }

        if(!cbAdditionalInfo.isChecked()) {
            if(currentAddress == null) {
                // lookup in cache
                try {
                    currentAddress = findAddressInCache();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                // found in cache?
                if(currentAddress == null) {
                    RetrieveAddress retrieveAddress = new RetrieveAddress(mLocation);
                    retrieveAddress.execute(mLocation);
                    retrieveAddress.waitForTaskFinish();
                    currentAddress = retrieveAddress.getAddress();
                } else {
                }
            }

            // set text
            if(currentAddress != null) {
                try {
                    etAdditionalInfo.setText(mPoiFromFoundAddress.getAdditionalInfo());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

            } else {
                Log.e(TAG, "void updateAddressField(): currentAddress is null");
            }
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
                        ((EditText) findViewById(R.id.etKilometers)).setText(String.valueOf(lastFahrtItem.getEndKmstand()));
                    } else {
                        // try start km stand
                        if(lastFahrtItem.getStartKmstand() != null) {
                            ((EditText) findViewById(R.id.etKilometers)).setText(String.valueOf(lastFahrtItem.getStartKmstand()));
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
            Log.e(TAG, "error: " + exc.getMessage());
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

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener, Looper.getMainLooper());
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
        String kmstand = ((EditText) findViewById(R.id.etKilometers)).getText().toString();

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

                // set up data
                try {
                    // date
                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");

                    // town / locality
                    String placeAddress = "";
                    try {
                        placeAddress = etPlaceAddress.getText().toString().split(",")[0].split(" ")[0] + "," + etPlaceAddress.getText().toString().split(",")[0].split(" ")[1];
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                    String addressLine = "";

                    try {
                        addressLine = etPlaceAddress.getText().toString().split(",")[1].trim();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                    // additional info
                    String additionalInfo = ((EditText) findViewById(R.id.etAdditionalInfo)).getText().toString();

                    // private drive
                    Boolean privateDrive = ((CheckBox) findViewById(R.id.cbPrivateDrive)).isChecked();

                    // car
                    String car = ((EditText) findViewById(R.id.etCar)).getText().toString();

                    tmpItem.setStartFields(
                            currentDate.format(new Date()),
                            currentTime.format(new Date()),
                            placeAddress,
                            addressLine,
                            Double.valueOf(kmstand),
                            mLocation.getLatitude(),
                            mLocation.getLongitude(),
                            additionalInfo,
                            privateDrive,
                            car
                    );
                } catch (Exception exc) {
                    Log.d("error", exc.getMessage());
                }

                long returnNumber = -1;

                try {
                    returnNumber = Database.getInstance(this).insertSingleItemIntoT_FAHRT(tmpItem);

                    Log.d("info", "return number = " + Long.toString(returnNumber));

                    Toast.makeText(activityStart.this, "Fahrt gestartet.", Toast.LENGTH_SHORT).show();
                    this.hideSoftwareKeyboard(view);
                    this.driveStarted = true;
                    this.driveEnded = false;
                    this.lastKmstand = Double.valueOf(kmstand);
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
        String kmstand = ((EditText) findViewById(R.id.etKilometers)).getText().toString();

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
                try {
                    // update item and db
                    // set up data

                    // date
                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");

                    // town / locality
                    String placeAddress = "";
                    try {
                        placeAddress = etPlaceAddress.getText().toString().split(",")[0].split(" ")[0] + "," + etPlaceAddress.getText().toString().split(",")[0].split(" ")[1];
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                    String addressLine = "";
                    try {
                        addressLine = etPlaceAddress.getText().toString().split(",")[1].trim();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }

                    String additionalInfo = etAdditionalInfo.getText().toString();
                    Boolean privateDrive = cbPrivateDrive.isChecked();
                    String car = ((EditText) findViewById(R.id.etCar)).getText().toString();

                    lastDriveItem.setEndFields(
                            currentDate.format(new Date()),
                            currentTime.format(new Date()),
                            placeAddress,
                            addressLine,
                            Double.valueOf(kmstand),
                            mLocation.getLatitude(),
                            mLocation.getLongitude(),
                            additionalInfo,
                            privateDrive,
                            car
                    );

                    long returnId = Database.getInstance(this).updateRowWithIdFromTableT_FAHRT(lastDriveItem.getId(), lastDriveItem);
                    Toast.makeText(activityStart.this, "Fahrt beendet.", Toast.LENGTH_SHORT).show();

                    // disable / enable button
                    ((Button) findViewById(R.id.btnEndDrive)).setEnabled(false);
                    ((Button) findViewById(R.id.btnStartDrive)).setEnabled(true);

                    //
                    this.driveEnded = true;
                    this.driveStarted = false;
                    this.lastKmstand = Double.valueOf(kmstand);
                } catch (Exception exc) {
                    Log.d("error", Errors.updating_table_t_fahrt + ": " + exc.getMessage());
                }
            } else {
                Toast.makeText(activityStart.this, "KM Stand zu niedrig", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(activityStart.this, "KM Stand eingeben!", Toast.LENGTH_LONG).show();
        }

        // send last drive to webserver
        if(this.sendLastRowToServer()) {
            Toast.makeText(this, "Fahrt auf Server übertragen", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean sendLastRowToServer() {
        SendDriveToServer send = new SendDriveToServer(this);
        send.execute();
        send.waitForTaskFinish();

        Boolean returnValue = false;

        if(send.getResult().matches("true")) {
            returnValue = true;
        }

        return returnValue;
    }

    public void btnClickAddPoi(View view) {
        // add new poi
        // need: location, place address, additional information, private drive
        if(mLocation != null) {
            String addressLine = etPlaceAddress.getText().toString();
            String additionalInfo = etAdditionalInfo.getText().toString();
            Boolean privateDrive = cbPrivateDrive.isChecked();

            if(!addressLine.matches("") && !additionalInfo.matches("")) {
                // all information present, insert in db
                String locality = null;
                String postalCode = null;

                try {
                    locality = addressLine.split(",")[0].split(" ")[1];
                    postalCode = addressLine.split(",")[0].split(" ")[0];
                } catch (Exception e) {
                    Toast.makeText(this, "Error, POI not inserted! (" + e.getMessage() + ")", Toast.LENGTH_LONG).show();

                    locality = "";
                    postalCode = "";
                }

                try {
                    PointOfInterest tmpPoi = new PointOfInterest();
                    tmpPoi.setLatitude(mLocation.getLatitude());
                    tmpPoi.setLongitude(mLocation.getLongitude());
                    tmpPoi.setAddressLine(addressLine);
                    tmpPoi.setAdditionalInfo(additionalInfo);
                    tmpPoi.setPrivateDrive(privateDrive);
                    tmpPoi.setLocality(locality);
                    tmpPoi.setPostalCode(postalCode);

                    Database.getInstance(this).insertSingleItemIntoT_POI(tmpPoi);

                    Toast.makeText(this, "Point of interest eingefügt!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, "Error, POI not inserted! (" + e.getMessage() + ")", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Bitte Adresse und Ortszusatz angeben!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
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
        //this.startLocationThread();

        // static views
        tvLocation = (TextView) findViewById(R.id.tvLocationInfo);
        etTimeDateField = (EditText) findViewById(R.id.etDateTime);
        etPlaceAddress = (EditText) findViewById(R.id.etPlaceAddress);
        etKilometers = (EditText) findViewById(R.id.etKilometers);
        etAdditionalInfo = (EditText) findViewById(R.id.etAdditionalInfo);
        cbPlaceAddress = (CheckBox) findViewById(R.id.cbBlockPlaceAddress);
        cbAdditionalInfo = (CheckBox) findViewById(R.id.cbBlockAdditionalInfo);
        cbPrivateDrive = (CheckBox) findViewById(R.id.cbPrivateDrive);

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

        // gps
        this.startLocationThread();

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