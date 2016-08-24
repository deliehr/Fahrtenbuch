package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
import java.util.ArrayList;

public class FahrtItem {
    private int id = 0;

    private String start_date = null;
    private String start_time = null;
    private String start_town = null;
    private String start_address = null;
    private Double start_kmstand = null;

    private String end_date = null;
    private String end_time = null;
    private String end_town = null;
    private String end_address = null;
    private Double end_kmstand = null;

    private Double latitude = null;
    private Double longitude = null;
    private String ortszusatz = null;
    private Boolean private_fahrt = null;
    private String car = null;

    public FahrtItem() {

    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) { this.id = id;}

    public void setStartFields(String start__date, String start__time, String start__town, String start__address, Double start__kmstand) {
        this.start_address = start__address;
        this.start_date = start__date;
        this.start_kmstand = start__kmstand;
        this.start_time = start__time;
        this.start_town = start__town;
    }

    public void setEndFields(String end__date, String end__time, String end__town, String end__address, Double end__kmstand) {
        this.end_address = end__address;
        this.end_date = end__time;
        this.end_kmstand = end__kmstand;
        this.end_time = end__time;
        this.end_town = end__town;
    }

    public void setOtherFields(Double lat, Double longi, String ort, Boolean priv_fahrt, String c) {
        this.latitude = lat;
        this.longitude = longi;
        this.ortszusatz = ort;
        this.private_fahrt = priv_fahrt;
        this.car = c;
    }

    public ArrayList<Object> getStartFields() {
        ArrayList<Object> fields = new ArrayList<Object>();
        fields.add(this.start_date);    // text 1
        fields.add(this.start_time);    // text 2
        fields.add(this.start_town);    // text 3
        fields.add(this.start_address); // text 4
        fields.add(this.start_kmstand); // real 5

        return fields;
    }

    public ArrayList<Object> getEndFields() {
        ArrayList<Object> fields = new ArrayList<Object>();
        fields.add(this.end_date);
        fields.add(this.end_time);
        fields.add(this.end_town);
        fields.add(this.end_address);
        fields.add(this.end_kmstand);

        return fields;
    }

    public ArrayList<Object> getOtherFields() {
        ArrayList<Object> fields = new ArrayList<Object>();
        fields.add(this.latitude);
        fields.add(this.longitude);
        fields.add(this.ortszusatz);
        fields.add(this.private_fahrt);
        fields.add(this.car);

        return fields;
    }

    public ArrayList<Object> getAllFields() {
        ArrayList<Object> fields = new ArrayList<Object>();

        fields.add(this.start_date);
        fields.add(this.start_time);
        fields.add(this.start_town);
        fields.add(this.start_address);
        fields.add(this.start_kmstand);

        fields.add(this.end_date);
        fields.add(this.end_time);
        fields.add(this.end_town);
        fields.add(this.end_address);
        fields.add(this.end_kmstand);

        fields.add(this.latitude);
        fields.add(this.longitude);
        fields.add(this.ortszusatz);
        fields.add(this.private_fahrt);
        fields.add(this.car);

        return fields;
    }

    public String getStartDate() { return this.start_date; }
    public String getStartTime() { return this.start_time; }
    public String getStartTown() { return this.start_town; }
    public String getStartAddress() { return this.start_address; }
    public Double getStartKmstand() { return this.start_kmstand; }
    public String getEndDate() { return this.end_date; }
    public String getEndTime() { return this.end_time; }
    public String getEndTown() { return this.end_town; }
    public String getEndAddress() { return this.end_address; }
    public Double getEndKmstand() { return this.end_kmstand; }
    public Double getLatitude() { return this.latitude; }
    public Double getLongitude() { return this.longitude; }
    public String getOrtszusatz() { return this.ortszusatz; }
    public Boolean getPrivateFahrt() { return this.private_fahrt; }
    public String getCar() { return this.car; }
}