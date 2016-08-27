package dliehr.com.fahrtenbuch;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class activityGps extends AppCompatActivity {
    private static Context mContext = null;

    public void btnLocationCalc(View w) {
        Location loc1 = new Location("oelde"), loc2 = new Location("paderborn");
        loc1.setLatitude(51.8210364);
        loc1.setLongitude(8.1362575);

        loc2.setLatitude(51.7323871);
        loc2.setLongitude(8.7356671);

        Float delta = loc1.distanceTo(loc2);

        ((Toast) Toast.makeText(this, String.valueOf((delta / 1000.0)) + " km", Toast.LENGTH_LONG)).show();
    }





    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_gps);

        // context
        mContext = this;
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
}