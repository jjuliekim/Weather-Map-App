package com.example.kim_j_project5;

public class ForecastDetails {
    private String date;
    private double lowestTemp;
    private double highestTemp;
    private double precipitation;

    public ForecastDetails(String date, double lowestTemp, double highestTemp, double precipitation) {
        this.date = date;
        this.lowestTemp = lowestTemp;
        this.highestTemp = highestTemp;
        this.precipitation = precipitation;
    }

    // getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLowestTemp() {
        return lowestTemp;
    }

    public void setLowestTemp(double lowestTemp) {
        this.lowestTemp = lowestTemp;
    }

    public double getHighestTemp() {
        return highestTemp;
    }

    public void setHighestTemp(double highestTemp) {
        this.highestTemp = highestTemp;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
}
