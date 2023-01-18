package comp5216.sydney.edu.au.customerapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;
import comp5216.sydney.edu.au.customerapp.onboarding.Progressive1;

public class EditProfile extends AppCompatActivity {

	private EditText mFirstNameText;
	private EditText mLastNameText;
	private MaterialButton mbirthDateButton;
	private UserDB mUser;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile_screen);
		mFirstNameText = findViewById(R.id.firstNameTextFieldEdit);
		mLastNameText = findViewById(R.id.lastNameTextFieldEdit);
		mbirthDateButton = findViewById(R.id.buttonEditBirthdate);
		Intent data = getIntent();
		mUser = data.getParcelableExtra("user");
		mFirstNameText.setText(mUser.getName());
		mLastNameText.setText(mUser.getLastname());
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
		mbirthDateButton.setText(dateFormatter.format(mUser.getBirthday()));
	}

	public void onClickCancel(View view) {
		Intent returnIntent = new Intent();
		//		returnIntent.putExtra("newPath", taskSnapshot.getMetadata().getPath());
		setResult(RESULT_CANCELED, returnIntent); // Set result code and bundle data for response
		finish(); // Close the activity, pass data to parent }
	}

	public static class DatePickerFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener {

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle arguments = getArguments();
			assert arguments != null;
			int year = arguments.getInt("year");
			int month = arguments.getInt("month");
			int day = arguments.getInt("day");
			return new DatePickerDialog(requireContext(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			EditProfile editProfileActivity = (EditProfile) getActivity();
			assert editProfileActivity != null;
			editProfileActivity.setBirthdate(year, month, day);
		}
	}

	public void setBirthdate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		mUser.setBirthday(c.getTime());
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
		mbirthDateButton.setText(dateFormatter.format(mUser.getBirthday()));
	}

	public void onChangeBirthdateClick(View view) {
		DialogFragment datePicker = new DatePickerFragment();
		Calendar c = Calendar.getInstance();
		c.setTime(mUser.getBirthday());
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		Bundle arguments = new Bundle();
		arguments.putInt("year", year);
		arguments.putInt("month", month);
		arguments.putInt("day", day);
		datePicker.setArguments(arguments);
		datePicker.show(getSupportFragmentManager(), "datePicker");
	}

	public void onSave(View view) {
		mUser.setName(mFirstNameText.getText().toString());
		mUser.setLastname(mLastNameText.getText().toString());
		if (mUser.getName() == null || mUser.getName().equals("") || mUser.getLastname() == null ||
			mUser.getLastname().equals("") || mUser.getBirthday() == null) {
			Toast.makeText(this, "Name, Last name and birthday are required", Toast.LENGTH_SHORT)
				.show();
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Updating your profile").show();
		Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(mUser.getId())
			.set(mUser.getUserForDB(), SetOptions.merge())
			.addOnCompleteListener((Task<Void> t) -> {
				Toast.makeText(this, "Details updated", Toast.LENGTH_SHORT).show();
				Intent data = new Intent();
				data.putExtra("user", mUser);
				setResult(RESULT_OK, data);
				dialog.dismiss();
				finish();
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w("TEST", "Error adding document", e);
				dialog.dismiss();
			});
	}

}
