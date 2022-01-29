package com.ekm.hairdo.Stylist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.ekm.hairdo.R
import com.ekm.hairdo.ViewModels.StackViewModel
import com.ekm.hairdo.adapters.StackAdapterST
import com.ekm.hairdo.listener.CustomStackAdapterListener
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.StackDiffCallback
import com.fasterxml.jackson.databind.ObjectMapper
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.yuyakaido.android.cardstackview.*
import kotlin.collections.ArrayList


class CardActivityStylist : AppCompatActivity(), CardStackListener, CustomStackAdapterListener {

    private var TAG = "CardActivityStylist"
    private var isUidPresent = false;
    private var uid = ""
    private var displayname = ""

    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    //Initialize variables

    //Initialize CardStackView
    lateinit var mStackView: CardStackView
    //Init adapter for cardstacks
    var mStackAdapter: StackAdapterST? = null
    //Init layout manager
    var manager: CardStackLayoutManager? = null
    //Init list of cards
    lateinit var mStacks: ArrayList<Stack>

    lateinit var showTalent: Button

    var mDirection = Direction.Bottom



    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: AuthStateListener

    private val stackViewModel: StackViewModel by viewModels()

    // Show cardstacks on create but first, we need to downloads a random set of stacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cardst)
        //Init views
        initOtherViews()

        //activity_cardst

        mFirebaseAuth = FirebaseAuth.getInstance()
        //Auth Listener Setup
        //Auth Listener Setup
        mAuthStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //user is signin
                onSignedInInitialized(user.displayName, user.uid)
                //   Toast.makeText(CardActivityST.this, "good", Toast.LENGTH_LONG);
            } else {
                //user is signed out
                onSignedOutCleanup()
                //Init recyclerView and Adapter
                initRecyclerViewAndAdapter()
            }
        }


    }

    //Init otherViews
    private fun initOtherViews() {
        showTalent = findViewById(R.id.message)
    }

    //Init recyclerview and stackadapter
    private fun initRecyclerViewAndAdapter() {
        mStackView = findViewById(R.id.activity_main_card_stackst)
        mStacks = ArrayList()
        mStackAdapter = StackAdapterST(this, mStacks, this)
        manager = CardStackLayoutManager(this, this)
        initializeCardsSettings()
        stackViewModel.getNewStacks()
        stackViewModel.stacksViewModel.observe(this, Observer {
            m: ArrayList<Stack> -> paginate(m)
            // Collections.shuffle(mStacks)
            println("real size ${m.size}")

            if (m.size==6) mStackAdapter!!.notifyDataSetChanged()

            mStackView.visibility = View.VISIBLE
        })
    }

    private fun initializeCardsSettings() {
        manager!!.setStackFrom(StackFrom.None)
        manager!!.setVisibleCount(3)
        manager!!.setTranslationInterval(8.0f)
        manager!!.setScaleInterval(0.95f)
        manager!!.setSwipeThreshold(0.3f)
        manager!!.setMaxDegree(20.0f)
        manager!!.setDirections(Direction.HORIZONTAL)
        manager!!.setCanScrollHorizontal(true)
        manager!!.setCanScrollVertical(true)
        manager!!.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        //manager.setOverlayInterpolator(LinearInterpolator());
        mStackView?.layoutManager = manager
        mStackView?.adapter = mStackAdapter
        mStackView?.itemAnimator = DefaultItemAnimator()
        showTalent.setOnClickListener{
        signin()
        }

    }
    //  private void paginate(ArrayList<Stack> freshStacks) {
    //        //Retrieve current stacks first, and include in new stacks
    //        ArrayList<Stack> newStacks = mStackAdapter.getmStacks();
    //        newStacks.addAll(freshStacks);
    //        StackDiffCallback callback = new StackDiffCallback(mStackAdapter.getmStacks(), newStacks);
    //        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
    //        mStackAdapter.setmStacks(newStacks);
    //        result.dispatchUpdatesTo(mStackAdapter);
    //    }

    private fun paginate(freshStacks: ArrayList<Stack>) {
        //Retrieve current stacks first, and include in new stacks
        println("Fresh stacks size ${freshStacks.size}")
        val newStacks = mStackAdapter!!.getmStacks()
        newStacks.addAll(freshStacks)
        val callback = StackDiffCallback(mStackAdapter!!.getmStacks(), newStacks)
        val result = DiffUtil.calculateDiff(callback)
        mStackAdapter!!.setmStacks(newStacks)
        result.dispatchUpdatesTo(mStackAdapter!!)
    }


    override fun onCardDragging(direction: Direction, ratio: Float) {

    }

    override fun onCardSwiped(direction: Direction) {
        println(direction)
        mDirection = direction
//        name.setText(displayname+!mFav.getmFavs().contains(id));
        //        name.setText(displayname+!mFav.getmFavs().contains(id));
        if (manager!!.topPosition == mStackAdapter!!.itemCount - 5) {
            println("lets paginate")
            stackViewModel.getNewStacks()
        }
    }

    override fun onCardRewound() {

    }

    override fun onCardCanceled() {

    }

    override fun onCardAppeared(view: View?, position: Int) {

    }

    override fun onCardDisappeared(view: View?, position: Int) {

    }

    override fun onRewind() {
        val setting = RewindAnimationSetting.Builder()
            .setDirection(mDirection)
            .setDuration(Duration.Fast.duration)
            .setInterpolator(DecelerateInterpolator())
            .build()
        manager!!.setRewindAnimationSetting(setting)
        mStackView.rewind()
    }

    override fun onChatButtonClicked(currentStack: Stack?) {

    }

    override fun addFavorite(isChecked: Boolean, hairid: String?) {

    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "onSignInResult: ${result.resultCode}")
           // signout()
            // ...
        } else {
            Log.e(TAG, "onSignInResult: ERROR WITH LOGIN")
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    private fun signin() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
           // AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            //AuthUI.IdpConfig.FacebookBuilder().build(),
            //AuthUI.IdpConfig.TwitterBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun signout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
    }

    private fun onSignedInInitialized(displayName: String?, uid: String) {
        this.uid = uid
        isUidPresent = true
        invalidateOptionsMenu()
        var name: TextView = findViewById(R.id.namme)
        name.setText(displayName)
        displayname = displayName.toString()
        stackViewModel.addToUserList(displayName.toString(), uid)
        goToDashboard()
    }

    private fun goToDashboard() {
        val myIntent = Intent(this, DesignPageK::class.java)
        // myIntent.putExtra(var.stylistID, currentStack.getStylistId()); //Optional parameters
        startActivity(myIntent)
        finish()
    }

    private fun onSignedOutCleanup() {
        uid = ""
        isUidPresent = false
        invalidateOptionsMenu()
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