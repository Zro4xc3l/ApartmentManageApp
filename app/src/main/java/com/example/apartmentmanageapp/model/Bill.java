package com.example.apartmentmanageapp.model;

import com.google.firebase.firestore.PropertyName;
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
    private double serviceFee; // NEW FIELD
    private double totalAdditional;
    private double grandTotal;
    private String billStatus;
    private long timestamp;
    private List<UnitBill> unitBills;
    private double totalserviceFee;

    // Default constructor required for Firebase
    public Bill() {
    }

    public Bill(String id,
                String propertyName,
                String propertyAddress,
                String billingPeriod,
                double totalRent,
                double totalElectric,
                double totalWater,
                double serviceFee,  // SETTING SERVICE FEE
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
        this.totalWater = totalWater;
        this.serviceFee = serviceFee;
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

    /**
     * Getter for totalWater.
     */
    public double getTotalWater() {
        return totalWater;
    }

    /**
     * Custom setter for totalWater that handles both Number and String inputs.
     * This method is annotated so Firestore calls it during deserialization.
     */
    @PropertyName("totalWater")
    public void setTotalWater(Object value) {
        if (value != null) {
            try {
                this.totalWater = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                this.totalWater = 0.0; // Default to 0 if parsing fails
            }
        } else {
            this.totalWater = 0.0;
        }
    }


    // Update: Map the Firestore field "totalServiceFee" to the serviceFee property
    @PropertyName("totalServiceFee")
    public double getServiceFee() {
        return serviceFee;
    }

    @PropertyName("totalServiceFee")
    public void setServiceFee(Object value) {
        if (value != null) {
            try {
                this.serviceFee = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                this.serviceFee = 0.0;
            }
        } else {
            this.serviceFee = 0.0;
        }
    }


    public double getTotalAdditional() {
        return totalAdditional;
    }

    /**
     * Custom setter for totalAdditional that handles both Number and String inputs.
     */
    @PropertyName("totalAdditional")
    public void setTotalAdditional(Object value) {
        if (value instanceof Number) {
            this.totalAdditional = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                this.totalAdditional = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                this.totalAdditional = 0.0;
            }
        } else {
            this.totalAdditional = 0.0;
        }
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