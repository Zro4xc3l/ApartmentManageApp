package com.example.apartmentmanageapp.model;

public class Tenant {
    private String name;
    private String unitNumber;
    private String rentStatus;
    private String phoneNumber;

    // Constructor
    public Tenant(String name, String unitNumber, String rentStatus, String phoneNumber) {
        this.name = name;
        this.unitNumber = unitNumber;
        this.rentStatus = rentStatus;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public String getRentStatus() {
        return rentStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public void setRentStatus(String rentStatus) {
        this.rentStatus = rentStatus;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
