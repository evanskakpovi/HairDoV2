package com.ekm.hairdo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.ekm.hairdo.adapters.ChatAdapter;
import com.ekm.hairdo.things.ChatDetails;
import com.ekm.hairdo.things.Message;
import com.ekm.hairdo.things.user;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.ui.auth.AuthUI;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static com.ekm.hairdo.var.MESSAGES;
import static com.ekm.hairdo.var.NONE;

public class ChatActivity extends AppCompatActivity {

    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1001;
    private static final String ANONYMOUS = "ANONYMOUS";
    private static final String FRIENDLY_MSG_LENGHT_KEY = "com.google.firebase:firebase-config:16.3.0" ;
    private static final String TAG = "MESSAGE_APP" ;
    private static final int RC_PHOTO_PICKER = 1002;
    private RecyclerView mRecyclerView;
    private ChatAdapter mMessageAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Message> myDataset;
    Button sendButton;
    ImageButton mPhotoPickerButton;
    EditText messageEditText;
    private boolean isFileChose;

    //Firebase stuff
    private FirebaseDatabase mDatabase;
    private DatabaseReference mMessageDatabaseReference;
    private ChildEventListener mchildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotoStorageReference;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public ObjectMapper mapper;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DocumentSnapshot lastVisible;
    private String uid = NONE, displayname=NONE;
    private boolean isReadyToSend;

    String stylistId;

    ArrayList<String> personuid = new ArrayList<>();

    user mUser1, mUser2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        // jackson's objectmapper
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Bundle extras = getIntent().getExtras();
        stylistId = extras.getString(var.otherID);

        //Init firebase Components
        initFirebaseStuff();

