package com.example.apartmentmanageapp.model;

public class Property {
    private String id; // Firestore document ID
    private String name;
    private String address;
    private int unitCount;

    public Property() {
        // Required empty constructor for Firestore
    }

    public Property(String name, String address, int unitCount) {
        this.name = name;
        this.address = address;
        this.unitCount = unitCount;
    }

    // Getter and setter for Firestore ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getUnitCount() {
        return unitCount;
    }
}
