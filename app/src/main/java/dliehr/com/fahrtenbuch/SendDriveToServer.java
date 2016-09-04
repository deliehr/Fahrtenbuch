package dliehr.com.fahrtenbuch;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dominik on 27.08.16.
 */

public class SendDriveToServer extends AsyncTask<FahrtItem, Void, Void> {
    // app
    private static final String TAG = SendDriveToServer.class.getSimpleName();

    // wait for task params
    private static int sleepTimeWait = 50;

    // class variables
    private Boolean backgroundJobDone = false;

    // constructor
    public SendDriveToServer(FahrtItem _drive) {
        // ?
    }

    // region getter
    public Boolean isBackgroundJobDone() { return this.backgroundJobDone; }
    // endregion

    // region setter
    // endregion

    public void waitForTaskFinish() {
        while(!this.backgroundJobDone) {
            try {
                Thread.sleep(sleepTimeWait);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onPreExecute() {

    }



    @Override
    protected Void doInBackground(FahrtItem... params) {
        String requestUrl = "http://www.idragon.de/testCall/index.php";

        this.backgroundJobDone = true;

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
