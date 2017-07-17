package dliehr.com.fahrtenbuch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class activityConfig extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_config);
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
