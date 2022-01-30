package com.ekm.hairdo.Stylist

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekm.hairdo.ChatGroupActivity
import com.ekm.hairdo.R
import com.ekm.hairdo.ViewModels.DesignPageViewModel
import com.ekm.hairdo.adapters.StackDesignAdapterST
import com.ekm.hairdo.listener.CustomStackDesignAdapterListener
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.vars
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.yuyakaido.android.cardstackview.*
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaSource

import androidx.annotation.NonNull
import com.ekm.hairdo.things.user

import pl.aprilapps.easyphotopicker.MediaFile

import pl.aprilapps.easyphotopicker.DefaultCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_cardst.*


class DesignPageK : AppCompatActivity(), CustomStackDesignAdapterListener {

    private val designPageViewModel: DesignPageViewModel by viewModels()

    //Initialize CardStackView
    lateinit var mStackView: RecyclerView
    //Init adapter for cardstacks
    lateinit var mStackDesignAdapter: StackDesignAdapterST
    //Init layout manager
    lateinit var manager: RecyclerView.LayoutManager
    //Init list of cards
    lateinit var mStacks: ArrayList<Stack>
    val tag = "DesignPageK"

    lateinit var uploadButton: Button
    lateinit var chat: Button
    lateinit var info: TextView
    //Temporary fields
    lateinit var testUser:Button

    var displayname: String = ""
    lateinit var address: String

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    private var isUidPresent = false
    private var uid = ""
    lateinit private var mUser: user


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //InitViews
        initViews()
        initRecyclerViewAndAdapter()

        uploadButton.setOnClickListener { goToUploadActivity() }

        chat.setOnClickListener { goToChat() }

        //Will not ship with final version
        testUser.setOnClickListener { goToCardActivity() }

        mFirebaseAuth = FirebaseAuth.getInstance()
        //Auth Listener Setup
        //Auth Listener Setup
        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.i(tag, "mAuthstatelistener is not null and uid is ${user.uid}")
                designPageViewModel.createStacks(user.uid)
                designPageViewModel.getAdress()
                //user is signin
                //onSignedInInitialized(user.displayName, user.uid)
                //   Toast.makeText(CardActivityST.this, "good", Toast.LENGTH_LONG);
            } else {
                Log.i(tag, "mAuthstatelistern is null, let's clean up, and load default")
                //user is signed out
                onSignedOutCleanup()
                //Init recyclerView and Adapter
                initRecyclerViewAndAdapter()
            }
        }

        //Starting the loading of data from viewmodel
        loadDataFromViewModelWhenReady()
        loaduserDataFromViewModelWhenReady()

    }

    private fun loaduserDataFromViewModelWhenReady() {
        designPageViewModel.mUser.observe(this, Observer {
                mUser: user -> this.mUser = mUser
    })
    }

    private fun loadDataFromViewModelWhenReady() {
        designPageViewModel.stacksViewModel.observe(this, Observer {
                m: ArrayList<Stack> ->

            // Collections.shuffle(mStacks)
            Log.i(tag,"There are ${m.size} cards downloaded")
            mStacks.addAll(m)
            mStackDesignAdapter.notifyDataSetChanged()
            mStackView.visibility = View.VISIBLE
        })
    }

    //Will not ship with final version
    private fun goToCardActivity() {
        TODO("Not yet implemented")
    }

    private fun goToChat() {
        val myIntent = Intent(this, ChatGroupActivity::class.java)
        myIntent.putExtra(vars.otherUID, uid) //Optional parameters
        startActivity(myIntent)
    }

    //Create a address activity result listerner and register it
    private val addressActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        Log.i(tag, "Result code is ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            Log.i(tag, "the address is ${data?.getStringExtra(vars.stylistAddress)}")
            // send address to database using viewModel
            //TODO use viewmodel to send address data to user
            //TODO go to uploadpicture activity
        }
    }
    private fun goToUploadActivity() {
            //First, check for address. If address is present, go to upload activity, otherwise, get address first.
            if (mUser?.address==null) {
                val myIntent = Intent(this, AddressShowActivity::class.java)
                myIntent.putExtra(vars.otherUID, uid) //Optional parameter
                Log.i(tag, "starting address activity for result")
                addressActivityResultLauncher.launch(myIntent)
            } else {
                //TODO go to uploadpicture activity
            }
    }


    private fun initViews() {
            uploadButton = findViewById(R.id.button)
            chat = findViewById(R.id.chatbubble)
            testUser = findViewById(R.id.chatbubble2)
            info = findViewById(R.id.infotext)
    }

    //Init recyclerview and stackadapter
    private fun initRecyclerViewAndAdapter() {
        mStackView = findViewById(R.id.mrecyclerlist)
        mStacks = ArrayList()
        mStackDesignAdapter = StackDesignAdapterST(this, mStacks, this)
        manager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

        mStackView.layoutManager = manager
        mStackView.adapter = mStackDesignAdapter
        designPageViewModel.stacksViewModel.observe(this, Observer {
                m: ArrayList<Stack> -> mStackDesignAdapter!!.notifyDataSetChanged()
            // Collections.shuffle(mStacks)
            Log.i(tag, "initial dataset size is ${m.size}")
            mStackView.visibility = View.VISIBLE
        })
    }


    override fun onEmpty() {
        info.visibility = View.VISIBLE
        chat.visibility = View.INVISIBLE
    }

    override fun onNotEmpty() {
        info.visibility = View.INVISIBLE
        chat.visibility = View.VISIBLE
    }

    override fun onRewind() {
        TODO("Not yet implemented")
    }

    override fun onChatButtonClicked(currentStack: Stack?) {
        TODO("Not yet implemented")
    }

    override fun addFavorite(isChecked: Boolean, hairid: String?) {
        TODO("Not yet implemented")
    }

    private fun onSignedOutCleanup() {
        uid = ""
        isUidPresent = false
        invalidateOptionsMenu()
        Log.i(tag, "uid is $uid and isuidpresent is $isUidPresent and allowing menu to allow for login" )
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }
    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }




}