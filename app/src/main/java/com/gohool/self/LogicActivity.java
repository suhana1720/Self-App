package com.gohool.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gohool.self.util.journalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LogicActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createButton;
    private AutoCompleteTextView emailAddress;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
   private ProgressBar progressBar;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logic);
        firebaseAuth =FirebaseAuth.getInstance();

progressBar=findViewById(R.id.login_progress);
        loginButton=findViewById(R.id.login);
        createButton=findViewById(R.id.accountButton);
        emailAddress=findViewById(R.id.email);
        password=findViewById(R.id.password);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0); // this for removing toolbar

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogicActivity.this,CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(pwd)){
            //as we hv to sign in
            firebaseAuth.signInWithEmailAndPassword(email,pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // till now user get login in so...
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            assert user != null;
                           final String currentUserId = user.getUid();
                            // loop through nd find currentuserid
                            collectionReference.whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() { // wanna get
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                                            if(e!=null){
                                                //we have a prob
                                            }
                                            //now we query everything
                                            assert value != null;
                                            if(!value.isEmpty()){
                                                progressBar.setVisibility(View.INVISIBLE);
                                                // now we want to get details i.e.username nd id
                                                //as we have morethan 1 username infuture so we put in loop
                                                for(QueryDocumentSnapshot snapshots :value){
                                                journalApi JournalApi=journalApi.getInstance();
                                                JournalApi.setUsername(snapshots.getString("username"));
                                                JournalApi.setUserId(snapshots.getString("userId"));

                                                //go to listactivity
                                                    startActivity(new Intent(LogicActivity.this,PostJournalActivity.class));

                                                }



                                            }

                                        }
                                    });




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          //  MediaRouteButton progressBar;
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });



        }else {

           progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LogicActivity.this,
                    "Please enter email and password",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}



