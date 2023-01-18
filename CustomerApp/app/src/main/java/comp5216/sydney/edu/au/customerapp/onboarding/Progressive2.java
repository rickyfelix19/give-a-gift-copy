package comp5216.sydney.edu.au.customerapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.customerapp.MainActivity;
import comp5216.sydney.edu.au.customerapp.R;

public class Progressive2 extends AppCompatActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onboarding_progressive2);
	}

	public void onSkip(View view) {
		Intent intent =
			new Intent(Progressive2.this, MainActivity.class);
		startActivity(intent);
	}

	public void onNext(View view) {
		Intent intent = new Intent(Progressive2.this, Progressive3.class);
		startActivity(intent);
	}
}
