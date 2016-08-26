package dliehr.com.fahrtenbuch;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik on 26.08.16.
 */
public class PointOfInterest {
    private String description = null;
    private Double latitude = null;
    private Double longitude = null;
    private static Map<String, PointOfInterest> pointsOfInterest = null;

    public PointOfInterest() {
        // set pois
        this.setPointsOfInterest();
    }

    public PointOfInterest(String _description, Double _latitude, Double _longitude) {
        if(_description != null && _latitude != null && _longitude != null) {
            this.setDescription(_description);
            this.setLatitude(_latitude);
            this.setLongitude(_longitude);
        }

        // set pois
        this.setPointsOfInterest();
    }

    public PointOfInterest(String _description, Location _location) {
        if(_description != null && _location != null) {
            this.setDescription(_description);
            this.setLatitude(_location.getLatitude());
            this.setLongitude(_location.getLongitude());
        }

        // set pois
        this.setPointsOfInterest();
    }

    private void setPointsOfInterest() {
        pointsOfInterest = new HashMap<String, PointOfInterest>();
        pointsOfInterest.put("Zuhause", new PointOfInterest("Zuhause", 51.8211671, 8.1361084));
        pointsOfInterest.put("Uni PB Fü", new PointOfInterest("Uni PB Fü", 51.7323871, 8.7356671));
        pointsOfInterest.put("JET PB", new PointOfInterest("JET PB", 51.7431987, 8.7064774));
    }

    // region getter
    public String getDescription() { return this.description; }
    public Double getLatitude() { return this.latitude; }
    public Double getLongitude() { return this.longitude; }
    public Location getLocation() {
        Location location = new Location(this.getDescription());
        location.setLatitude(this.getLatitude());
        location.setLongitude(this.getLongitude());

        return location;
    }
    // endregion

    // region setter
    public void setDescription(String _description) { this.description = _description; }
    public void setLatitude(Double _latitude) { this.latitude = _latitude; }
    public void setLongitude(Double _longitude) { this.longitude = _longitude; }
    public void setLocation(Location _location) {
        if(_location != null) {
            this.setLatitude(_location.getLatitude());
            this.setLongitude(_location.getLongitude());
        }
    }
    // endregion
}
