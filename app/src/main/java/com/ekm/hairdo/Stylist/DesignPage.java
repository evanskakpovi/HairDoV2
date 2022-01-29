package com.ekm.hairdo.Stylist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ekm.hairdo.CardActivity;
import com.ekm.hairdo.ChatGroupActivity;
import com.ekm.hairdo.R;
import com.ekm.hairdo.adapters.StackDesignAdapterST;
import com.ekm.hairdo.listener.CustomStackDesignAdapterListener;
import com.ekm.hairdo.things.Stack;
import com.ekm.hairdo.things.StackD;
import com.ekm.hairdo.things.user;
import com.ekm.hairdo.vars;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.yuyakaido.android.cardstackview.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class DesignPage extends AppCompatActivity implements CustomStackDesignAdapterListener {

    private static final String TAG = "CardActivity" ;
    RecyclerView mStackView;
    StackDesignAdapterST mStackAdapter;
    RecyclerView.LayoutManager manager;
    ArrayList<Stack> mStacks;
    public static final int RC_SIGN_IN = 1002;
    public ObjectMapper mapper;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    DocumentSnapshot lastVisible;

    private String uid = "";
    private boolean isUidPresent;
    Button showtalent;
    //Temporary fields
    Button uploadButton;
    Button chat,chat2;
    String displayname;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        uploadButton = findViewById(R.id.button);
        uploadButton.setOnClickListener(view -> goToUpload());
        chat = findViewById(R.id.chatbubble);
        chat2 = findViewById(R.id.chatbubble2);
        chat.setOnClickListener(view -> goToChat());
        chat2.setOnClickListener(view -> goToCardActivity());

        String apiKey = getString(R.string.api_key);
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        mStacks = new ArrayList();
        mStackView= findViewById(R.id.mrecyclerlist );
        mStackAdapter = new StackDesignAdapterST(this, mStacks, this);
        manager = new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        mStackView.setLayoutManager(manager);
        mStackView.setAdapter(mStackAdapter);
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Auth Listener Setup
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            System.out.println(user);
            if (user !=null ) {
                //user is signin
                onSignedInInitialized(user.getDisplayName(), user.getUid());
                Toast.makeText(DesignPage.this, "good", Toast.LENGTH_LONG);
            } else {
                //user is signed out
                onSignedOutCleanup();
            }
        };
    }

    private void goToChat() {
        Intent myIntent = new Intent(DesignPage.this, ChatGroupActivity.class);
        myIntent.putExtra(vars.otherUID, uid); //Optional parameters
        startActivity(myIntent);
    }

    //Opening customer activity page!
    private void goToCardActivity() {
        Intent myIntent = new Intent(DesignPage.this, CardActivity.class);
        myIntent.putExtra(vars.otherUID, uid); //Optional parameters
        startActivity(myIntent);
    }

    private void onSignedInInitialized(String displayName, String uid) {
        this.uid = uid;
        isUidPresent = true;
        invalidateOptionsMenu();
        createSpots();
        mStackView.invalidate();
    }
    private void onSignedOutCleanup() {
        uid = "";
        isUidPresent = false;
        invalidateOptionsMenu();
    }

    private void getadress() {
        DocumentReference docRef = db.collection(vars.USERS_DATA).document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    user mUser = mapper.convertValue(document.getData(), user.class);
                    System.out.println(mUser.getAddress()+"-------------------");

                    if (mUser.getAddress()==null)
                    {
                        Intent myIntent = new Intent(DesignPage.this, AddressShowActivity.class);
                        myIntent.putExtra(vars.otherUID, uid); //Optional parameters
                        startActivity(myIntent);
                    } else {
                        Intent myIntent = new Intent(DesignPage.this, UploadPhoto2.class);
                        myIntent.putExtra(vars.otherUID, uid); //Optional parameters
                        myIntent.putExtra(vars.stylistAddress, mUser.getAddress()); //Optional parameters
                        myIntent.putExtra(vars.stylistLat, mUser.getLatitude()); //Optional parameters
                        myIntent.putExtra(vars.stylistLng, mUser.getLongitude()); //Optional parameters
                        startActivity(myIntent);
                    }
                }
                else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void createSpots() {
        Query first = db.collection(vars.STYLES).whereEqualTo("stylistId", uid);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() &&task.getResult().size()>0) {
                            mStacks.clear();
                            //System.out.println("Task: "+task.getResult().size());
                            lastVisible = task.getResult().getDocuments()
                                    .get(task.getResult().size() -1);
                           // System.out.println("last visible: "+lastVisible);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Stack mStack = mapper.convertValue(document.getData(), Stack.class);
                                mStacks.add(mStack);
                                mStackAdapter.notifyDataSetChanged();
                                //System.out.println(document);
                            }
                            mStackAdapter.notifyDataSetChanged();
                            mStackView.setVisibility(View.VISIBLE);
//                            getFavorites();

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            addToUserList();
                        }
                    }
                });
    }

    private void goToUpload() {
        getadress();
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
    public void onRewind() {

    }

    @Override
    public void onChatButtonClicked(Stack currentStack) {

    }

    @Override
    public void addFavorite(boolean isChecked, String hairid) {

    }
    @Override
    public void onEmpty() {
        TextView info = findViewById(R.id.infotext);
        info.setVisibility(View.VISIBLE);
        chat.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNotEmpty() {
        TextView info = findViewById(R.id.infotext);
        info.setVisibility(View.GONE);
        chat.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                //sign out
                AuthUI.getInstance().signOut(this);
                finish();
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
    private void signin() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }
}