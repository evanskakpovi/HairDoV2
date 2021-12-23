package com.ekm.hairdo.Stylist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import com.ekm.hairdo.ChatActivity;
import com.ekm.hairdo.R;
import com.ekm.hairdo.adapters.StackAdapterST;
import com.ekm.hairdo.listener.CustomStackAdapterListener;
import com.ekm.hairdo.things.Stack;
import com.ekm.hairdo.things.StackDiffCallback;
import com.ekm.hairdo.things.user;
import com.ekm.hairdo.vars;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class CardActivityST extends AppCompatActivity implements CardStackListener, CustomStackAdapterListener {
    private static final String TAG = "CardActivity" ;
    CardStackView mStackView;
    StackAdapterST mStackAdapter;
    CardStackLayoutManager manager;
    ArrayList<Stack> mStacks;
    public static final int RC_SIGN_IN = 1002;
    public ObjectMapper mapper;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    DocumentSnapshot lastVisible;
    Direction mDirection = Direction.Bottom;
    private String uid = "";
    private boolean isUidPresent;
    Button showtalent;
   //Temporary fields
    private TextView name;
    Button addButton;
    String displayname;
    // [START auth_fui_create_launcher]
    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    // [END auth_fui_create_launcher]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardst);

        //Temporary and to be deleted
        name = findViewById(R.id.namme);
        addButton = findViewById(R.id.add);
        //All permanent bellow

        //setup button
        showtalent = findViewById(R.id.message);

         mapper = new ObjectMapper(); // jackson's objectmapper
         mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

         mStackView= findViewById(R.id.activity_main_card_stackst );
         mStacks = new ArrayList();
         mStackAdapter = new StackAdapterST(this, mStacks, this);
         manager = new CardStackLayoutManager(this, this);


        mFirebaseAuth = FirebaseAuth.getInstance();
        //Auth Listener Setup
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                System.out.println(user);
                if (user !=null ) {
                    //user is signin
                    onSignedInInitialized(user.getDisplayName(), user.getUid());
                 //   Toast.makeText(CardActivityST.this, "good", Toast.LENGTH_LONG);
                } else {
                    //user is signed out
                    onSignedOutCleanup();
                }
            }
        };

        setupCardStackView();
    }

    private void onSignedInInitialized(String displayName, String uid) {
        this.uid = uid;
        isUidPresent = true;
        invalidateOptionsMenu();
        name.setText(displayName);
        displayname = displayName;
        addToUserList();
        goToDashboard();
    }

    private void addToUserList() {

        user mUser = new user();
        mUser.setName(displayname);
        // Convert POJO to Map
        Map<String, Object> map =
                mapper.convertValue(mUser, new TypeReference<Map<String, Object>>() {});
        db.collection(vars.USERS_DATA).document(uid)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!1");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    private void onSignedOutCleanup() {
        uid = "";
        isUidPresent = false;
        invalidateOptionsMenu();
    }
    private void setupCardStackView() {
        initialize();
    }

    private void initialize() {


        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        //manager.setOverlayInterpolator(LinearInterpolator());
        mStackView.setLayoutManager(manager);
        mStackView.setAdapter(mStackAdapter);
        mStackView.setItemAnimator(new DefaultItemAnimator());

        showtalent.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
            signin();
                                         }
                                     }
        );
        createSpots();
    }
    //TODO use code bellow when creating styles
