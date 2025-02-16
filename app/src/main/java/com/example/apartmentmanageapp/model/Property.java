package com.example.apartmentmanageapp.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Property {
    @Exclude // 🔥 Prevents Firestore from saving this as a field
    private String id;
    private String name;
    private String address;
    private String ownerId;
    private int unitCount;
    private double electricityRate;
    private double waterRate;
    private double serviceFee;

    // Firestore annotations on fields
    @PropertyName("minElectricPrice")
    private double minElectricityPrice;

    @PropertyName("minWaterPrice")
    private double minWaterPrice;

    // Default constructor required for Firestore deserialization
    public Property() {}

    // Constructor with parameters
    public Property(String name, String address, String ownerId, int unitCount,
                    double electricityRate, double waterRate, double serviceFee,
                    double minElectricityPrice, double minWaterPrice) {
        this.name = name;
        this.address = address;
        this.ownerId = ownerId;
        this.unitCount = unitCount;
        this.electricityRate = electricityRate;
        this.waterRate = waterRate;
        this.serviceFee = serviceFee;
        this.minElectricityPrice = minElectricityPrice;
        this.minWaterPrice = minWaterPrice;
    }

    // Getter and Setter for id (Excluded from Firestore)
    @Exclude
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for address
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    // Getter and Setter for ownerId
    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // Getter and Setter for unitCount
    public int getUnitCount() {
        return unitCount;
    }
    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
    }

    // Getter and Setter for electricityRate
    public double getElectricityRate() {
        return electricityRate;
    }
    public void setElectricityRate(double electricityRate) {
        this.electricityRate = electricityRate;
    }

    // Getter and Setter for waterRate
    public double getWaterRate() {
        return waterRate;
    }
    public void setWaterRate(double waterRate) {
        this.waterRate = waterRate;
    }

    // Getter and Setter for serviceFee
    public double getServiceFee() {
        return serviceFee;
    }
    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    // Annotated Getter and Setter for minElectricityPrice
    @PropertyName("minElectricPrice")
    public double getMinElectricityPrice() {
        return minElectricityPrice;
    }
    @PropertyName("minElectricPrice")
    public void setMinElectricityPrice(double minElectricityPrice) {
        this.minElectricityPrice = minElectricityPrice;
    }

    // Annotated Getter and Setter for minWaterPrice
    @PropertyName("minWaterPrice")
    public double getMinWaterPrice() {
        return minWaterPrice;
    }
    @PropertyName("minWaterPrice")
    public void setMinWaterPrice(double minWaterPrice) {
        this.minWaterPrice = minWaterPrice;
    }
}
