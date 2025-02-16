package com.example.apartmentmanageapp.model;

public class Bill {
    private String id;
    private String tenantName;
    private double amount;
    private String dueDate;
    private String status;

    // Default constructor required for calls to DataSnapshot.getValue(Bill.class)
    public Bill() {}

    public Bill(String id, String tenantName, double amount, String dueDate, String status) {
        this.id = id;
        this.tenantName = tenantName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTenantName() {
        return tenantName;
    }
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
