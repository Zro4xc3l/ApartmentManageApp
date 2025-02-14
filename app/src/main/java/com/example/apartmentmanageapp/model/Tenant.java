package com.example.apartmentmanageapp.model;

public class Tenant {
    private String firstName;
    private String lastName;
    private String unitNumber;
    private String rentStatus;
    private String phoneNumber;

    // Required empty constructor for Firestore
    public Tenant() {}

    // Constructor
    public Tenant(String firstName, String lastName, String unitNumber, String rentStatus, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.unitNumber = unitNumber;
        this.rentStatus = rentStatus;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    // Helper method to get full name
    public String getName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}
