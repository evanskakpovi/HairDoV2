package com.ekm.hairdo.Stylist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ekm.hairdo.*
import com.ekm.hairdo.ViewModels.DesignPageViewModel
import com.ekm.hairdo.adapters.StackDesignAdapterST
import com.ekm.hairdo.listener.CustomStackDesignAdapterListener
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.user
import com.google.firebase.auth.FirebaseAuth


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



    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    private var isUidPresent = false
    lateinit private var mUser: user



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        Log.i(tag, "On create")
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
                Log.i(tag, "user is $user and uid is ${user.uid}")
                uploadButton.setText("Upload my work -${user.displayName.toString()}")
                designPageViewModel.createStacksFromDatabase(user.uid, user.displayName.toString())
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


    }

    private fun loaduserDataFromViewModelWhenReady() {
        Log.i(tag, "LoadUserDataFromViewModel")
        designPageViewModel.mUser.observe(this, Observer {
                mUser: user -> this.mUser = mUser

            //First, check for address. If address is present, go to upload activity, otherwise, get address first.
            if (mUser?.address==null) {
                val myIntent = Intent(this, AddressShowActivity::class.java)
                myIntent.putExtra(vars.otherUID, designPageViewModel.uid) //Optional parameter
                Log.i(tag, "starting address activity for result")
                addressActivityResultLauncher.launch(myIntent)
            } else {
                Log.i(tag, "Opening UploadPhotoK")
                val myIntent = Intent(this, UploadPhotoK::class.java)
                myIntent.putExtra(vars.otherUID, designPageViewModel.uid) //Optional parameters
                myIntent.putExtra(vars.stylistAddress, mUser.address) //Optional parameters
                myIntent.putExtra(vars.stylistLat, mUser.latitude) //Optional parameters
                myIntent.putExtra(vars.stylistLng, mUser.longitude) //Optional parameters
                startActivity(myIntent)
            }
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
            //Get user data in order to retrieve address
            designPageViewModel.getAdress()
        })
    }

    //Debug feature -- This will not ship with production version
    //this button allows easy navigation to customer facing version of cardview for testing
    private fun goToCardActivity() {
       //Create an intent for CardActivity and start intent
        val myIntent = Intent(this, CardActivityCustomer::class.java)
        myIntent.putExtra(vars.otherUID, designPageViewModel.uid) //Optional parameters
        startActivity(myIntent)
    }

    private fun goToChat() {
        val myIntent = Intent(this, ChatGroupActivity::class.java)
        myIntent.putExtra(vars.otherUID, designPageViewModel.uid) //Optional parameters
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
            //TODO use viewmodel save address data to user data
            designPageViewModel.submitAddress(
                data?.getStringExtra(vars.stylistAddress).toString(),
                data?.getStringExtra(vars.stylistLat).toString(),
                data?.getStringExtra(vars.stylistLng).toString()
            )
        }
    }
    private fun goToUploadActivity() {
        Log.i(tag, "Upload clicked")
        loaduserDataFromViewModelWhenReady()
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
//        uid = ""
        isUidPresent = false
        invalidateOptionsMenu()
        Log.i(tag, "uid is ${designPageViewModel.uid} and isuidpresent is $isUidPresent and allowing menu to allow for login" )
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }
    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    fun fib(n: Int): Int {
        return if (n <= 1) n else fib(n - 1) + fib(n - 2)
    }


}