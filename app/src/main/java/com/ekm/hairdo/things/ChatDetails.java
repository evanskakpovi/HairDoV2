package com.ekm.hairdo.things;

import java.util.ArrayList;

public class ChatDetails {

    String last_message;
    long last_openned_time;
    String name;
    String person1;
    String person2;
    String person1name;
    String person2name;
    String person1url = "https://res.cloudinary.com/hairdo/image/upload/c_scale,h_48,w_48,r_max,c_fill/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg";
    String person2url = "https://res.cloudinary.com/hairdo/image/upload/c_scale,h_48,w_48/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg";
    ArrayList<String> persons;

    public ChatDetails() {
        persons = new ArrayList<>();
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public long getLast_openned_time() {
        return last_openned_time;
    }

    public void setLast_openned_time(long last_openned_time) {
        this.last_openned_time = last_openned_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerson1() {
        return person1;
    }

    public void setPerson1(String person1) {
        this.person1 = person1;
    }

    public String getPerson2() {
        return person2;
    }

    public void setPerson2(String person2) {
        this.person2 = person2;
    }

    public String getPerson1name() {
        return person1name;
    }

    public void setPerson1name(String person1name) {
        this.person1name = person1name;
    }

    public String getPerson2name() {
        return person2name;
    }

    public void setPerson2name(String person2name) {
        this.person2name = person2name;
    }

    public String getPerson1url() {
        return person1url;
    }

    public void setPerson1url(String person1url) {
        this.person1url = person1url;
    }

    public String getPerson2url() {
        return person2url;
    }

    public void setPerson2url(String person2url) {
        this.person2url = person2url;
    }

    public ArrayList<String> getPersons() {
        return persons;
    }

    public void setPersons(ArrayList<String> persons) {
        this.persons = persons;
    }
}
