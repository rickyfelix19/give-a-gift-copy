package comp5216.sydney.edu.au.customerapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import comp5216.sydney.edu.au.customerapp.MainActivity;
import comp5216.sydney.edu.au.customerapp.R;

public class Progressive1 extends AppCompatActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onboarding_progressive1);
		TextView titleText = findViewById(R.id.titleText);
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			FirebaseFirestore.getInstance()
				.collection("users")
				.document(user.getUid())
				.get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						DocumentSnapshot document = task.getResult();
						if (document.exists()) {
							Log.d("LOGGING", "DocumentSnapshot data: " + document.getData());
							if (titleText != null) {
								titleText.setText(new StringBuilder().append("Welcome, ")
									.append(document.getString("name"))
									.toString());
							}
						} else {
							Log.d("LOGGING", "No such document");
						}
					} else {
						Log.d("LOGGING", "get failed with ", task.getException());
					}
				});
		}
	}

	public void onSkip(View view) {
		Intent intent = new Intent(Progressive1.this, MainActivity.class);
		startActivity(intent);
	}

	public void onNext(View view) {
		Intent intent = new Intent(Progressive1.this, Progressive2.class);
		startActivity(intent);
	}
}
