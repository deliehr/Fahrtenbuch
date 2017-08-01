package implementations;

import android.location.Location;

import java.util.Date;

public class WayPoint extends Location {
    // local fields
    private Date pointOfTime = null;
    // endregion

    // region init
      public WayPoint(String provider) {
        super(provider);
        this.pointOfTime = new Date();
    }

    public WayPoint(Location location) {
        super(location);
        this.pointOfTime = new Date();
    }
    // endregion

    // region getter & setter

    public Date getPointOfTime() {
        return pointOfTime;
    }

    // endregion
}