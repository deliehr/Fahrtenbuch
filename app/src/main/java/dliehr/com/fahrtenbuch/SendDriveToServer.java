package dliehr.com.fahrtenbuch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private Context mContext = null;
    private String result = "";

    // constructor
    public SendDriveToServer(Context _context) {
        this.mContext = _context;
    }

    // region getter
    public Boolean isBackgroundJobDone() { return this.backgroundJobDone; }
    public String getResult() { return this.result; }
    // endregion

    // region setter
    public void setResult(String value) { this.result = value; }
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
        final String appCode = "zff7DUFhDeGNC8Rt2VM6PoTQggVoVi7Nb3fD4696WtNebjFwXL";

        try {
            FahrtItem lastDrive = Database.getInstance(mContext).getLastDrive();

            if(lastDrive != null) {
                String requestUrl = "http://www.idragon.de/testCall/index.php" + "?appCode=" + appCode + "&content=" + lastDrive.getFormatedStringForServer();
                this.setResult(SharedCode.sendHttpGetRequest(requestUrl));
            } else {
                this.setResult("false");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            this.setResult("false");
        }

        this.backgroundJobDone = true;

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
