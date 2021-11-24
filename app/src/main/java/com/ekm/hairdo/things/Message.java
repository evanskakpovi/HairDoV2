package com.ekm.hairdo.things;

public class Message {

  String name;
  String text;
  String url_message;
  long message_time;
  String key;
  boolean _my_message;

    public Message() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl_message() {
        return url_message;
    }

    public void setUrl_message(String url_message) {
        this.url_message = url_message;
    }

    public long getMessage_time() {
        return message_time;
    }

    public void setMessage_time(long message_time) {
        this.message_time = message_time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean get_my_message() {
        return _my_message;
    }

    public void set_my_message(boolean my_message) {
        this._my_message = my_message;
    }
}
