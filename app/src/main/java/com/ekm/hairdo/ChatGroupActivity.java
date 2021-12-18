package com.ekm.hairdo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ekm.hairdo.adapters.ChatGroupAdapter;
import com.ekm.hairdo.listener.UsergroupListener;
import com.ekm.hairdo.things.ChatDetails;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ChatGroupActivity extends AppCompatActivity implements UsergroupListener {

    private static final String ANONYMOUS = "ANONYMOUS";
    private static final String TAG = "MESSAGE_APP" ;
    private RecyclerView mRecyclerView;
    private ChatGroupAdapter mMessageAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ChatDetails> myDataset;

    //Firebase stuff
    private ChildEventListener mchildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseStorage mFirebaseStorage;

    private String mUsername= ANONYMOUS;

    public ObjectMapper mapper;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DocumentSnapshot lastVisible;
    private String uid;
    private boolean isReadyToSend;

    String otherID;

    String personMe = var.NONE;
    String personOther = var.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);

        // jackson's objectmapper
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Bundle extras = getIntent().getExtras();
        otherID = extras.getString(var.otherUID);

        //Init firebase Components
        initFirebaseStuff();

        //Auth Listener Setup
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user !=null ) {
                //user is signin
                onSignedInInitialized(user.getDisplayName(), user.getUid());
                Toast.makeText(ChatGroupActivity.this, "good", Toast.LENGTH_LONG);
            } else {
                //ToDo update signingcode
                // user is signed out
                onSignedOutCleanup();
            }
        };
    }

    private void setUpRecyclerViewWithAdapter() {
        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next ekm)
        mRecyclerView.setAdapter(mMessageAdapter);

    }

    private String getOtherUid(String pattern) {
        //Decode other party id from chatlink pattern sample as follow ROva7D1ONCOt3jJ79BWMRJAF4I53_chatlink_w2hYtJc7VTQKODnUTLyJ0mD2GGE2
        //Compare first part to current user id. If no match, return second part
        String part1 = pattern.substring(0,pattern.lastIndexOf("_chatlink_"));
        if (!part1.equals(uid)) {
            System.out.println(part1);
            return part1;}
        else {
        String part2 = pattern.substring(pattern.lastIndexOf("_chatlink_")+10);
            System.out.println(part2);
            return part2;}
    }

    private void initFirebaseStuff() {
        //init firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    private void initViews() {
        //Iniatilizing references to views
        myDataset = new ArrayList<>();
        mRecyclerView =  findViewById(R.id.mview);
        mLayoutManager = new LinearLayoutManager(this);
        mMessageAdapter = new ChatGroupAdapter(myDataset, R.layout.user_group, uid, this);
    }

    //---------------------------------BOILER CODE
    private void dettachDatabaseListener() {
        if (chatDetailRegistration !=null) {
            chatDetailRegistration.remove();
        }
    }
    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        if (myDataset!=null)
        myDataset.clear();
        mMessageAdapter.notifyDataSetChanged();
        dettachDatabaseListener();
    }
    private void onSignedInInitialized(String displayName, String uid) {
        mUsername = displayName;
        this.uid = uid;
        activateChat();
        attachDatabaseReadListener();

        //Iniatilizing references to views
        initViews();

        //Setup recyclerview with adapter
        setUpRecyclerViewWithAdapter();
    }

    private void activateChat() {
        isReadyToSend = true;
    }

    ListenerRegistration chatDetailRegistration;
    //TODO CHECK FOR USER

    private void attachDatabaseReadListener() {
//        Query first = db.collection(var.MESSAGES).whereIn(personCode, Arrays.asList(uid,"other"));
        Query first = db.collection(var.MESSAGES).whereArrayContains("persons", uid);
        chatDetailRegistration = first.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if ( queryDocumentSnapshots.getDocuments().size()>0) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.getDocuments().size() - 1);
                //myDataset.clear();
                // Listen to query changes and update screen
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {

                    switch (dc.getType()) {
                        case ADDED:
                            ChatDetails mDetails = mapper.convertValue(dc.getDocument().getData(), ChatDetails.class);
                            myDataset.add(mDetails);
                            mMessageAdapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            ChatDetails mDetailsEdited = mapper.convertValue(dc.getDocument().getData(), ChatDetails.class);
                            ChatDetails james = Iterables.tryFind(myDataset,
                                    new Predicate<ChatDetails>() {
                                        public boolean apply(ChatDetails c) {
                                            String part1 = mDetailsEdited.getPerson1()+mDetailsEdited.getPerson2();
                                            return part1.equals(c.getPerson1()+c.getPerson2());
                                        }
                                    }).orNull();
                            if (james!=null) {
                            myDataset.set(myDataset.indexOf(james), mDetailsEdited);
                            mMessageAdapter.notifyDataSetChanged();}
                            //Todo notification onpause
                            break;

                    }
                }

            } else {
                        Log.w(TAG, "Error getting documents", e);
                    }

        });
    }

    private String createUidPattern(String myUID, String hisUID) {
        String a = myUID;
        String b = hisUID;

        int c = a.compareTo(b);

        if (c<0) {
            return a+var.CHATLINK+b;
        } else if (c>0) {
            return b+var.CHATLINK+a;
        } else if (c==0) {
            return a+var.CHATLINK+a;
        } else {
            return a+var.CHATLINK+a;}

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
        dettachDatabaseListener();
        myDataset.clear();
        mMessageAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                //sign out
                AuthUI.getInstance().signOut(this);
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

    @Override
    public void onChatClicked(ChatDetails mchat) {
        //TODO OPen chat activity
        System.out.println("before going to chat "+mchat.getName());
       goToChat(getOtherUid(mchat.getName()));
    }

    private void goToChat(String otherUid) {
        System.out.println("Going to chat with "+otherUid);
        Intent myIntent = new Intent(ChatGroupActivity.this, ChatActivity.class);
        myIntent.putExtra(var.otherUID, otherUid); //Optional parameters
        startActivity(myIntent);
    }

}
