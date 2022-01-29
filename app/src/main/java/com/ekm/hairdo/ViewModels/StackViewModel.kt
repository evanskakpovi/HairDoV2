package com.ekm.hairdo.ViewModels

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.ekm.hairdo.Stylist.CardActivityST
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.Stack.Companion.toStack
import com.ekm.hairdo.things.StackDiffCallback
import com.ekm.hairdo.things.user
import com.ekm.hairdo.vars
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class StackViewModel : ViewModel() {

    private val TAG = "StackViewModel"

    lateinit var mapper: ObjectMapper

    //Get Data
    private val _stacksViewModel = MutableLiveData<ArrayList<Stack>>()
    //Convert data into LiveData (not mutable)
    val stacksViewModel: LiveData<ArrayList<Stack>>
    get() = _stacksViewModel

    // Access a Cloud Firestore instance from your Activity
    var db = FirebaseFirestore.getInstance()

    lateinit var lastVisible: DocumentSnapshot
    
    init {
        mapper = ObjectMapper()
//        getFreshStacks()
    }

    //Get initial set of stacks
    public fun getFreshStacks() {
        viewModelScope.launch {
            val next = db.collection(vars.STYLES).limit(6)

            next.get()
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

                    }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting Stacks: ", exception)
                }
        }
    }

    //Get new additional set of stacks
    fun getNewStacks() {
        viewModelScope.launch {
            val next = db.collection(vars.STYLES).startAfter(lastVisible).limit(10)

            next.get()
                .addOnSuccessListener { documents ->
                    if (documents.size() > 0) {
                        lastVisible = documents.documents.get(documents.size()-1)
                        var freshStacks: ArrayList<Stack> = ArrayList()
                        for (document in documents) {
                            val stack = document.toStack()
                            freshStacks.add(stack!!)
                            println(stack.url)
                        }

                        //Retrieve current stacks first, and include in new stacks
                        _stacksViewModel.postValue(freshStacks)
                        println("size ${_stacksViewModel.value?.size}")


                    }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting Stacks: ", exception)
                }
        }
    }


    //Rest of your viewmodel
    fun addToUserList(displayname: String, uid: String) {
        val mUser = user()
        mUser.name = displayname
        // Convert POJO to Map
        val map: Map<String, Any> = mapper.convertValue<Map<String, Any>>(
            mUser,
            object : TypeReference<Map<String?, Any?>?>() {})
        db.collection(vars.USERS_DATA).document(uid)
            .set(map)
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "User name $displayname has been added to database"
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing user name to database",
                    e
                )
            }
    }


}