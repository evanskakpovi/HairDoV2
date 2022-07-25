package com.ekm.hairdo.Stylist

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ekm.hairdo.R
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.vars
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fonfon.geohash.GeoHash
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UploadPhotoK : AppCompatActivity() {

    final val EMPTY = "none"

    private final val tag = "UploadPhotoK"

    //define views
    private lateinit var viewImage: ImageView
    private lateinit var uploadButton: Button

    //Define mapper
    private lateinit var mapper: ObjectMapper

    //Define stack
    private lateinit var myStack: Stack

    //define firebase items
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    private var isUidPresent = false
    private var uid = ""
    private var displayname = ""

    private var lng: String = EMPTY
    private var lat = EMPTY
    private var address = EMPTY

    //define animation view
    private lateinit var mLottieAnimationViewUploading: LottieAnimationView
    private lateinit var mLottieAnimationViewPrior: LottieAnimationView

    //define upload boolean
    private var isPhotoChosen = false

    // Access a Cloud Firestore instance from your Activity
    var db = FirebaseFirestore.getInstance()

    //Create a photoupload activity result listener and register it
    private val retrievePhotoWithResults = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
        ) { result ->
        Log.i(tag, "Result code in photoupload is ${result.resultCode}")
        //reset anim to allow for  new choices
        stopAnimatingWhileWeWait()
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

            //Image Uri will not be null for RESULT_OK

            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            uploadButton.visibility = View.VISIBLE
          //  uploadImageUrlToFireStore(fileUri)
            uploadButton.setOnClickListener { uploadImageUrlToFireStore(fileUri) }
//            viewImage.setImageURI(fileUri)
            Glide.with(this).load(fileUri).into(viewImage)
            isPhotoChosen = true
            Log.i(tag, "the data has won ${data?.data}")
            // send address to database using viewModel
          //TODO upload picture here
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        //retrieve data from intent
        val mIntent = intent
        uid = mIntent?.getStringExtra(vars.otherUID).toString()
        address = mIntent?.getStringExtra(vars.stylistAddress).toString()
        lng = mIntent?.getStringExtra(vars.stylistLng).toString()
        lat = mIntent?.getStringExtra(vars.stylistLat).toString()

        Log.i(tag, "uid is $uid, address is $address, lng is $lng, and lat is $lat")


        //initialize items
        initAll()

        //set clicklistners
        setClicks()

        selectImage()
    }

    override fun onResume() {
        super.onResume()
        //Show animition while waiting for intent data
        Log.i(tag, "RESUME")
        animateWhileWeWait()
    }

    override fun onStart() {
        super.onStart()
        Log.i(tag, "START")
        animateWhileWeWait()
    }

    private fun animateWhileWeWait() {
        if (anim)
            mLottieAnimationViewPrior.visibility = View.VISIBLE
    }
    private fun stopAnimatingWhileWeWait() {
        anim = false
        mLottieAnimationViewPrior.visibility = View.GONE
    }

    private fun uploadImageUrlToFireStore(uri: Uri?) {
        Log.i(tag, "doing this")
        MediaManager.get()
            .upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.i(tag,"On Start Media Manager with requestID $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.i(tag,"requestID $requestId -On Progress Media Manager $bytes out of $totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as String
                    val id = resultData["public_id"] as String
                    val finalid = id.substring(id.lastIndexOf("hair/") + 5)
                    myStack = Stack(address,url,"",lat, lng, "", finalid,"Unique Style", uid, "", false, false)
                    addStyleToFirestoreDatabase(myStack)
                    Log.i(tag, "Final ${myStack.hairid} ResultData ${myStack.url} ")
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.i(tag, "RequestId $requestId with error ${error.description}")
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).unsigned("hairdo_default2").dispatch()
    //.option("public_id", "style_"+uid)

    }

    private fun addStyleToFirestoreDatabase(myStack: Stack) {

        //Convert POJO to Map
        myStack.distance = geoHashForMe(myStack)
        val map = mapper.convertValue<Map<String, Any>>(
            myStack,
            object : TypeReference<Map<String?, Any?>?>() {})
        db.collection("HAIR_STYLES3").document(myStack.stylistId+"_"+myStack.hairid)
            .set(map)
            .addOnSuccessListener {
                //Remove uploading animation
                Log.i(tag, "$myStack")
                Log.i(tag, "Data string is ${myStack.stylistId} _ ${myStack.hairid}")
                mLottieAnimationViewUploading.visibility = View.GONE
                //Exit Upload activity
                finish()

            }
            .addOnFailureListener {
                //TODO Failure notification
                Log.i(tag, "Error is $it")
            }

    }

    override fun onPause() {
        super.onPause()
        Log.i(tag, "PAUSE")
    }
    private fun initAll() {
        Log.i(tag, "initialize items")
        //init mapper
        mapper = ObjectMapper()
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        //init lottieanimationview
        mLottieAnimationViewUploading = findViewById(R.id.animation_view)
        mLottieAnimationViewPrior = findViewById(R.id.animation_view_prior)

        //init firebase
        mFirebaseAuth = FirebaseAuth.getInstance()

        //init upload button
        uploadButton = findViewById(R.id.UploadBtn)

        //init view image
        viewImage = findViewById(R.id.IdProf)
    }

    private fun setClicks() {
        Log.i(tag, "setOnclickListener for uploadbutton and imageview")
        viewImage.setOnClickListener{selectImage() }
//        uploadButton.setOnClickListener { uploadImageUrlToFireStore() }
    }
    var anim = false
    private fun selectImage() {
        if (!anim) {
            ImagePicker.with(this)
                .compress(1024)
                .maxResultSize(1080,1080)
                .createIntent { intent ->
                    retrievePhotoWithResults.launch(intent)
                    anim = true
                }
        }
    }

    private fun geoHashForMe(mStack: Stack) : String {
        //Create location variable
        var location = Location("geohash")
        location.latitude = mStack?.lat.toDouble()
        location.longitude = mStack?.lng.toDouble()

        val hash = GeoHash.fromLocation(location, 9)
        return hash.toString() //"v12n8trdj"
    }


}