package dliehr.com.fahrtenbuch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class activityDatabase extends AppCompatActivity {
    // region class variables
    static boolean deleteDbEntries = false;
    // endregion

    // region button methods
    public void btnClickSendDatabase(View view) {
        SharedCode.sendAutomaticEmail(view.getContext(), "domi@idragon.de", "android dev dump db");
    }

    public void btnClickDbDeleteEntries(View view) {
        // ask really delete?
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("Bestätigen");
        alertDialogBuilder.setMessage("Sollen die Daten wirklich aus der Datenbank gelöscht werden?");
        alertDialogBuilder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                activityDatabase.deleteDbEntries = true;
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                activityDatabase.deleteDbEntries = false;
                dialog.dismiss();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        try {
            // first make a backup
            File backup = SharedCode.createDatabaseBackup(view.getContext());

            if(backup != null) {
                Database.getInstance(view.getContext()).deleteEntries(Database.T_FAHRT.TABLE_NAME);
            } else {
                // no backup
            }
        } catch (Exception exc) {
            Log.d("error", Errors.could_not_delete_db_entries.getErrorText() + ": " + exc.getMessage());
        }
    }
    // endregion

    // region other methods
    private void showDataFromTable() {
        List<FahrtItem> result = Database.getInstance(this).getAll();

        int column = 0;
        for(FahrtItem fi : result) {

            // reset column
            column = 0;
        }
    }
    // endregion

    // region override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_database);

        // show t_fahrt data
        this.showDataFromTable();
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
    // endregion
}