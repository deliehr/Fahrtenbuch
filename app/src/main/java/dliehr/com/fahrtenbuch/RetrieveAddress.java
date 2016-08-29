package dliehr.com.fahrtenbuch;

import android.accounts.AbstractAccountAuthenticator;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Created by Dominik on 27.08.16.
 */

public class RetrieveAddress extends AsyncTask<Location, Void, Void> {
    // app
    private static final String TAG = activityStart.class.getSimpleName();

    // wait for task params
    private static int sleepTimeWait = 50;

    // class variables
    private static final String LOCATION_API_KEY = "AIzaSyDFfDvQ-h8XaY3ZqDgooEOW38Aj9oEAf5Q";
    private Location location = null;
    private Address address = null;
    private Boolean backgroundJobDone = false;

    // constructor
    public RetrieveAddress(Location _location) {
        if(_location != null) {
            this.location = _location;
        } else {
            this.location = null;
            this.address = null;
        }
    }

    // region getter
    public Address getAddress() { return this.address; }
    public Location getLocation() { return this.location; }
    public Boolean isBackgroundJobDone() { return this.backgroundJobDone; }
    // endregion

    // region setter
    private void setAddress(Address _address) {
        this.address = _address;
    }
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
    protected Void doInBackground(Location... params) {
        if(this.location != null) {
            address = new Address(Locale.GERMANY);
            address.setLatitude(this.location.getLatitude());
            address.setLongitude(this.location.getLongitude());

            // sending the request
            String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(this.location.getLatitude()) +"," + String.valueOf(this.location.getLongitude()) + "&sensor=false&key=" + LOCATION_API_KEY;

            Log.i(TAG, "apiRequest = " + apiRequest);

            HttpGet httpGet = new HttpGet(apiRequest);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();

                // process response
                int b;
                while((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException cpe) {
                Log.d("error", "error executing http-get request: " + cpe.getMessage());
            } catch (Exception e) {
                Log.d("error", e.getMessage());
            }

            // get result in json object
            JSONObject result = null;
            try {
                result = new JSONObject(stringBuilder.toString());
            } catch (JSONException jsoe) {
                result = null;
                Log.d("error", "error parsing in json string: " + jsoe.getMessage());
            }

            // process json object
            if(result != null) {
                if(result.has("results")) {
                    try {
                        JSONArray array = result.getJSONArray("results");
                        if( array.length() > 0 ){
                            JSONObject place = array.getJSONObject(0);
                            JSONArray components = place.getJSONArray("address_components");

                            String addressLine = "";
                            String streetNumber = "";

                            for( int i = 0 ; i < components.length() ; i++ ){
                                JSONObject component = components.getJSONObject(i);
                                JSONArray types = component.getJSONArray("types");

                                for( int j = 0 ; j < types.length() ; j ++ ){
                                    if( types.getString(j).equals("locality") ){
                                        this.address.setLocality(new String(component.getString("long_name").getBytes("ISO-8859-1"), "UTF-8"));
                                    }

                                    if( types.getString(j).equals("postal_code") ){
                                        this.address.setPostalCode(new String(component.getString("long_name").getBytes("ISO-8859-1"), "UTF-8"));
                                    }

                                    if( types.getString(j).equals("route") ){
                                        addressLine = new String(component.getString("long_name").getBytes("ISO-8859-1"), "UTF-8");
                                    }

                                    if( types.getString(j).equals("street_number") ){
                                        streetNumber = new String(component.getString("long_name").getBytes("ISO-8859-1"), "UTF-8");
                                    }
                                }
                            }

                            this.address.setAddressLine(0, addressLine + " " + streetNumber);
                        }
                    } catch (UnsupportedEncodingException uee) {
                        Log.e(TAG, uee.getMessage());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                this.backgroundJobDone = true;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
