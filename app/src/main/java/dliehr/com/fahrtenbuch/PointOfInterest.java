package dliehr.com.fahrtenbuch;

import android.location.Address;
import android.location.Location;
import android.util.Log;

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

    private Address address = null;
    private String additionalnfo = null;
    private Double latitude = null;
    private Double longitude = null;
    private static List<PointOfInterest> pointsOfInterest = null;

    public PointOfInterest() {
        // set pois
        this.setPointsOfInterest();
    }

    public PointOfInterest(Address _address, String _additionalInfo, Double _latitude, Double _longitude) {
        if(_address != null && _additionalInfo != null && _latitude != null && _longitude != null) {
            this.setAddress(_address);
            this.setAdditionalnfo(_additionalInfo);
            this.setLatitude(_latitude);
            this.setLongitude(_longitude);

            Location tmpLocation = new Location(_additionalInfo);
            tmpLocation.setLatitude(_latitude);
            tmpLocation.setLongitude(_longitude);
            this.setLocation(tmpLocation);
        }
    }

    public PointOfInterest(Address _address, String _additionalInfo, Location _location) {
        if(_address != null && _additionalInfo != null && _location != null) {
            this.setAddress(_address);
            this.setAdditionalnfo(_additionalInfo);
            this.setLatitude(_location.getLatitude());
            this.setLongitude(_location.getLongitude());
            this.setLocation(_location);
        }
    }

    private static void setPointsOfInterest() {
        pointsOfInterest = new ArrayList<PointOfInterest>();

        try {
            Address tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("59302");
            tmpAddress.setLocality("Oelde");
            tmpAddress.setAddressLine(0, "");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "Zuhause", 51.8211671, 8.1361084));

            tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("33102");
            tmpAddress.setLocality("Paderborn");
            tmpAddress.setAddressLine(0, "Fürstenallee 9");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "Uni PB Fü", 51.7323871, 8.7356671));

            tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("33104");
            tmpAddress.setLocality("Paderborn");
            tmpAddress.setAddressLine(0, "Münsterstraße 11");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "JET PB", 51.7431987, 8.7064774));

            tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("33378");
            tmpAddress.setLocality("Rheda-Wiedenbrück");
            tmpAddress.setAddressLine(0, "Heinrich-Heineke-Straße");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "Poco Außenlager Rheda", 51.861523, 8.2629678));

            tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("59269");
            tmpAddress.setLocality("Beckum");
            tmpAddress.setAddressLine(0, "");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "See Beckum", 51.7737455, 8.031064));

            tmpAddress = new Address(Locale.GERMANY);
            tmpAddress.setPostalCode("59302");
            tmpAddress.setLocality("Oelde");
            tmpAddress.setAddressLine(0, "TBonhoefferstraße 5");
            pointsOfInterest.add(new PointOfInterest(tmpAddress, "Dirk", 51.8409095, 8.1533871));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // Papa, Sascha, WG, Katrin, Julian

    }

    // region getter
    public Address getAddress() { return this.address; }
    public String getAdditionalnfo() { return this.additionalnfo; }
    public Double getLatitude() { return this.latitude; }
    public Double getLongitude() { return this.longitude; }

    public Location getLocation() {
        Location location = new Location(this.getAdditionalnfo());
        location.setLatitude(this.getLatitude());
        location.setLongitude(this.getLongitude());

        return location;
    }
    public static List<PointOfInterest> getPointsOfInterest() {
        setPointsOfInterest();

        return pointsOfInterest;
    }
    // endregion

    // region setter
    public void setAddress(Address _address) { this.address = _address; }
    public void setAdditionalnfo(String _additionalInfo) { this.additionalnfo = _additionalInfo; }
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
