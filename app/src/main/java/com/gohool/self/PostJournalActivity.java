package com.gohool.self;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gohool.self.model.Journal;
import com.gohool.self.util.journalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.Objects;

import static com.gohool.self.util.journalApi.getInstance;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1;
    private static final String TAG = "Post Journal Activity";
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView currentUserTextView;
    private ImageView imageView;
    private String currentUsername;
    private String currentUserId;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    // connection to firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title);
        thoughtsEditText = findViewById(R.id.post_description);
        currentUserTextView = findViewById(R.id.postUserName_textView);

        imageView = findViewById(R.id.imageView2);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        addPhotoButton = findViewById(R.id.postCameraButton);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        addPhotoButton.setOnClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);

        // taking name nd id as checking is good for developer
        if (journalApi.getInstance() != null) {
            currentUserId = getInstance().getUserId();
            currentUsername = getInstance().getUsername();

            currentUserTextView.setText(currentUsername);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {


                } else {
                }

            }
        };
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.save_button:
                //saveJournal
                saveJournal();
                break;
            case R.id.postCameraButton:
                //get image from gallery/phone(explict intent)
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE); // asking for result
                break;
        }
    }
    private void saveJournal() {
        final String title = titleEditText.getText().toString().trim();
        final String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(thoughts) &&
                imageUri != null) {// 2 things happen text add nd image too
            final StorageReference filepath = storageReference //.../journal_images/our_image.jpeg
                    .child("journal_images") //folder for img
                    .child("my_image_" + Timestamp.now().getSeconds()); // my_image_887474737(getting diff name for diff img)

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.INVISIBLE);
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // here uri for saving to firebase
                                    String imageUrl = uri.toString();
                                    //Todo: create a Journal Object - model

                                    // this is here bcus we need to set img uri
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currentUsername);
                                    journal.setUserId(currentUserId);

                                    //Todo:invoke our collectionReference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {

                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this,
                                                            JournalListActivity.class));
                                                    finish(); //bcus we see all these journal on listactivity
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {

                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e.getMessage());

                                                }
                                            });
                                    //Todo: and save a Journal instance.

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });


        } else {
            progressBar.setVisibility(View.INVISIBLE);

        }
    }


    @Override // we get the path for image url
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {// here data means galery img
                imageUri = data.getData(); // we have the actual path
                imageView.setImageURI(imageUri); // set imagebackground(get img)
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}




