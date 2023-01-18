package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class EmailPasswordSignIn extends AppCompatActivity {
	private FirebaseAuth mAuth;
	private EditText emailTextField;
	private EditText passwordTextField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		mAuth = FirebaseAuth.getInstance();
		emailTextField = (EditText) findViewById(R.id.emailTextField);
		passwordTextField = (EditText) findViewById(R.id.passwordTextField);
		// get data from intent if there is any
		Intent data = getIntent();
		String email = data.getStringExtra("email");
		if (email != null) {
			emailTextField.setText(email);
		}
		String password = data.getStringExtra("password");
		if (password != null) {
			passwordTextField.setText(password);
		}
	}

	private void signIn(String email, String password) {
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Signing in").show();
		mAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
				if (task.isSuccessful()) {
					// Sign in success, update UI with the signed-in user's information
					Log.d("LOGGING", "signInWithEmail:success");
					FirebaseUser user = mAuth.getCurrentUser();
					Intent intent = new Intent(EmailPasswordSignIn.this, MainActivity.class);
					dialog.dismiss();
					startActivity(intent);
					// updateUI(user);
				} else {
					// If sign in fails, display a message to the user.
					Log.w("WARNING", "signInWithEmail:failure", task.getException());
					Toast.makeText(EmailPasswordSignIn.this, "Authentication failed.",
						Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					new MaterialAlertDialogBuilder(this,
						com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setTitle(
							"Error signing in")
						.setMessage(
							"There was an error signing in. Please make sure your credentials are" +
								" " +
								"correct")
						.setPositiveButton("DISMISS", (a, b) -> {
						})
						.show();
					// updateUI(null);
				}
			});
	}

	public void onSignInClick(View view) {
		// validate email and password
		if (emailTextField.getText() == null || emailTextField.getText().toString().equals("") ||
			passwordTextField.getText() == null ||
			passwordTextField.getText().toString().equals("")) {
			Log.w("WARNING", "Try to sign in with empty credentials");
			Toast.makeText(EmailPasswordSignIn.this, "Email and Password are required",
				Toast.LENGTH_SHORT).show();
			return;
		}
		signIn(emailTextField.getText().toString(), passwordTextField.getText().toString());
	}

	public void onGotoSignUp(View view) {
		Intent intent = new Intent(EmailPasswordSignIn.this, EmailPasswordSignUp.class);
		intent.putExtra("password", passwordTextField.getText().toString());
		intent.putExtra("email", emailTextField.getText().toString());
		startActivity(intent);
	}


	// Register a request to start an activity for result and register the result callback
	ActivityResultLauncher<Intent> mLauncherResetPassword =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == RESULT_OK) {
				Intent data = result.getData();

				String email = data.getStringExtra("email");
				emailTextField.setText(email);

				if (Objects.isNull(email) || email.equals("")) {
					Toast.makeText(getApplicationContext(), "Could not reset empty email",
						Toast.LENGTH_SHORT).show();
				} else {
					mAuth.setLanguageCode("en"); // Set language to ENG
					mAuth.sendPasswordResetEmail(email)
						.addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								if (task.isSuccessful()) {
									// Make a standard toast that just contains text
									Toast.makeText(getApplicationContext(), "Email sent.",
										Toast.LENGTH_SHORT).show();
								}
							}
						});
				}

			} else if (result.getResultCode() == RESULT_CANCELED) {
				// Make a standard toast that just contains text
				Toast.makeText(getApplicationContext(), "Canceled Reset Password",
					Toast.LENGTH_SHORT).show();
			}
		});

	public void onForgotPassword(View view) {
		Intent intent = new Intent(EmailPasswordSignIn.this, ForgotPassword.class);
		intent.putExtra("email", emailTextField.getText().toString());
		mLauncherResetPassword.launch(intent);
	}

	;
}


