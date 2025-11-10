package com.example.bai2.model;

public class Customer {
    private String phone;
    private String name;
    private int points;
    private String createdAt;
    private String updatedAt;

    public Customer(String phone, String name, int points, String createdAt, String updatedAt) {
        this.phone = phone;
        this.name = name;
        this.points = points;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }


}
