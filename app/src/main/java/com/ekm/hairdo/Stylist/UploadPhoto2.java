package com.ekm.hairdo.Stylist;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import com.ekm.hairdo.R;
import com.ekm.hairdo.things.Stack;
import com.ekm.hairdo.things.user;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fonfon.geohash.GeoHash;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.Map;

import static com.ekm.hairdo.var.NONE;
import static com.ekm.hairdo.var.USERS_DATA;
import static com.ekm.hairdo.var.stylistAddress;
import static com.ekm.hairdo.var.otherID;
import static com.ekm.hairdo.var.stylistLat;
import static com.ekm.hairdo.var.stylistLng;

public class UploadPhoto2 extends AppCompatActivity {

    private static final String TAG = "Uploader";
    ImageView viewImage;
    Button b;

    Stack myStack;
    public ObjectMapper mapper;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    String uid=NONE, address = NONE;
    String longitude = NONE; String latitude = NONE;
    private boolean isUidPresent;
    String displayname;

    LottieAnimationView mview;
    private boolean isFileChose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        mview = findViewById(R.id.animation_view);

        Intent intent = getIntent();
        uid = intent.getStringExtra(otherID);
        if (intent.getStringExtra(stylistAddress)!=null){
            address = intent.getStringExtra(stylistAddress);
            latitude = intent.getStringExtra(stylistLat);
            longitude = intent.getStringExtra(stylistLng);
        }

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



        b= findViewById(R.id.UploadBtn);
        viewImage= findViewById(R.id.IdProf);

        viewImage.setOnClickListener(view -> selectImage());
        b.setOnClickListener(v -> selectImage());

        selectImage();

        System.out.println(address+"----------------");

//vlImage.setProgress(68);
    }

    private void setUpStack() {
        //Create a stack
        myStack = new Stack();
        //set address
        myStack.setAddress(address);
        //set gps location
        myStack.setLat(latitude);
        myStack.setLng(longitude);
        //set name
        myStack.setName(displayname);
        //todo set price
        myStack.setPrice("$51");
        //todo set category
        myStack.setStyleName("Beautiful Hair");
        //set stylist id
        myStack.setStylistId(uid);
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
            viewImage.setImageURI(fileUri);
            Glide.with(this).load(fileUri).into(viewImage);
            mview.setVisibility(View.VISIBLE);
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
                            String url = (String) resultData.get("secure_url");
                            String id = (String) resultData.get("public_id");
                            String finalid = id.substring(id.lastIndexOf("hair/")+5);
                            myStack.setHairid(finalid);
                            myStack.setUrl(url);
                            addStyleToFirestore(myStack);
                            System.out.println(finalid);
                            System.out.println(resultData);
                            //todo upload
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            System.out.println(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    //.option("public_id", "style_"+uid)
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

    //used for goehashing
    private String geoHashForMe(Stack mstack) {
         Location location = new Location("geohash");
         location.setLatitude(Double.parseDouble(mstack.getLat()));
         location.setLongitude(Double.parseDouble(mstack.getLng()));

         GeoHash hash = GeoHash.fromLocation(location, 9);
         return  hash.toString(); //"v12n8trdj"
         }

         private void addStyleToFirestore(Stack mstack) {
             // Convert POJO to Map
             mstack.setDistance(geoHashForMe(mstack));
             Map<String, Object> map =
                     mapper.convertValue(mstack, new TypeReference<Map<String, Object>>() {
                     });
             db.collection("HAIR_STYLES3").document(mstack.getStylistId() + "_"+mstack.getHairid())
                     .set(map)
                     .addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             //todo done uploading and creating data
                             Log.d(TAG, "DocumentSnapshot successfully written!");
                             mview.setVisibility(View.GONE);
                             finish();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Log.w(TAG, "Error writing document", e);
                         }
                     });
         }

    private void addAddress(String address, String displayName, String latitude, String longitude) {
        user mUser  = new user();
        mUser.setAddress(address);
        mUser.setName(displayName);
        mUser.setLatitude(latitude);
        mUser.setLongitude(longitude);
        Map<String, Object> map =
                mapper.convertValue(mUser, new TypeReference<Map<String, Object>>() {
                });
        db.collection(USERS_DATA).document(uid)
                .set(map, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //todo done uploading and creating data
//                        Log.d(TAG, "DocumentSnapshot successfully written!");
//                        mview.setVisibility(View.GONE);
//                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }


    private void onSignedInInitialized(String displayName, String uid) {
        this.uid = uid;
        isUidPresent = true;
        invalidateOptionsMenu();
        System.out.println(displayName);
        displayname = displayName;
        if (!address.equals(NONE)) {
            addAddress(address, displayName,latitude,longitude);
        }
        setUpStack();
    }
    private void onSignedOutCleanup() {
        uid = "";
        isUidPresent = false;
        invalidateOptionsMenu();
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



}


