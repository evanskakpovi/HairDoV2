package com.ekm.hairdo.Stylist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ekm.hairdo.R
import com.ekm.hairdo.vars.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*

class AddressShowActivity : AppCompatActivity() {
    lateinit var continue_button: Button
    val TAG = "Addressactivity"
    var uid= "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_show)
        Log.i(TAG, "OnCreate")
        val apiKey = getString(R.string.api_key)
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        val intent = intent
        uid = intent?.getStringExtra(otherUID).toString()
        continue_button = findViewById(R.id.donebuttoninadressact)
        continue_button.setOnClickListener{onSearchCalled()}
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {

                Activity.RESULT_OK -> {
                    Log.i(TAG, "result okay")
                    val data: Intent? = result.data
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

                        //destroy places and free resource
                        Places.deinitialize()
                        //Send results back to calling activity
                        setResult(Activity.RESULT_OK, myIntent)
                        //destroy this activity
                        finish()


                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    }
                    println(" -----------------------------------")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    Log.i(TAG, "result error")
                    val data: Intent? = result.data
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        println(status.statusMessage+" -----------------------------------")
                        Log.i(TAG, status.statusMessage.toString())
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                    Log.i(TAG, "result is cancelled")
                }
            }
    }




    fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).setCountry("USA") //USA
                .build(this)
        resultLauncher.launch(intent)

    }

}