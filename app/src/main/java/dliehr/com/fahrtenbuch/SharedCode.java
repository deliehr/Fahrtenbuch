package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dominik on 23.08.16.
 */
public class SharedCode {
    public static File createDatabaseBackup(Context context) {
        try {
            // copy db
            Uri r = Uri.fromFile(context.getDatabasePath(Database.FahrtenbuchDbHelper.DATABASE_NAME));

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                // generate backupname
                SimpleDateFormat currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String backupname = currentTime.format(new Date()) + "_database.db";

                String currentDBPath = r.getPath();
                String backupDBPath = backupname;
                File currentDB = new File("/", currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    try {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();

                        (new Toast(context).makeText(context, "DB Backup: " + backupname, Toast.LENGTH_SHORT)).show();

                        return backupDB;
                    } catch (Exception exc) {
                        return null;
                    }
                }
            }
        } catch (Exception exc) {
            return null;
        }

        return null;
    }

    public static boolean sendAutomaticEmail(Context context, String targetEmailAddress, String text) {
        Boolean returnValue = false;

        Intent i = null;

        try {
            File backupDB = SharedCode.createDatabaseBackup(context);

            if(backupDB != null) {
                try {
                    i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{targetEmailAddress});
                    i.putExtra(Intent.EXTRA_SUBJECT, "android dev dump db");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupDB));

                    if(i != null) {
                        context.startActivity(Intent.createChooser(i, "Send mail..."));
                        returnValue = true;
                    }
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // no backup
            }


        } catch (Exception exc) {
            Log.d("error", Errors.sending_email.getErrorText() + ": " + exc.getMessage());
        }



        return returnValue;
    }
}