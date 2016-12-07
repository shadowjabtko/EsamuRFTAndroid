package hu.esamu.rft.esamurft.gps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jonat on 2016. 11. 25..
 */
public class RawMaterial {

    private String name;
    private LatLng location;

    public RawMaterial(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }

    public RawMaterial(String name, double latitude, double longitude) {
        this.name = name;
        this.location = new LatLng(latitude, longitude);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "RawMaterial{" +
                "name='" + name + "\n" +
                "location=" + location +
                '}';
    }
}
