package com.example.shopverse_customer_app.data;

/**
 * POJO representing a store location with all relevant information.
 * Used for JSON deserialization and data transfer throughout the app.
 */
public class Store {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String hours;

    // Default constructor required for Gson
    public Store() {
    }

    public Store(String id, String name, double latitude, double longitude, String address, String hours) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.hours = hours;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getHours() {
        return hours;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    /**
     * Validates that this store has valid coordinates.
     * @return true if latitude and longitude are valid
     */
    public boolean isValid() {
        return latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180 &&
               name != null && !name.isEmpty();
    }

    @Override
    public String toString() {
        return "Store{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", hours='" + hours + '\'' +
                '}';
    }
}
