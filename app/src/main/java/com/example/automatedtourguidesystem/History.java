package com.example.automatedtourguidesystem;

public class History {
    private String location;

    public History(String location, String start_time, String end_time) {
        this.location = location;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public History() {
        this.location = "";
        this.start_time = "";
        this.end_time = "";
    }

    private String start_time;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    private String end_time;

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }


}

