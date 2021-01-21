package com.gohool.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gohool.self.model.Journal;
import com.gohool.self.ui.RecyclerViewAdapter;
import com.gohool.self.util.journalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private CollectionReference collectionReference=db.collection("Journal");
    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        noJournalEntry =findViewById(R.id.no_journal);
        journalList=new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //onclick listener


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                //Take users to add journal
                if(user!=null && firebaseAuth != null){
                    startActivity(new Intent(JournalListActivity.this,PostJournalActivity.class));
                  // finish();
                }
                break;

            case R.id.action_signout:
                //sign user out
                if(user!=null && firebaseAuth!=null){
                    firebaseAuth.signOut();
                    //as we have to go to begin
                    startActivity(new Intent(JournalListActivity.this,MainActivity.class));
                    //finish();
                }
                break;




        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {// here we get journal from firestore
        collectionReference.whereEqualTo("userId", journalApi.getInstance().getUserId()) // we use whereequalto so as to get doc of current user
                   .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // here we get all the doc of particular user we need to add to journal list
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal); // each time we go through loop we add it on list
                            }

                            //invoke recyclerview
                            recyclerViewAdapter = new RecyclerViewAdapter(JournalListActivity.this,
                                    journalList);
                            recyclerView.setAdapter(recyclerViewAdapter);
                            recyclerViewAdapter.notifyDataSetChanged();

                        } else {
                         noJournalEntry.setVisibility(View.VISIBLE);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        super.onStart();
    }
}
