package dliehr.com.fahrtenbuch;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Dominik on 26.08.16.
 */
public class PointOfInterest {
    private static final String TAG = activityStart.class.getSimpleName();
    private static final Locale mGermanLocale = Locale.GERMAN;

    String postalCode = "";
    String locality = "";
    String addressLine = "";
    String additionalInfo = "";
    Double latitude = 0.0;
    Double longitude = 0.0;

    public PointOfInterest() {
    }

    // region object getter
    public String getPostalCode() { return this.postalCode; }
    public String getLocality() { return this.locality; }
    public String getAddressLine() { return this.addressLine; }
    public String getAdditionalInfo() { return this.additionalInfo; }
    public Double getLatitude() { return this.latitude; }
    public Double getLongitude() { return this.longitude; }

    public Address getAddress() {
        Address tmpAddress = new Address(mGermanLocale);
        tmpAddress.setAddressLine(0, this.getAddressLine());
        tmpAddress.setPostalCode(this.getPostalCode());
        tmpAddress.setLocality(this.getLocality());
        tmpAddress.setLatitude(this.getLatitude());
        tmpAddress.setLongitude(this.getLongitude());

        return tmpAddress;
    }

    public Location getLocation() {
        Location tmpLocation = new Location(this.getAdditionalInfo());
        tmpLocation.setLatitude(this.getLatitude());
        tmpLocation.setLongitude(this.getLongitude());

        return tmpLocation;
    }

    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getAdditionalInfo());
        sb.append(": ");
        sb.append(this.getPostalCode());
        sb.append(" ");
        sb.append(this.getLocality());
        sb.append(", ");
        sb.append(this.getAddressLine());

        return sb.toString();
    }
    // endregion

    // region object setter
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setLocality(String locality) { this.locality = locality; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    // endregion

    // region class getter
    public static List<PointOfInterest> getPoints(Context context) {
        List<PointOfInterest> tmpPoints = new ArrayList<PointOfInterest>();

        // this.address.setLocality(new String(component.getString("long_name").getBytes("ISO-8859-1"), "UTF-8"));

        try {
            JSONObject jsonFile = new JSONObject(getJSONContent(context));

            if(jsonFile.has("points")) {
                JSONArray points = jsonFile.getJSONArray("points");
                if(points.length() > 0) {
                    for(int i=0;i < points.length();i++) {
                        JSONObject poi = points.getJSONObject(i);

                        PointOfInterest pointOfInterest = new PointOfInterest();
                        pointOfInterest.setPostalCode(new String(poi.get("postal_code").toString().getBytes("UTF-8"), "UTF-8"));
                        pointOfInterest.setLocality(new String(poi.get("locality").toString().getBytes("UTF-8"), "UTF-8"));
                        pointOfInterest.setAddressLine(new String(poi.get("address").toString().getBytes("UTF-8"), "UTF-8"));
                        pointOfInterest.setAdditionalInfo(new String(poi.get("additional_info").toString().getBytes("UTF-8"), "UTF-8"));
                        //pointOfInterest.setLatitude(new String(poi.get("latitude").toString().getBytes("ISO-8859-1"), "UTF-8"));
                        //pointOfInterest.setLongitude(new String(poi.get("longitude").toString().getBytes("ISO-8859-1"), "UTF-8"));
                        //pointOfInterest.setLatitude(Double.valueOf(new String(poi.get("latitude").toString().getBytes("ISO-8859-1"), "UTF-8")));
                        //pointOfInterest.setLongitude(Double.valueOf(new String(poi.get("longitude").toString().getBytes("ISO-8859-1"), "UTF-8")));
                        pointOfInterest.setLatitude(Double.valueOf(poi.get("latitude").toString()));
                        pointOfInterest.setLongitude(Double.valueOf(poi.get("longitude").toString()));

                        tmpPoints.add(pointOfInterest);
                    }
                }
            }
        } catch (JSONException jsoe) {
            Log.e(TAG, jsoe.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return tmpPoints;
    }
    // endregion

    // region class methods
    private static String getJSONContent(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.json_test_file);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, uee.getMessage());
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }finally {
            try {
                is.close();
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return writer.toString();
    }
    // endregion
}