/**
    private String geoHashForMe(Stack mstack) {
        Location location = new Location("geohash");
        location.setLatitude(mstack.getLat());
        location.setLongitude(mstack.getLng());

        GeoHash hash = GeoHash.fromLocation(location, 9);
      return  hash.toString(); //"v12n8trdj"
    }

    private void addhashstyles() {
        // Convert POJO to Map
        for (int i=0; i<mStacks.size(); i++){
            mStacks.get(i).setDistance(geoHashForMe(mStacks.get(i)));
            Map<String, Object> map =
                    mapper.convertValue(mStacks.get(i), new TypeReference<Map<String, Object>>() {});
            final int finalI = i;
            db.collection("HAIR_STYLES").document(mStacks.get(i).getStylistId()+"___"+finalI)
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, finalI + "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }


    }
**/
    private void createSpots() {

        Query first = db.collection(vars.STYLES).limit(6);
                first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size()>0) {
                            System.out.println("Task: "+task.getResult().size());
                            lastVisible = task.getResult().getDocuments()
                                    .get(task.getResult().size() -1);
                            System.out.println("last visible: "+lastVisible);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Stack mStack = mapper.convertValue(document.getData(), Stack.class);
                                mStacks.add(mStack);

                            }
                            Collections.shuffle(mStacks);
                            mStackAdapter.notifyDataSetChanged();
                            mStackView.setVisibility(View.VISIBLE);
                       //
                           // getFavorites();

                        } else {
                            mStackView.setVisibility(View.VISIBLE);
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

autoSwipe();

    }
    public void autoSwipe() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Left)
                        .setDuration(Duration.Normal.duration)
                        .setInterpolator(new AccelerateInterpolator())
                        .build();
                manager.setSwipeAnimationSetting(setting);
                mStackView.swipe();
               //todo autoswipe
                // autoSwipe();
            }
        }, 3000);
    }
    private void createSpotsNew() {

        Query next = db.collection(vars.STYLES).startAfter(lastVisible).limit(10);

                next.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size()>0) {
                            System.out.println("Task: "+task.getResult().size());
                            if (task.getResult().size()>0)
                            lastVisible = task.getResult().getDocuments()
                                    .get(task.getResult().size() -1);

                            ArrayList<Stack> freshStacks = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Stack mStack = mapper.convertValue(document.getData(), Stack.class);
                                freshStacks.add(mStack);
                            }
                            //Add extra pages
                            paginate(freshStacks);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void paginate(ArrayList<Stack> freshStacks) {
        //Retrieve current stacks first, and include in new stacks
        ArrayList<Stack> newStacks = mStackAdapter.getmStacks();
        newStacks.addAll(freshStacks);
        StackDiffCallback callback = new StackDiffCallback(mStackAdapter.getmStacks(), newStacks);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        mStackAdapter.setmStacks(newStacks);
        result.dispatchUpdatesTo(mStackAdapter);
    }


    @Override
    public void onCardDragging(Direction direction, float ratio) {

    }

    @Override
    public void onCardSwiped(Direction direction) {
        System.out.println(direction);
        mDirection = direction;
//        name.setText(displayname+!mFav.getmFavs().contains(id));
        if (manager.getTopPosition() == mStackAdapter.getItemCount() - 5) {
            System.out.println("lets paginate");
            createSpotsNew();
        }


    }

    @Override
    public void onCardRewound() {

    }

    @Override
    public void onCardCanceled() {

    }

    @Override
    public void onCardAppeared(View view, int position) {


    }

    @Override
    public void onCardDisappeared(View view, int position) {

    }

    @Override
    public void onRewind() {
        RewindAnimationSetting setting = new RewindAnimationSetting.Builder()
                .setDirection(mDirection)
                .setDuration(Duration.Fast.duration)
                .setInterpolator(new DecelerateInterpolator())
                .build();
        manager.setRewindAnimationSetting(setting);
        mStackView.rewind();
    }

    @Override
    public void onChatButtonClicked(Stack currentStack) {
        if (uid.equals("")) {signin();}

        else {
        Intent myIntent = new Intent(CardActivityST.this, ChatActivity.class);
        myIntent.putExtra(vars.otherUID, currentStack.getStylistId()); //Optional parameters
        startActivity(myIntent);
    }}

    @Override
    public void addFavorite(boolean isChecked, String hairid) {}

    private void signin() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

    // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //TODO on signin
        } else {
           //TODO Error handling
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.signin:
                //   signin user
                signin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.signout, menu);
        return true;
    }

    //Update option menu content visibility
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Check UID status
        if (isUidPresent) {
            //UID is present. Give option for login out
            menu.findItem(R.id.signout).setVisible(true);
            menu.findItem(R.id.signin).setVisible(false);
        } else {
            //UID is not present. Give option to login
            menu.findItem(R.id.signout).setVisible(false);
            menu.findItem(R.id.signin).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    private void goToDashboard() {
        Intent myIntent = new Intent(CardActivityST.this, DesignPage.class);
        // myIntent.putExtra(var.stylistID, currentStack.getStylistId()); //Optional parameters
        startActivity(myIntent);
        finish();
    }
}
