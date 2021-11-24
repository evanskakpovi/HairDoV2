package com.ekm.hairdo.things;

import java.util.ArrayList;

public class favs {


    public ArrayList<String> mFavs;

    public favs() {
        this.mFavs = new ArrayList<>();
    }

    public ArrayList<String> getmFavs() {
        return mFavs;
    }

    public void setmFavs(ArrayList<String> mFavs) {
        this.mFavs = mFavs;
    }

    public void addToFav(String id) {
        mFavs.add(id);
    }

    public  void  removeFav(String id) {
        mFavs.remove(id);
    }
}
