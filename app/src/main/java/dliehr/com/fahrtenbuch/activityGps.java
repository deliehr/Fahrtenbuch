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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class activityGps extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_gps);
    }

    private void setBluetoothInfo() {
        // start bluetooth connection
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        if(bluetooth != null) {
            if(bluetooth.isEnabled()) {
                activityGps.tvBluetooth.setText("Bluetooth: ok. Adresse: " + bluetooth.getAddress() + ", Name: " + bluetooth.getName());
            } else {
                Log.d("error", Errors.bluetooth_not_enabled.getErrorText());
            }
        } else {
            Log.d("error", Errors.no_bluetooth_device_found.getErrorText());
        }
    }

    private void setLocationInfo() {
        // get location
        LocationManager locManager = null;
        boolean gpsEnabled = false;

        try {
            locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            gpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception exc) {
            Log.d("error", Errors.no_location_manager_existing.getErrorText());
        }

        if(gpsEnabled) {
            try {
                Criteria gpsCriteria = new Criteria();
                String gpsProvider = locManager.getBestProvider(gpsCriteria, false);
                activityGps.gpsLocation = locManager.getLastKnownLocation(gpsProvider);

                // get new location, 1000ms time
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);

                if (activityGps.gpsLocation != null) {
                    activityGps.updateLocationInfo(activityGps.gpsLocation, this);
                } else {
                    Log.d("error", Errors.location_is_null.getErrorText());
                }
            } catch(SecurityException exc) {
                Log.d("error", Errors.gettings_gps_location_not_allowed.getErrorText());
            } catch(Exception exc) {
                Log.w("error", "error: " + exc.getMessage());
            }
        } else {
            Log.d("error", Errors.gps_not_enabled.getErrorText());
        }
    }

    static void updateLocationInfo(Location loc, Context cont) {
        if(activityGps.tvLocation != null) {
            String locInfo = "Accuracy: " + Float.toString(loc.getAccuracy()) + ", " + "Latitude: " + Double.toString(loc.getLatitude()) + ", " + "Longitude: " + Double.toString(loc.getLongitude());

            // adress
            List<Address> addresses;
            Geocoder geocoder = new Geocoder(cont, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(activityGps.gpsLocation.getLatitude(), activityGps.gpsLocation.getLongitude(), 1);

                locInfo += ", Adresse: " + addresses.get(0).getAddressLine(0) + ", " + "Stadt: " + addresses.get(0).getLocality();
            } catch (IOException exc) {
                Log.d("error", Errors.no_addresses_available.getErrorText());
            }

            activityGps.tvLocation.setText(locInfo);
        }
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