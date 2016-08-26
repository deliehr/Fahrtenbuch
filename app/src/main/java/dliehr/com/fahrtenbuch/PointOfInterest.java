package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 26.08.16.
 */
public class PointOfInterest {
    private String description = null;
    private Float latitude = null;
    private Float longitude = null;

    public PointOfInterest() {

    }

    public PointOfInterest(String _description, Float _latitude, Float _longitude) {
        if(_description != null && _latitude != null && _longitude != null) {
            this.setDescription(_description);
            this.setLatitude(_latitude);
            this.setLongitude(_longitude);
        }
    }

    // region getter
    public String getDescription() { return this.description; }
    public Float getLatitude() { return this.latitude; }
    public Float getLongitude() { return this.longitude; }
    // endregion

    // region setter
    public void setDescription(String _description) { this.description = _description; }
    public void setLatitude(Float _latitude) { this.latitude = _latitude; }
    public void setLongitude(Float _longitude) { this.longitude = _longitude; }
    // endregion
}
