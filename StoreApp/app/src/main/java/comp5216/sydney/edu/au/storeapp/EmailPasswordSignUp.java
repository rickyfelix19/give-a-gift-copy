package comp5216.sydney.edu.au.storeapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class User {
	public String name;
	public String lastname;
	public Calendar birthDate;
	public String email;
	public String password;

	public Map<String, Object> getUserForDB() {
		Map<String, Object> aux = new HashMap<>();
		if (name != null)
			aux.put("name", name);
		if (lastname != null)
			aux.put("lastname", lastname);
		if (birthDate != null)
			aux.put("birthdate", birthDate.getTime());
		if (email != null)
			aux.put("email", email);
		return aux;
	}
}

public class EmailPasswordSignUp extends AppCompatActivity
	implements DatePickerDialog.OnDateSetListener {
	private FirebaseAuth mAuth;
	private EditText emailTextField;
	private EditText passwordTextField;
	private FirebaseFirestore db;

	private EditText firstName;
	private EditText lastName;
	private MaterialButton pickerButton;

	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
		emailTextField = (EditText) findViewById(R.id.emailTextField);
		passwordTextField = (EditText) findViewById(R.id.passwordTextField);
		firstName = (EditText) findViewById(R.id.firstNameTextField);
		lastName = (EditText) findViewById(R.id.lastNameTextField);
		user = new User();
		pickerButton = findViewById(R.id.btnPickDate);
		pickerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				DatePickerDialog datePickerDialog =
					new DatePickerDialog(EmailPasswordSignUp.this, EmailPasswordSignUp.this, year,
						month, day);
				datePickerDialog.show();
			}
		});

		// get data from intent if there is any
		Intent data = getIntent();
		user.email = data.getStringExtra("email");
		if (user.email != null) {
			emailTextField.setText(user.email);
		}
		user.password = data.getStringExtra("password");
		if (user.password != null) {
			passwordTextField.setText(user.password);
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		// Only has to check 1 of them
		if (!Objects.isNull(year) || year != 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
			user.birthDate = new GregorianCalendar(year, month, dayOfMonth);
			pickerButton.setText(sdf.format(user.birthDate.getTime()));
		}
	}

	public void signUp() {
		Log.d("LOGGING", "Email: " + user.email + " pass: " + user.password);
		Toast.makeText(EmailPasswordSignUp.this, "Signing in", Toast.LENGTH_SHORT).show();
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Creating your account").show();
		mAuth.createUserWithEmailAndPassword(user.email, user.password)
			.addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
				if (task.isSuccessful()) {
					FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
					if (currentUser == null)
						return; // TODO: change this
					user.name = firstName.getText().toString();
					user.lastname = lastName.getText().toString();
					user.email = currentUser.getEmail();
					Map<String, Object> userToDB = user.getUserForDB();
					FirebaseFirestore.getInstance()
						.collection("users")
						.document(currentUser.getUid())
						.set(userToDB)
						.addOnCompleteListener((Task<Void> t) -> {
							Log.d("TEST", "DocumentSnapshot added with ID: ");
							Intent intent = new Intent(EmailPasswordSignUp.this,
								SelectStore.class);
							dialog.dismiss();
							startActivity(intent);
						})
						.addOnFailureListener((@NonNull Exception e) -> {
							Log.w("TEST", "Error adding document", e);
							dialog.dismiss();
							new MaterialAlertDialogBuilder(this,
								com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setTitle(
									"Error creating account")
								.setMessage("There was an error while creating your account")
								.setPositiveButton("DISMISS", (a, b) -> {
								})
								.show();
						});
				} else {
					Toast.makeText(EmailPasswordSignUp.this, "Authentication failed.",
						Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					new MaterialAlertDialogBuilder(this,
						com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setTitle(
							"Error creating account")
						.setMessage(
							"There was an error while creating your account. Make sure you have a " +
								"strong password")
						.setPositiveButton("DISMISS", (a, b) -> {
						})
						.show();
				}
			});
	}

	public void onSignUpClick(View view) {
		user.email = emailTextField.getText().toString();
		user.password = passwordTextField.getText().toString();
		signUp();
	}

	public void onGoToLogIn(View view) {
		Intent intent = new Intent(EmailPasswordSignUp.this, EmailPasswordSignIn.class);
		intent.putExtra("password", passwordTextField.getText().toString());
		intent.putExtra("email", emailTextField.getText().toString());
		startActivity(intent);
	}
}
