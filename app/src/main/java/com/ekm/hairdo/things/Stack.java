package com.ekm.hairdo.things;

import java.io.Serializable;

public class Stack implements Serializable {

    public String address;
    public String url;

    public String price;

    public String lat, lng;
    public String name;

    public String hairid;
    public String styleName;

    public String stylistId;

    String distance;

    public boolean fav=false, favUpdated=false;

    public Stack() {
    }

    public Stack(String url) {
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHairid() {
        return hairid;
    }

    public void setHairid(String hairid) {
        this.hairid = hairid;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStylistId() {
        return stylistId;
    }

    public void setStylistId(String stylistId) {
        this.stylistId = stylistId;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean isFavUpdated() {
        return favUpdated;
    }

    public void setFavUpdated(boolean favUpdated) {
        this.favUpdated = favUpdated;
    }
}

