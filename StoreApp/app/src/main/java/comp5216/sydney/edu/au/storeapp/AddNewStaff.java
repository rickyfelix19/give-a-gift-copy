package comp5216.sydney.edu.au.storeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Staff;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;

public class AddNewStaff extends FragmentActivity {

	private TextInputEditText mStaffEmailEdit;
	private FirebaseFirestore mDb;
	private UserDB mUser;

	private final String[] roles = {"Admin", "Staff"};
	AutoCompleteTextView autoCompleteTextView;
	ArrayAdapter<String> adapterRoles;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_new_staff);
		mStaffEmailEdit = findViewById(R.id.staffEmail);
		mDb = FirebaseFirestore.getInstance();
		Intent intent = getIntent();
		mUser = intent.getParcelableExtra("user");

		autoCompleteTextView = findViewById(R.id.staffRole);
		adapterRoles = new ArrayAdapter<String>(this, R.layout.role_item, roles);
		autoCompleteTextView.setAdapter(adapterRoles);
		autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
			String role = parent.getItemAtPosition(position).toString();
		});
	}

	public void onClickAddNewStaff(View view) {
		String email = mStaffEmailEdit.getText().toString();
		String role = autoCompleteTextView.getText().toString().toLowerCase();
		if (email.equals("") || role.equals("")) {
			Toast.makeText(this, "Email and role are required", Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Adding new staff").show();
		mDb.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(t -> {
			if (!t.isSuccessful()) {
				dialog.dismiss();
				new MaterialAlertDialogBuilder(this).setTitle("User not found")
					.setPositiveButton("Dismiss", (dia, which) -> {
					})
					.show();
				return;
			}
			List<DocumentSnapshot> docs = t.getResult().getDocuments();
			if (docs.size() == 0) {
				dialog.dismiss();
				new MaterialAlertDialogBuilder(this).setTitle("User not found")
					.setPositiveButton("Dismiss", (dia, which) -> {
					})
					.show();
				return;
			}
			DocumentSnapshot staffDoc = docs.get(0);
			DocumentReference storeRef =
				mDb.collection("stores").document(mUser.getCurrentStoreId());
			mDb.collection("userStores")
				.whereEqualTo("user", staffDoc.getReference())
				.whereEqualTo("store", storeRef)
				.get()
				.addOnCompleteListener((staffInStoreList) -> {
					if (!staffInStoreList.getResult().isEmpty()) {
						dialog.dismiss();
						new MaterialAlertDialogBuilder(this).setTitle(
								"This user is already staff in your store")
							.setPositiveButton("Dismiss", (dia, which) -> {
							})
							.show();
						return;
					}
					if (!staffInStoreList.isSuccessful()) {
						dialog.dismiss();
						new MaterialAlertDialogBuilder(this).setTitle("Error adding staff member")
							.setPositiveButton("Dismiss", (dia, which) -> {
							})
							.show();
						return;
					}
					Map<String, Object> staffInStore = new HashMap<>();
					staffInStore.put("user", staffDoc.getReference());
					staffInStore.put("store", storeRef);
					staffInStore.put("type", role);
					mDb.collection("userStores")
						.add(staffInStore)
						.addOnCompleteListener((ignored) -> {
							dialog.dismiss();
							Toast.makeText(this, "Staff added", Toast.LENGTH_SHORT).show();
							Intent returnIntent = new Intent();
							setResult(RESULT_OK, returnIntent);
							finish();
						});
				});
			dialog.dismiss();
		});
	}

	public void onClickCancel(View view) {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		finish();
	}

}
