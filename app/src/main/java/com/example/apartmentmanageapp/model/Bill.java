package com.example.apartmentmanageapp.model;

import java.io.Serializable;
import java.util.List;

public class Bill implements Serializable {
    private String id;
    private String propertyName;
    private String propertyAddress;
    private String billingPeriod;
    private double totalRent;
    private double totalElectric;
    private double totalElectricUsage;
    private double totalWater;
    private double totalWaterUsage;
    private double serviceFee; // 🔹 NEW FIELD
    private double totalAdditional;
    private double grandTotal;
    private String billStatus;
    private long timestamp;
    private List<UnitBill> unitBills;

    // Default constructor required for Firebase
    public Bill() {
    }

    public Bill(String id,
                String propertyName,
                String propertyAddress,
                String billingPeriod,
                double totalRent,
                double totalElectric,
                double totalElectricUsage,
                double totalWater,
                double totalWaterUsage,
                double serviceFee,  // 🔹 ADDED IN CONSTRUCTOR
                double totalAdditional,
                double grandTotal,
                String billStatus,
                long timestamp,
                List<UnitBill> unitBills) {

        this.id = id;
        this.propertyName = propertyName;
        this.propertyAddress = propertyAddress;
        this.billingPeriod = billingPeriod;
        this.totalRent = totalRent;
        this.totalElectric = totalElectric;
        this.totalElectricUsage = totalElectricUsage;
        this.totalWater = totalWater;
        this.totalWaterUsage = totalWaterUsage;
        this.serviceFee = serviceFee;  // 🔹 SETTING SERVICE FEE
        this.totalAdditional = totalAdditional;
        this.grandTotal = grandTotal;
        this.billStatus = billStatus;
        this.timestamp = timestamp;
        this.unitBills = unitBills;
    }

    // --------------------------
    // Getters & Setters
    // --------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public void setBillingPeriod(String billingPeriod) {
        this.billingPeriod = billingPeriod;
    }

    public double getTotalRent() {
        return totalRent;
    }

    public void setTotalRent(double totalRent) {
        this.totalRent = totalRent;
    }

    public double getTotalElectric() {
        return totalElectric;
    }

    public void setTotalElectric(double totalElectric) {
        this.totalElectric = totalElectric;
    }

    public double getTotalElectricUsage() {
        return totalElectricUsage;
    }

    public void setTotalElectricUsage(double totalElectricUsage) {
        this.totalElectricUsage = totalElectricUsage;
    }

    public double getTotalWater() {
        return totalWater;
    }

    public void setTotalWater(double totalWater) {
        this.totalWater = totalWater;
    }

    public double getTotalWaterUsage() {
        return totalWaterUsage;
    }

    public void setTotalWaterUsage(double totalWaterUsage) {
        this.totalWaterUsage = totalWaterUsage;
    }

    public double getServiceFee() {  // 🔹 GETTER FOR SERVICE FEE
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {  // 🔹 SETTER FOR SERVICE FEE
        this.serviceFee = serviceFee;
    }

    public double getTotalAdditional() {
        return totalAdditional;
    }

    public void setTotalAdditional(double totalAdditional) {
        this.totalAdditional = totalAdditional;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<UnitBill> getUnitBills() {
        return unitBills;
    }

    public void setUnitBills(List<UnitBill> unitBills) {
        this.unitBills = unitBills;
    }

    public double getTotalAmount() {
        return grandTotal;
    }
}
