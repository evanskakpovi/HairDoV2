package com.ekm.hairdo.Stylist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ekm.hairdo.R
import com.ekm.hairdo.`var`.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*

class AddressShowActivity : AppCompatActivity() {
    val AUTOCOMPLETE_REQUEST_CODE = 2
    lateinit var continue_button: Button
    val TAG = "Addressactivity"
    var uid= "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_show)

        val intent = intent
        uid = intent.getStringExtra(otherUID).toString()
        continue_button = findViewById(R.id.donebuttoninadressact)
        continue_button.setOnClickListener{onSearchCalled()}
    }

    fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).setCountry("USA") //USA
                .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {

                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        ///

                        println(place.latLng)
                        //todo save address to database
                        val myIntent = Intent(this, UploadPhoto2::class.java)
                        //send necessary data forwardd
                        myIntent.putExtra(otherUID, uid)
                        myIntent.putExtra(stylistAddress, place.address)
                        myIntent.putExtra(stylistLat, place.latLng?.latitude.toString())
                        myIntent.putExtra(stylistLng, place.latLng?.longitude.toString())
                        startActivity(myIntent)
                        finish()
                        ///

                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    }
                    println(" -----------------------------------")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        println(status.statusMessage+" -----------------------------------")
                        Log.i(TAG, status.statusMessage.toString())
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                    println(" canceled -----------------------------------")
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}