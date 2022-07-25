package com.ekm.hairdo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.ekm.hairdo.ViewModels.StackViewModelCustomer
import com.ekm.hairdo.adapters.StackAdapter
import com.ekm.hairdo.listener.CustomStackAdapterListener
import com.ekm.hairdo.things.Stack
import com.ekm.hairdo.things.StackDiffCallback
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.yuyakaido.android.cardstackview.*


class CardActivityCustomer : AppCompatActivity(), CardStackListener, CustomStackAdapterListener {

    private var TAG = "CardActivityCustomer"
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
    var mStackAdapter: StackAdapter? = null
    //Init layout manager
    var manager: CardStackLayoutManager? = null
    //Init list of cards
    lateinit var mStacks: ArrayList<Stack>

    lateinit var name: TextView

    var mDirection = Direction.Bottom



    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: AuthStateListener

    private val stackViewModelCustomer: StackViewModelCustomer by viewModels()

    // Show cardstacks on create but first, we need to downloads a random set of stacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
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

            }
        }


    }

    //Init otherViews
    private fun initOtherViews() {
        //Temporary and to be deleted
        Log.i(TAG, "initialize other views")
        //Temporary and to be deleted
        name = findViewById(R.id.namme)
    }

    //Init recyclerview and stackadapter
    private fun initRecyclerViewAndAdapter() {
        Log.i(TAG, "initials recycler")
        mStackView = findViewById(R.id.activity_main_card_stack)
        mStacks = ArrayList()
        mStackAdapter = StackAdapter(this, mStacks, this)
        manager = CardStackLayoutManager(this, this)
        initializeCardsSettings()
        stackViewModelCustomer.getfirstStacks()
        stackViewModelCustomer.stacksViewModel.observe(this, Observer {
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
        //Load new cards if getting close to the end of the list
        if (manager!!.topPosition == mStackAdapter!!.itemCount - 5) {
            println("lets paginate")
            //load new cards
            stackViewModelCustomer.getNewStacks()
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

    //Launch chat activity if logged in
    override fun onChatButtonClicked(currentStack: Stack?) {
        //TODO check logged in state from ViewModel
        if (isUidPresent) {
        val myIntent = Intent(this, ChatActivity::class.java)
        myIntent.putExtra(vars.otherUID, currentStack!!.stylistId) //Optional parameters
        startActivity(myIntent)}
        else {
          //TO DO sign in and go to chat
        }
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
        name.setText(displayName)
        displayname = displayName.toString()
        initRecyclerViewAndAdapter()
        stackViewModelCustomer.addToUserList(displayName.toString(), uid)

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