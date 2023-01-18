package comp5216.sydney.edu.au.customerapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.customerapp.Home;
import comp5216.sydney.edu.au.customerapp.R;

public class Progressive5 extends AppCompatActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onboarding_progressive5);
	}

	public void onGetStarted(View view) {
		Intent intent = new Intent(Progressive5.this, Home.class);
		startActivity(intent);
	}
}
