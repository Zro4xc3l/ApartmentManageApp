package com.example.apartmentmanageapp.model;

public class Unit {

    private String id;
    private String propertyId;
    private String unitNumber; // Matches Firestore field name
    private double rentAmount;
    private boolean occupied;  // Renamed from isOccupied
    private String tenantName;
    private String rentStatus;
    private String roomSize;
    private String floorLevel;
    private String amenities;  // Single string for all amenities

    // Empty constructor required by Firestore
    public Unit() {
    }

    // Full constructor
    public Unit(String id,
                String propertyId,
                String unitNumber,
                double rentAmount,
                boolean occupied,
                String tenantName,
                String rentStatus,
                String roomSize,
                String floorLevel,
                String amenities) {

        this.id = id;
        this.propertyId = propertyId;
        this.unitNumber = unitNumber;
        this.rentAmount = rentAmount;
        this.occupied = occupied;
        // Provide defaults if the values are null
        this.tenantName = (tenantName != null) ? tenantName : "No Tenant";
        this.rentStatus = (rentStatus != null) ? rentStatus : "Unpaid";
        this.roomSize = roomSize;
        this.floorLevel = floorLevel;
        this.amenities = amenities; // single text field
    }

    // ----------------
    // GETTERS
    // ----------------
    public String getId() {
        return id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getRentStatus() {
        return rentStatus;
    }

    public String getRoomSize() {
        return roomSize;
    }

    public String getFloorLevel() {
        return floorLevel;
    }

    public String getAmenities() {
        return amenities;
    }

    // ----------------
    // SETTERS
    // ----------------
    public void setId(String id) {
        this.id = id;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setRentStatus(String rentStatus) {
        this.rentStatus = rentStatus;
    }

    public void setRoomSize(String roomSize) {
        this.roomSize = roomSize;
    }

    public void setFloorLevel(String floorLevel) {
        this.floorLevel = floorLevel;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
}
