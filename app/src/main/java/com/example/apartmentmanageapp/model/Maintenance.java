package com.example.apartmentmanageapp.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Maintenance {
    private String id;
    private String title;
    private String description;
    private String status;
    private String expectedDate;
    // Rename field from propertyId to property to match Firestore.
    private String property;
    private String unit;
    private String imageUrl;
    private String propertyName;  // Optional: store fetched property name
    private String unitName;      // Optional: store fetched unit name

    // Getters and setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpectedDate() {
        return expectedDate;
    }
    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    // Use "property" to match Firestore field
    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getUnitName() {
        return unitName;
    }
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
