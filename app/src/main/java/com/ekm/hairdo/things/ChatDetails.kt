package com.ekm.hairdo.things

import java.util.ArrayList

class ChatDetails {
    var last_message: String? = null
    var last_openned_time: Long = 0
    var name: String? = null
    var person1url =
        "https://res.cloudinary.com/hairdo/image/upload/c_scale,h_48,w_48,r_max,c_fill/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg"
    var person2url =
        "https://res.cloudinary.c om/hairdo/image/upload/c_scale,h_48,w_48/v1595563675/hair/jn0zpe4wpgox2qrcdjzq.jpg"
    var persons: ArrayList<String>? = null
    var personsNames: ArrayList<String>? = null
}