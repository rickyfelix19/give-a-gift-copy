package comp5216.sydney.edu.au.storeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private static final int LIMIT = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.initial_screen);
    }

    @Override
    public void onStart() {
        super.onStart();
        setContentView(R.layout.initial_screen);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, SelectStore.class);
            startActivity(intent);
        }
    }

    public void onClickRegister(View v) {
        Intent intent = new Intent(MainActivity.this, EmailPasswordSignUp.class);
        startActivity(intent);
    }

    public void onClickLogIn(View v) {
        Intent intent = new Intent(MainActivity.this, EmailPasswordSignIn.class);
        startActivity(intent);
    }
}