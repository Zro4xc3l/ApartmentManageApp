package com.example.apartmentmanageapp.model;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class UnitBill implements Serializable {
    private String unit;
    private String tenantName;
    private double prevElectricReading;
    private double prevWaterReading;
    private double rentAmount;
    private double electricityRate;
    private double waterRate;
    private double minElectricPrice;
    private double minWaterPrice;
    private double serviceFee; // ✅ Add Service Fee

    // Additional fields for calculations
    private Double currentElectricReading;
    private Double currentWaterReading;
    private Double additionalFee;
    private String additionalFeeDescription;
    private String propertyId;


    // Required empty constructor for Firestore
    public UnitBill() {
        this.currentElectricReading = 0.0;
        this.currentWaterReading = 0.0;
        this.additionalFee = 0.0;
        this.serviceFee = 0.0;
        this.additionalFeeDescription = "";
    }

    public UnitBill(String unit, String tenantName, double prevElectricReading, double prevWaterReading, double rentAmount,
                    double electricityRate, double waterRate, double minElectricPrice, double minWaterPrice, double serviceFee, String propertyId) {
        this.unit = unit;
        this.tenantName = tenantName;
        this.prevElectricReading = prevElectricReading;
        this.prevWaterReading = prevWaterReading;
        this.rentAmount = rentAmount;
        this.electricityRate = electricityRate;
        this.waterRate = waterRate;
        this.minElectricPrice = minElectricPrice;
        this.minWaterPrice = minWaterPrice;
        this.serviceFee = serviceFee;
        this.currentElectricReading = prevElectricReading; // Default to previous reading
        this.currentWaterReading = prevWaterReading;       // Default to previous reading
        this.additionalFee = 0.0;
        this.additionalFeeDescription = "";
    }

    // Firebase Mapping
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

    public Double getCurrentElectricReading() {
        return currentElectricReading;
    }

    public void setCurrentElectricReading(Double currentElectricReading) {
        if (currentElectricReading != null && currentElectricReading >= prevElectricReading) {
            this.currentElectricReading = currentElectricReading;
        } else {
            this.currentElectricReading = prevElectricReading; // Ensure it's not incorrectly set
        }
    }

    public Double getCurrentWaterReading() {
        return currentWaterReading;
    }

    public void setCurrentWaterReading(Double currentWaterReading) {
        if (currentWaterReading != null && currentWaterReading >= prevWaterReading) {
            this.currentWaterReading = currentWaterReading;
        } else {
            this.currentWaterReading = prevWaterReading;
        }
    }

    public Double getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Double additionalFee) {
        this.additionalFee = (additionalFee != null && additionalFee >= 0) ? additionalFee : 0.0;
    }


    public String getAdditionalFeeDescription() {
        return additionalFeeDescription;
    }

    public void setAdditionalFeeDescription(String additionalFeeDescription) {
        this.additionalFeeDescription = additionalFeeDescription;
    }

    @PropertyName("serviceFee")
    public double getServiceFee() {
        return serviceFee;
    }

    @PropertyName("serviceFee")
    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }
}
