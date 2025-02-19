package com.example.apartmentmanageapp.model;

import com.google.firebase.firestore.PropertyName;

public class UnitBill {
    private String unit;             // e.g., "01", "15", etc.
    private String tenantName;       // Tenant name
    private double prevElectricReading; // Loaded from DB (electricMeter)
    private double prevWaterReading;    // Loaded from DB (waterMeter)
    private double rentAmount;          // Rent amount
    private double electricityRate;     // Loaded from property
    private double waterRate;           // Loaded from property
    private double minElectricPrice;    // Loaded from property
    private double minWaterPrice;       // Loaded from property

    // Required empty constructor for Firestore
    public UnitBill() {
    }

    // Constructor
    public UnitBill(String unit, String tenantName, double prevElectricReading, double prevWaterReading, double rentAmount,
                    double electricityRate, double waterRate, double minElectricPrice, double minWaterPrice) {
        this.unit = unit;
        this.tenantName = tenantName;
        this.prevElectricReading = prevElectricReading;
        this.prevWaterReading = prevWaterReading;
        this.rentAmount = rentAmount;
        this.electricityRate = electricityRate;
        this.waterRate = waterRate;
        this.minElectricPrice = minElectricPrice;
        this.minWaterPrice = minWaterPrice;
    }

    // Getters and setters
    @PropertyName("unitId")
    public String getUnit() {
        return unit;
    }

    @PropertyName("unitId")
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    @PropertyName("electricMeter")
    public double getPrevElectricReading() {
        return prevElectricReading;
    }

    @PropertyName("electricMeter")
    public void setPrevElectricReading(double prevElectricReading) {
        this.prevElectricReading = prevElectricReading;
    }

    @PropertyName("waterMeter")
    public double getPrevWaterReading() {
        return prevWaterReading;
    }

    @PropertyName("waterMeter")
    public void setPrevWaterReading(double prevWaterReading) {
        this.prevWaterReading = prevWaterReading;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public double getElectricityRate() {
        return electricityRate;
    }

    public void setElectricityRate(double electricityRate) {
        this.electricityRate = electricityRate;
    }

    public double getWaterRate() {
        return waterRate;
    }

    public void setWaterRate(double waterRate) {
        this.waterRate = waterRate;
    }

    public double getMinElectricPrice() {
        return minElectricPrice;
    }

    public void setMinElectricPrice(double minElectricPrice) {
        this.minElectricPrice = minElectricPrice;
    }

    public double getMinWaterPrice() {
        return minWaterPrice;
    }

    public void setMinWaterPrice(double minWaterPrice) {
        this.minWaterPrice = minWaterPrice;
    }
}
