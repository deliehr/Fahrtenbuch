package implementations;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Locale;

import implementations.WayPoint;

public class GpsLocationListener implements LocationListener {
    // region local fields
    private final String TAG = "GpsLocationListener";
    private WayPoint lastWayPoint = null;
    private Double traveledSpeed = 0.0;
    // endregion

    // region init
    public GpsLocationListener() {
        this.lastWayPoint = new WayPoint(LocationManager.GPS_PROVIDER);
    }
    // endregion

    // region object methods
    public WayPoint getLastWayPoint() {
        synchronized (this.lastWayPoint) {
            return this.lastWayPoint;
        }
    }

    private Double calcTravelSpeed(WayPoint oldWayPoint, WayPoint newWayPoint) {
        Double speed = 0.0;

        try {
            if(oldWayPoint != null) {
                long timeDistance = Math.abs(oldWayPoint.getPointOfTime().getTime() - newWayPoint.getPointOfTime().getTime());
                double distance = Math.abs(oldWayPoint.distanceTo(newWayPoint));
                speed = distance / Double.valueOf(String.valueOf(timeDistance)) * 3600.0;
            }
        } catch (Exception e) {
            speed = 0.0;
        }

        return speed;
    }
    // endregion

    // region interface methods
    @Override
    public void onLocationChanged(Location location) {
        // location not null?
        if(location == null) {
            return;
        } else {
            // threading: synchronized !
            synchronized (this.lastWayPoint) {
                // calc speed
                this.setTraveledSpeed(this.calcTravelSpeed(this.lastWayPoint, new WayPoint(location)));

                //Log.i(this.TAG, "gps listener: new waypoint");
                this.lastWayPoint = new WayPoint(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    // endregion

    // region getters & setters
    public Double getTraveledSpeed() {
        synchronized (this.traveledSpeed) {
            return this.traveledSpeed;
        }
    }

    public void setTraveledSpeed(Double traveledSpeed) {
        synchronized (this.traveledSpeed) {
            this.traveledSpeed = traveledSpeed;
        }
    }
    // endregion
}