        //Auth Listener Setup
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user !=null ) {
                    //user is signin
                    onSignedInInitialized(user.getDisplayName(), user.getUid());
                    Toast.makeText(ChatActivity.this, "good", Toast.LENGTH_LONG);
                } else {
                    //user is signed out
                    onSignedOutCleanup();
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
        };




    }

    private void sendMessageToDatabase_parent(String message, boolean image) {

        System.out.println(message+"()()()");
        //Getting the current date
        Date date = new Date();
        //This method returns the time in millis
        long timeMilli = date.getTime();

        Message mMessage = new Message();

        mMessage.setName(displayname);
        if (image) {
            mMessage.setText("Image");
            mMessage.setUrl_message(message);
        } else {
            mMessage.setText(NONE);
        mMessage.setUrl_message(NONE);
        }
        mMessage.setMessage_time(timeMilli);
        mMessage.setKey(uid);

        Map<String, Object> req = mapper.convertValue(mMessage, Map.class);

        String uidPattern = createUidPattern(uid, stylistId);

        System.out.println(uidPattern+"____________________");
        db.collection(uidPattern)
                .document().set(req)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        updateMessageTree_child(uidPattern,mMessage.getText(),  timeMilli);
        updateMessageChatDetails_child(mMessage.getText(), mMessage.getName(), timeMilli, uidPattern);
    }

    private void updateMessageTree_child(String docName, String message, long timemilli) {
        ChatDetails mChatDetails = new ChatDetails();
        mChatDetails.setLast_message(message);
        mChatDetails.setLast_openned_time(timemilli);
        mChatDetails.setName(docName);
        mChatDetails.setPerson1(personuid.get(0));
        mChatDetails.setPerson2(personuid.get(1));
        mChatDetails.setPerson1name(mUser1.getName());
        mChatDetails.setPerson2name(mUser2.getName());
        mChatDetails.setPersons(personuid);

        Map<String, Object> req = mapper.convertValue(mChatDetails, Map.class);

        db.collection(MESSAGES)
                .document(docName).set(req)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void updateMessageChatDetails_child(String message, String name, long timemilli, String uidPattern) {

        ChatDetails mChatDetails = new ChatDetails();
        mChatDetails.setLast_message(message);
        mChatDetails.setLast_openned_time(timemilli);
        mChatDetails.setName("");
        mChatDetails.setPerson1(personuid.get(0));
        mChatDetails.setPerson2(personuid.get(1));
        mChatDetails.setPerson1name(mUser1.getName());
        mChatDetails.setPerson2name(mUser2.getName());
        mChatDetails.setPersons(personuid);

        Map<String, Object> req = mapper.convertValue(mChatDetails, Map.class);

        db.collection(uidPattern)
                .document(var.chatDetails).set(req)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void selectImage() {
        if (!isFileChose){
            ImagePicker.Companion.with(this)
                    .saveDir(new File(Environment.getExternalStorageDirectory(), "HairDo"))
                    //.crop(9f,16f)	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start();
        }}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri fileUri = data.getData();
//            viewImage.setImageURI(fileUri); //TODO load image in chat box
//            Glide.with(this).load(fileUri).into(viewImage);
//            mview.setVisibility(View.VISIBLE);
            isFileChose = true;
//            System.out.println("Result OK With "+fileUri.getPath());
            MediaManager.get()
                    .upload(fileUri)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            System.out.println("On start");

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            //not functional
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            //todo edit filename structure
                            String url = (String) resultData.get("secure_url");
                            String id = (String) resultData.get("public_id");
                            String finalid = id.substring(id.lastIndexOf("hair/")+5);
                          //  sendMessageToDatabase_parent(url,true);
                            //myStack.setHairid(finalid);
                            //myStack.setUrl(url);
                            //addStyleToFirestore(myStack);
                            System.out.println(finalid);
                            System.out.println(resultData);
                            System.out.println("+++++++++++++++done");
                            //todo upload
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            System.out.println(error.getDescription());
                            System.out.println("+++++++++++++++notdone");
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    //.option("public_id", "style_"+uid)
                    //todo change the unsinged code
                    .unsigned("hairdo_default2").dispatch();

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            ///  Toast.makeText(this, ImagePicker, Toast.LENGTH_SHORT).show();
            //todo Display error banner before finish
            // finish();
        } else {
            //todo cancelled process
            //Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private void setUpRecyclerViewWithAdapter() {
        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next ekm)
        mRecyclerView.setAdapter(mMessageAdapter);

    }

    private void sendingMessage() {
        //Sending messages to database
//                Chat mChat = new Chat(messageEditText.getText().toString().trim(), mUsername, null, true);
//                mMessageDatabaseReference.push().setValue(mChat);
        String text = messageEditText.getText()+"";
        if (text.length()>0) {
            sendMessageToDatabase_parent(text,false);
            //Clear message on send
            messageEditText.setText("");
        }
    }

    private void customizeMessageBox() {
        messageEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        messageEditText.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        //Keyboard send action added
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendingMessage();
                    handled = true;
                }
                return handled;
            }
        });
        // Enable Send button when there's text to send
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && isReadyToSend) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        //Customize drawables
        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (messageEditText.getRight() - messageEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        sendingMessage();
                        return true;
                    }
                    else if(event.getRawX() <= (messageEditText.getLeft()+messageEditText.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        // your action here
                        selectImage();
                        return true;
                    }
                }
                return false;
            }
        });
    }



    private String createUidPattern(String myUID, String hisUID) {
        String a = myUID;
        String b = hisUID;

        int c = a.compareTo(b);

        if (c<0) {
            personuid.add(a);
            personuid.add(b);
            return a+var.CHATLINK+b;
        } else if (c>0) {
            personuid.add(b);
            personuid.add(a);
            return b+var.CHATLINK+a;
        } else if (c==0) {
            personuid.add(a);
            personuid.add(a);
            return a+var.CHATLINK+a;
        } else {
            personuid.add(a);
            personuid.add(a);
            return a+var.CHATLINK+a;}

    }

    private void initFirebaseStuff() {
        //init firebase Components
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessageDatabaseReference = mDatabase.getReference().child("messages");
        mChatPhotoStorageReference = mFirebaseStorage.getReference().child("chat_photos");
    }

    private void initViews() {
        //Iniatilizing references to views
        sendButton = findViewById(R.id.sendButton);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        myDataset = new ArrayList<>();
        mRecyclerView =  findViewById(R.id.mview);
        mLayoutManager = new LinearLayoutManager(this);
        mMessageAdapter = new ChatAdapter(myDataset, R.layout.msg_item, uid);
        messageEditText = findViewById(R.id.messageEditText);
    }

    //---------------------------------BOILER CODE
    private void dettachDatabaseListener() {
        if (mchildEventListener !=null) {
            mMessageDatabaseReference.removeEventListener(mchildEventListener);
            mchildEventListener = null;
        }
    }
    private void onSignedOutCleanup() {
        displayname = NONE;
        if (myDataset!=null)
        myDataset.clear();
        mMessageAdapter.notifyDataSetChanged();
        dettachDatabaseListener();
    }
    private void onSignedInInitialized(String displayName, String uid) {

        this.uid = uid;
        this.displayname = displayname;
        activateChat();
        attachDatabaseReadListener();
        getUserDetails1(uid);
        getUserDetails2(stylistId);

        //Iniatilizing references to views
        initViews();

        //Enable Send button when there's text to send
        customizeMessageBox();

        //Setup recyclerview with adapter
        setUpRecyclerViewWithAdapter();
    }

    private void activateChat() {
        isReadyToSend = true;
    }

    private void attachDatabaseReadListener() {
        String uidPattern = createUidPattern(uid, stylistId);
        Query first = db.collection(uidPattern).orderBy("message_time", Query.Direction.ASCENDING);;//.limit(6);
        first.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if ( queryDocumentSnapshots.getDocuments().size()>0) {
                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.getDocuments().size() - 1);

                    // Listen to query changes and update screen
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (dc.getDocument().getData().containsKey("last_message")) {
                            continue;
                        }
                        Message message = mapper.convertValue(dc.getDocument().getData(), Message.class);
                        myDataset.add(message);
                        mMessageAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
                        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

                            @Override
                            public void onLayoutChange(View v,
                                                       int left, int top, int right, int bottom,
                                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                if (bottom < oldBottom) {
                                    mRecyclerView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRecyclerView.smoothScrollToPosition(
                                                    mRecyclerView.getAdapter().getItemCount() - 1);
                                        }
                                    }, 10);
                                }
                            }
                        });
                    }
                } else {
                            Log.w(TAG, "Error getting documents.", e);
                        }

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
        dettachDatabaseListener();
        myDataset.clear();
        mMessageAdapter.notifyDataSetChanged();
    }

    public void fetchConfig() {
        long cacheExpiration = 3600;

        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "ERROR GETTING CONFIG");
                        applyRetrievedLengthLimit();
                    }
                });

    }

    //TODO find a smarterway around this block
    private void getUserDetails1(String uid) {
        DocumentReference docRef = db.collection(var.USERS_DATA).document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                  mUser1 = mapper.convertValue(document.getData(), user.class);
                }
                else {
                    Log.d(TAG, "No such document");

                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
    private void getUserDetails2(String uid) {
        DocumentReference docRef = db.collection(var.USERS_DATA).document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    mUser2 = mapper.convertValue(document.getData(), user.class);
                }
                else {
                    Log.d(TAG, "No such document");

                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void applyRetrievedLengthLimit() {
        Long friendly_msg_lenght = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGHT_KEY);
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_lenght.intValue())});
        Log.d(TAG, FRIENDLY_MSG_LENGHT_KEY + " = " + friendly_msg_lenght);
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
}
