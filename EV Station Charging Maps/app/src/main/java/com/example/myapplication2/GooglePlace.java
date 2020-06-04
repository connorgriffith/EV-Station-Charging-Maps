package com.example.myapplication2;


import com.google.android.gms.maps.model.LatLng;

public class GooglePlace {
    private String name;
    private String placeId;
    private LatLng latLng;

    public GooglePlace(String name, String placeId, LatLng latLng) {
        this.name = name;
        this.placeId = placeId;
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getLatitudeString() {
        return Double.toString(latLng.latitude);
    }

    public String getLongitudeString() {
        return Double.toString(latLng.longitude);
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public String toString() {
        return getName();
    }
}