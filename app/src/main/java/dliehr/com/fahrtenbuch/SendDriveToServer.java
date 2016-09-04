package dliehr.com.fahrtenbuch;

import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        String apiRequest = "http://www.idragon.de/testCall.php";

        HttpPost httpPost = new HttpPost(apiRequest);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // http post parameters
            JSONObject postParameters = new JSONObject();
            postParameters.put("username", "anton");
            httpPost.setEntity(new StringEntity(postParameters.toString(), "UTF-8"));

            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();

            // process response
            int b;
            while((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }

            String x = "a";
            Log.i(TAG, stringBuilder.toString());
        } catch (JSONException jsoe) {
            Log.e(TAG, jsoe.getMessage());
        } catch (ClientProtocolException cpe) {
            Log.e("error", "error executing http-get request: " + cpe.getMessage());
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }

        this.backgroundJobDone = true;

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
