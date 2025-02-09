package com.example.apartmentmanageapp.model;

public class Unit {
    private String id;
    private String propertyId;
    private String unitNumber;
    private double rentAmount;
    private boolean isOccupied;
    private String tenantName;
    private String rentStatus;

    // 🔹 Firestore Needs an Empty Constructor
    public Unit() {}

    // Constructor
    public Unit(String id, String propertyId, String unitNumber, double rentAmount, boolean isOccupied, String tenantName, String rentStatus) {
        this.id = id;
        this.propertyId = propertyId;
        this.unitNumber = unitNumber;
        this.rentAmount = rentAmount;
        this.isOccupied = isOccupied;
        this.tenantName = tenantName != null ? tenantName : "No Tenant"; // ✅ Prevent null crashes
        this.rentStatus = rentStatus != null ? rentStatus : "Unpaid"; // ✅ Prevent null crashes
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName != null ? tenantName : "No Tenant"; } // ✅ Prevent null crashes

    public String getRentStatus() { return rentStatus; }
    public void setRentStatus(String rentStatus) { this.rentStatus = rentStatus != null ? rentStatus : "Unpaid"; } // ✅ Prevent null crashes
}
