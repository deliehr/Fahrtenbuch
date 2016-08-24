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
    private String start_kmstand = null;

    private String end_date = null;
    private String end_time = null;
    private String end_town = null;
    private String end_address = null;
    private String end_kmstand = null;

    public FahrtItem() {

    }

    public int getId() {
        return this.id;
    }
    public void setId(int id) { this.id = id;}

    public void setStartFields(String start__date, String start__time, String start__town, String start__address, String start__kmstand) {
        this.start_address = start__address;
        this.start_date = start__date;
        this.start_kmstand = start__kmstand;
        this.start_time = start__time;
        this.start_town = start__town;
    }

    public void setEndFields(String end__date, String end__time, String end__town, String end__address, String end__kmstand) {
        this.end_address = end__address;
        this.end_date = end__time;
        this.end_kmstand = end__kmstand;
        this.end_time = end__time;
        this.end_town = end__town;
    }

    public ArrayList<String> getStartFields() {
        ArrayList<String> fields = new ArrayList<String>();
        fields.add(this.start_date);
        fields.add(this.start_time);
        fields.add(this.start_town);
        fields.add(this.start_address);
        fields.add(this.start_kmstand);

        return fields;
    }

    public ArrayList<String> getEndFields() {
        ArrayList<String> fields = new ArrayList<String>();
        fields.add(this.end_date);
        fields.add(this.end_time);
        fields.add(this.end_town);
        fields.add(this.end_address);
        fields.add(this.end_kmstand);

        return fields;
    }

    public ArrayList<String> getAllFields() {
        ArrayList<String> fields = new ArrayList<String>();

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

        return fields;
    }

    public String getStartDate() { return this.start_date; }
    public String getStartTime() { return this.start_time; }
    public String getStartTown() { return this.start_town; }
    public String getStartAddress() { return this.start_address; }
    public String getStartKmstand() { return this.start_kmstand; }
    public String getEndDate() { return this.end_date; }
    public String getEndTime() { return this.end_time; }
    public String getEndTown() { return this.end_town; }
    public String getEndAddress() { return this.end_address; }
    public String getEndKmstand() { return this.end_kmstand; }
}