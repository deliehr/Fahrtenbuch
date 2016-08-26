package dliehr.com.fahrtenbuch;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class activityGps extends AppCompatActivity {
    // context
    private static Context mContext = null;

    // gps
    private static Location mLocation = null;
    private static GoogleApiClient mGoogleApiClient = null;
    private static LocationRequest mLocationRequest = null;
    private static com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;

            Log.i("info", "Location changed (onLocationChanged)");
        }
    };

    private static GoogleApiClient.ConnectionCallbacks mConnectionFallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if(mLocation == null) {
                try {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

                    //Toast.makeText(context, "location update (onConnected)", Toast.LENGTH_SHORT).show();
                    Log.i("info", "location update (onConnected)");
                } catch (SecurityException se) {
                    Toast.makeText(mContext, "getting location not allowed", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "connection failed", Toast.LENGTH_SHORT).show();
        }
    };

    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_UPDATE_INTERVAL = 100;

    private static void startGoogleApiClient() {
        try {
            // builder
            GoogleApiClient.Builder googleApiClientBuilder = new GoogleApiClient.Builder(mContext);
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

            Log.i("info", "google api client connected");
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopGoogleApiClient();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopGoogleApiClient();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_gps);

        // context
        mContext = this;

        // start gps
        startGoogleApiClient();

        // google map
        GoogleMap googleMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
}