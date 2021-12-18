package com.ekm.hairdo.things;

import java.util.ArrayList;

public class ChatDetails {

    String last_message;
    long last_openned_time;
    String name;
    String person1url = "https://res.cloudinary.com/hairdo/image/upload/c_scale,h_48,w_48,r_max,c_fill/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg";
    String person2url = "https://res.cloudinary.com/hairdo/image/upload/c_scale,h_48,w_48/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg";
    ArrayList<String> persons;
    ArrayList<String> personsNames;

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

    public ArrayList<String> getPersonsNames() {
        return personsNames;
    }

    public void setPersonsNames(ArrayList<String> personsNames) {
        this.personsNames = personsNames;
    }
}
