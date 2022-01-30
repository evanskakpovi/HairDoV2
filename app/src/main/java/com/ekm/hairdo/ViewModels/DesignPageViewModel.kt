package com.ekm.hairdo.ViewModels

import android.nfc.Tag
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.ekm.hairdo.R
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.Stack.Companion.toStack
import com.ekm.hairdo.things.user
import com.ekm.hairdo.vars
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

    var mFirebaseAuth: FirebaseAuth

    private var uid = ""
    private var isUidPresent = false
    private var dataLoaded = false
    private var attempToGetAddress = false
    init {
        // init firebaseauth and listener here if there is an error
        Log.i(TAG, "init mFirebaseAuth")
        mFirebaseAuth = getInstance()
        Log.i(TAG, "init phase")
        //init here
        Log.i(TAG, "init mapper")
        mapper = ObjectMapper()
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    //Get initial set of stacks
     fun createStacks(uid: String) {
        this.uid = uid
        if (!dataLoaded)
        viewModelScope.launch {
            val first = db.collection(vars.STYLES).whereEqualTo("stylistId", uid)

            first.get()
                .addOnSuccessListener { documents ->
                    if (documents.size() > 0) {
                        lastVisible = documents.documents.get(documents.size()-1)
                        var freshStacks: ArrayList<Stack> = ArrayList()
                        for (document in documents) {
                            val stack = document.toStack()
                            freshStacks.add(stack!!)
                            println(stack.url)
                        }
                        _stacksViewModel.postValue(freshStacks)
                        dataLoaded = true
                        getAdress()
                    }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting Stacks: ", exception)
                }
        }
    }

    fun getAdress(){
        if (!attempToGetAddress)
        viewModelScope.launch {
            val docRef = db.collection(vars.USERS_DATA).document(uid)
            docRef.get().addOnSuccessListener {
                document -> if (document.exists()) {
                    val mUser: user = mapper.convertValue(document.data, user::class.java)
                    attempToGetAddress = true
                    _mUser.postValue(mUser)
            }
            }
        }
    }




}