package com.ekm.hairdo.Stylist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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

    private var isUidPresent = false;
    private var uid = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //InitViews
        initViews()
        initRecyclerViewAndAdapter()

        uploadButton.setOnClickListener { goToUpload() }

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

        loadDataFromViewModelWhenReady()
    }

    private fun loadDataFromViewModelWhenReady() {
        designPageViewModel.stacksViewModel.observe(this, Observer {
                m: ArrayList<Stack> ->

            // Collections.shuffle(mStacks)
            println("real size ${m.size}")
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

    private fun goToUpload() {
        Log.i(tag, "upload" )
        val easyImage: EasyImage = EasyImage.Builder(this) // Chooser only
            // Will appear as a system chooser title, DEFAULT empty string
            //.setChooserTitle("Pick media")
            // Will tell chooser that it should show documents or gallery apps
            //.setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)  you can use this or the one below
            //.setChooserType(ChooserType.CAMERA_AND_GALLERY)
            // saving EasyImage state (as for now: last camera file link)
             // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
            .setCopyImagesToPublicGalleryFolder(false) // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
            .setFolderName("EasyImage sample") // Allow multiple picking
            .allowMultiple(true)
            .build()
        //TODO uploadActivity start
        easyImage.openChooser(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}