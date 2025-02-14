package com.example.apartmentmanageapp.model;

import java.util.List;

public class Unit {
    private String unitId;
    private String propertyId;
    private double rentAmount;
    private boolean isOccupied;
    private String tenantName;
    private String rentStatus;
    private String roomSize;
    private String floorLevel;
    private List<String> amenities; // Updated to List<String>
    private double electricMeter;
    private double waterMeter;

    // Default constructor (Needed for Firestore)
    public Unit() {}

    // ✅ Updated constructor to include electricMeter & waterMeter
    public Unit(String unitId, String propertyId, double rentAmount, boolean isOccupied,
                String tenantName, String rentStatus, String roomSize, String floorLevel,
                List<String> amenities, double electricMeter, double waterMeter) {
        this.unitId = unitId;
        this.propertyId = propertyId;
        this.rentAmount = rentAmount;
        this.isOccupied = isOccupied;
        this.tenantName = tenantName;
        this.rentStatus = rentStatus;
        this.roomSize = roomSize;
        this.floorLevel = floorLevel;
        this.amenities = amenities;
        this.electricMeter = electricMeter;
        this.waterMeter = waterMeter;
    }

    // Getters
    public String getUnitId() { return unitId; }
    public String getPropertyId() { return propertyId; }
    public double getRentAmount() { return rentAmount; }
    public boolean isOccupied() { return isOccupied; }
    public String getTenantName() { return tenantName; }
    public String getRentStatus() { return rentStatus; }
    public String getRoomSize() { return roomSize; }
    public String getFloorLevel() { return floorLevel; }
    public List<String> getAmenities() { return amenities; }
    public double getElectricMeter() { return electricMeter; }
    public double getWaterMeter() { return waterMeter; }

    // Setters
    public void setUnitId(String unitId) { this.unitId = unitId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public void setRentStatus(String rentStatus) { this.rentStatus = rentStatus; }
    public void setRoomSize(String roomSize) { this.roomSize = roomSize; }
    public void setFloorLevel(String floorLevel) { this.floorLevel = floorLevel; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public void setElectricMeter(double electricMeter) { this.electricMeter = electricMeter; }
    public void setWaterMeter(double waterMeter) { this.waterMeter = waterMeter; }
}
