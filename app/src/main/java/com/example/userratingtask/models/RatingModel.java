package com.example.userratingtask.models;

public class RatingModel {

    Number minRating, maxRating;
    String dateTime;

    public RatingModel(Number minRating, Number maxRating, String dateTime) {
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.dateTime = dateTime;
    }

    public Number getMinRating() {
        return minRating;
    }

    public void setMinRating(Number minRating) {
        this.minRating = minRating;
    }

    public Number getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(Number maxRating) {
        this.maxRating = maxRating;
    }

    public void setMaxRating(int maxRating) {
        this.maxRating = maxRating;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
