package com.example.apartmentmanageapp.model;

public class Bill {
    private String propertyName;
    private double totalAmount;
    private String status;

    public Bill() {
        // Default constructor required for Firebase deserialization
    }

    public Bill(String propertyName, double totalAmount, String status) {
        this.propertyName = propertyName;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
