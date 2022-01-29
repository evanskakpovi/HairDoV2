package com.ekm.hairdo.things

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Stack ( var address: String,
                   var url: String,
                   var price: String,
                   var lat: String,
                   var lng:String,
                   var name: String,
                   var hairid: String,
                   var styleName: String,
                   var stylistId: String,
                   var distance: String,
                   var fav: Boolean,
                   var favUpdated:Boolean) : Parcelable {

    companion object {
        fun DocumentSnapshot.toStack(): Stack? {
            try {

                var address = getString("address")!!
                var url = getString("url")!!
                var price = getString("price")!!
                var lat = getString("lat")!!
                var lng:String = getString("lng")!!
                var name = getString("name")!!
                var hairid = getString("hairid")!!
                var styleName = getString("styleName")!!
                var stylistId = getString("stylistId")!!
                var distance = getString("distance")!!
                var fav: Boolean = getBoolean("fav")!!
                var favUpdated:Boolean = getBoolean("favUpdated")!!
                return Stack(address,url,price,lat,lng,name,hairid,styleName,stylistId,distance,fav,favUpdated)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting user profile", e)
//                FirebaseCrashlytics.getInstance().log("Error converting user profile")
//                FirebaseCrashlytics.getInstance().setCustomKey("userId", id)
//                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            }
        }
        private const val TAG = "Stack"
    }

}