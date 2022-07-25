package com.ekm.hairdo.ViewModels

import android.util.Log
import androidx.lifecycle.*
import com.ekm.hairdo.Stylist.UploadPhoto2
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.Stack.Companion.toStack
import com.ekm.hairdo.things.user
import com.ekm.hairdo.vars
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch

class DesignPageViewModel : ViewModel() {

    private val TAG = "DesignPageViewModel"

    //Object mapper
    lateinit var mapper: ObjectMapper

    // Access a Cloud Firestore instance from your Activity
    var db = FirebaseFirestore.getInstance()

    lateinit var lastVisible: DocumentSnapshot

    //Get Data
    private val _stacksViewModel = MutableLiveData<ArrayList<Stack>>()
    private val _mUser = MutableLiveData<user>()
    //Convert data into LiveData (not mutable)
    val stacksViewModel: LiveData<ArrayList<Stack>>
        get() = _stacksViewModel
    val mUser: LiveData<user>
        get() = _mUser

    var mFirebaseAuth: FirebaseAuth = getInstance()

     private var _uid = ""
    val uid:String
        get() = _uid
     var displayName = ""
     private var isUidPresent = false
    private var dataLoaded = false
    private var attempToGetAddress = false
    private lateinit var registration: ListenerRegistration
    init {
        // init firebaseauth and listener here if there is an error
      //  Log.i(TAG, "init mFirebaseAuth")
        //mFirebaseAuth = getInstance()
        //Log.i(TAG, "init phase")
        //init here
        Log.i(TAG, "init mapper")
        mapper = ObjectMapper()
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    //Get initial set of stacks
     fun createStacksFromDatabase(uid: String, displayName: String) {
        _uid = uid
        this.displayName = displayName
        if (!dataLoaded)
        viewModelScope.launch {
            Log.i(TAG, "Loading data in designviewmodel")
            getAdress()
            val first = db.collection(vars.STYLES).whereEqualTo("stylistId", uid)
            registration = first.addSnapshotListener {
                documents, e->
                var freshStacks: ArrayList<Stack> = ArrayList()
                if (documents != null && !documents.isEmpty) {
                    Log.i(TAG, "found ${documents.size()} documents")
                    if (documents.size() > 0) {
                       // lastVisible = documents.documents.get(documents.size()-1)
                        for (document in documents) {
                            val stack = document.toStack()
                            freshStacks.add(stack!!)
                            println(stack.url)
                        }
                    }
                    _stacksViewModel.postValue(freshStacks)
                    dataLoaded = true

                }



            }
//            first.get()
//                .addOnSuccessListener { documents ->
//                    var freshStacks: ArrayList<Stack> = ArrayList()
//                    if (documents.size() > 0) {
//                        lastVisible = documents.documents.get(documents.size()-1)
//                        for (document in documents) {
//                            val stack = document.toStack()
//                            freshStacks.add(stack!!)
//                            println(stack.url)
//                        }
//                    }
//                    _stacksViewModel.postValue(freshStacks)
//                    dataLoaded = true
//                    getAdress()
//
//                }
//                .addOnFailureListener { exception ->
//                    Log.w(TAG, "Error getting Stacks: ", exception)
//                }
        }
    }

    fun getAdress(){
        if (!attempToGetAddress) {
        viewModelScope.launch {
            val docRef = db.collection(vars.USERS_DATA).document(uid)
            docRef.get().addOnSuccessListener {
                document -> if (document.exists()) {
                    val mUser: user = mapper.convertValue(document.data, user::class.java)
                    attempToGetAddress = true
                Log.i(TAG, "reading address in getAddress with ${mUser.address}")
                    _mUser.postValue(mUser)
            }
            }
        }}
    }

    fun submitAddress(mAddress: String, mLat: String, mLng: String) {
        //Upload address to user file
        viewModelScope.launch {
            val mUser = user()
            mUser.address = mAddress
            mUser.name = displayName
            mUser.latitude = mLat
            mUser.longitude = mLng
            val map = mapper.convertValue<Map<String, Any>>(
                mUser,
                object : TypeReference<Map<String?, Any?>?>() {})
            db.collection(vars.USERS_DATA).document(uid)
                .set(map, SetOptions.merge())
                .addOnSuccessListener {
                   _mUser.postValue(mUser)
                    Log.i(TAG, "Posting address")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error writing address to user file")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "Clearing everything in viewmodel")
        //Remove listerner register
        registration.remove()
        Log.i(TAG, "Registration removed")
    }




}