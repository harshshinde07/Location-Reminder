package com.apps.harsh.locationreminder;

import java.io.Serializable;


public class LocationCoordiante implements Serializable{

    public double latitude, longitude;

    public LocationCoordiante(double x, double y) {
        latitude = x;
        longitude = y;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
