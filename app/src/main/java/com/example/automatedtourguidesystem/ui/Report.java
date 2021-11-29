package com.example.automatedtourguidesystem.ui;

import android.location.Location;

public class Report {





    public Report(String address, String place, String latitude, String longitude, String audio) {
        this.address = address;
        this.place = place;
        Latitude = latitude;
        Longitude = longitude;
        this.audio = audio;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }



    public void setAudio(String audio) {
        this.audio = audio;
    }



    public String getAddress() {
        return address;
    }

    public String getPlace() {
        return place;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public String getAudio() {
        return audio;
    }

    private String address;
    private String place;
    private String Latitude;
    private String Longitude;
    private String audio;


    public Report(){
        this.address = "";
        this.place = "";
        this.Latitude = "";
        this.Longitude = "";
        this.audio="";

    }

    public  double getDistanceDifferent(Double langtitude, Double longtitude){

        Location lhs_LOCATION = new Location("lhslocation");
        lhs_LOCATION.setLatitude(langtitude);
        lhs_LOCATION.setLongitude(longtitude);

        Location rhs_LOCATION = new Location("rhslocation");
        rhs_LOCATION.setLatitude(Double.parseDouble(this.Latitude) );
        rhs_LOCATION.setLongitude(Double.parseDouble(this.Longitude));

        return Math.round(rhs_LOCATION.distanceTo(lhs_LOCATION)*100.0/100000) ;

    }


}
