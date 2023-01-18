package comp5216.sydney.edu.au.customerapp;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import comp5216.sydney.edu.au.customerapp.fragments.MapViewFragment;

public class MainActivity extends AppCompatActivity {
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FirebaseApp.initializeApp(this);
		mAuth = FirebaseAuth.getInstance();
		//        setContentView(R.layout.activity_main);
		setContentView(R.layout.initial_screen);
	}

	@Override
	public void onStart() {

		super.onStart();
		setContentView(R.layout.initial_screen);
		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = mAuth.getCurrentUser();
		//currentUser = null;
		if (currentUser != null) {
			Intent intent = new Intent(MainActivity.this, Home.class);
			startActivity(intent);
			// go to main menu (map view)
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