package com.example.apartmentmanageapp.model;

public class Tenant {
    // New fields for Firestore document ID and property ID.
    private String id;
    private String propertyId;

    private String firstName;
    private String lastName;
    private String unit;
    private String phoneNumber;
    private String email;
    private String leaseStartDate;
    private String leaseEndDate;
    private String emergencyContactName;
    private String emergencyContactPhone;

    // Required empty constructor for Firestore.
    public Tenant() {}

    // Full constructor (excluding id and propertyId, which are set separately).
    public Tenant(String firstName, String lastName, String unit, String phoneNumber,
                  String email, String leaseStartDate, String leaseEndDate,
                  String emergencyContactName, String emergencyContactPhone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.unit = unit;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.leaseStartDate = leaseStartDate;
        this.leaseEndDate = leaseEndDate;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
    }

    // Getters and Setters for new fields.
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId;
    }
    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    // Existing getters.
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUnit() { return unit; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getLeaseStartDate() { return leaseStartDate; }
    public String getLeaseEndDate() { return leaseEndDate; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }

    // Existing setters.
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setLeaseStartDate(String leaseStartDate) { this.leaseStartDate = leaseStartDate; }
    public void setLeaseEndDate(String leaseEndDate) { this.leaseEndDate = leaseEndDate; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    // Helper method to get full name.
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}
