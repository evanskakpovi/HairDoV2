package com.ekm.hairdo.things

import java.util.ArrayList

class favs {

    var mFavs: ArrayList<String>? = null

    fun favs() {
        mFavs = ArrayList()
    }

    fun addToFav(id: String) {
        mFavs!!.add(id)
    }

    fun removeFav(id: String?) {
        mFavs!!.remove(id)
    }
}