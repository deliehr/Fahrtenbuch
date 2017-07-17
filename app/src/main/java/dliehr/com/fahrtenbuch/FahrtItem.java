package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class FahrtItem {
    private static final String TAG = FahrtItem.class.getSimpleName();

    private int id = 0;

    private String start_date = null;
    private String start_time = null;
    private String start_town = null;
    private String start_address = null;
    private Double start_kmstand = null;
    private Double start_latitude = null;
    private Double start_longitude = null;
    private String start_ortszusatz = null;
    private Boolean start_private_fahrt = null;
    private String start_car = null;

    private String end_date = null;
    private String end_time = null;
    private String end_town = null;
    private String end_address = null;
    private Double end_kmstand = null;
    private Double end_latitude = null;
    private Double end_longitude = null;
    private String end_ortszusatz = null;
    private Boolean end_private_fahrt = null;
    private String end_car = null;

    public FahrtItem() {

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) { this.id = id;}

    public void setStartFields(String start__date, String start__time, String start__town, String start__address, Double start__kmstand, Double start__latitude, Double start__longitude, String start__ortszusatz, Boolean start__private_fahrt, String start__car) {
        this.start_address = start__address;
        this.start_date = start__date;
        this.start_kmstand = start__kmstand;
        this.start_time = start__time;
        this.start_town = start__town;
        this.start_latitude = start__latitude;
        this.start_longitude = start__longitude;
        this.start_ortszusatz = start__ortszusatz;
        this.start_private_fahrt = start__private_fahrt;
        this.start_car = start__car;
    }

    public void setEndFields(String end__date, String end__time, String end__town, String end__address, Double end__kmstand, Double end__latitude, Double end__longitude, String end__ortszusatz, Boolean end__private_fahrt, String end__car) {
        this.end_address = end__address;
        this.end_date = end__date;
        this.end_kmstand = end__kmstand;
        this.end_time = end__time;
        this.end_town = end__town;
        this.end_latitude = end__latitude;
        this.end_longitude = end__longitude;
        this.end_ortszusatz = end__ortszusatz;
        this.end_private_fahrt = end__private_fahrt;
        this.end_car = end__car;
    }

    public ArrayList<Object> getStartFields() {
        ArrayList<Object> fields = new ArrayList<Object>();
        fields.add(this.start_date);    // text 1
        fields.add(this.start_time);    // text 2
        fields.add(this.start_town);    // text 3
        fields.add(this.start_address); // text 4
        fields.add(this.start_kmstand); // real 5
        fields.add(this.start_latitude);    // real 6
        fields.add(this.start_longitude);   // real 7
        fields.add(this.start_ortszusatz);  // text 8
        fields.add(this.start_private_fahrt);   // integer 9
        fields.add(this.start_car); // text 10

        return fields;
    }

    public ArrayList<Object> getEndFields() {
        ArrayList<Object> fields = new ArrayList<Object>();

        fields.add(this.end_date);  // text 11
        fields.add(this.end_time);  // text 12
        fields.add(this.end_town);  // text 13
        fields.add(this.end_address);   // text 14
        fields.add(this.end_kmstand);   // real 15
        fields.add(this.end_latitude);    // real 16
        fields.add(this.end_longitude);   // real 17
        fields.add(this.end_ortszusatz);  // text 18
        fields.add(this.end_private_fahrt);   // integer 19
        fields.add(this.end_car); // text 20

        return fields;
    }

    public ArrayList<Object> getAllFields() {
        ArrayList<Object> fields = new ArrayList<Object>();

        fields.add(this.start_date);    // text 1
        fields.add(this.start_time);    // text 2
        fields.add(this.start_town);    // text 3
        fields.add(this.start_address); // text 4
        fields.add(this.start_kmstand); // real 5
        fields.add(this.start_latitude);    // real 6
        fields.add(this.start_longitude);   // real 7
        fields.add(this.start_ortszusatz);  // text 8
        fields.add(this.start_private_fahrt);   // integer 9
        fields.add(this.start_car); // text 10

        fields.add(this.end_date);  // text 11
        fields.add(this.end_time);  // text 12
        fields.add(this.end_town);  // text 13
        fields.add(this.end_address);   // text 14
        fields.add(this.end_kmstand);   // real 15
        fields.add(this.end_latitude);    // real 16
        fields.add(this.end_longitude);   // real 17
        fields.add(this.end_ortszusatz);  // text 18
        fields.add(this.end_private_fahrt);   // integer 19
        fields.add(this.end_car); // text 20

        return fields;
    }

    public String getFormatedStringForServer() {
        StringBuilder result = new StringBuilder();

        try {
            result.append(this.getId());
            result.append(";");

            for(Object o : this.getStartFields()) {
                result.append(URLEncoder.encode(String.valueOf(o), "ISO-8859-1"));
                result.append(";");
            }

            for(Object o : this.getEndFields()) {
                result.append(URLEncoder.encode(String.valueOf(o), "ISO-8859-1"));
                result.append(";");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return result.toString().substring(0, result.length()-1);
    }

    public String getStartDate() { return this.start_date; }
    public String getStartTime() { return this.start_time; }
    public String getStartTown() { return this.start_town; }
    public String getStartAddress() { return this.start_address; }
    public Double getStartKmstand() { return this.start_kmstand; }
    public Double getStartLatitude() { return this.start_latitude; }
    public Double getStartLongitude() { return this.start_longitude; }
    public String getStartOrtszusatz() { return this.start_ortszusatz; }
    public Boolean getStartPrivateFahrt() { return this.start_private_fahrt; }
    public String getStartCar() { return this.start_car; }

    public String getEndDate() { return this.end_date; }
    public String getEndTime() { return this.end_time; }
    public String getEndTown() { return this.end_town; }
    public String getEndAddress() { return this.end_address; }
    public Double getEndKmstand() { return this.end_kmstand; }
    public Double getEndLatitude() { return this.start_latitude; }
    public Double getEndLongitude() { return this.start_longitude; }
    public String getEndOrtszusatz() { return this.start_ortszusatz; }
    public Boolean getEndPrivateFahrt() { return this.start_private_fahrt; }
    public String getEndCar() { return this.start_car; }

}