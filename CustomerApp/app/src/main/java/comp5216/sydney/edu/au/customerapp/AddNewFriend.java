package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class AddNewFriend extends FragmentActivity {

	private TextInputEditText mFriendEmailEdit;
	private FirebaseFirestore mDb;
	private UserDB mUser;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_new_friend);
		mFriendEmailEdit = findViewById(R.id.friendsEmailEditText);
		mDb = FirebaseFirestore.getInstance();
		Intent intent = getIntent();
		mUser = intent.getParcelableExtra("user");
	}

	public void onClickAddNewFriend(View view) {
		String email = mFriendEmailEdit.getText().toString();
		if (email.equals("")) {
			Toast.makeText(this, "You need to enter an email", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.d("HERE", "e: " + email + " user: " + mUser.getEmail());
		if (email.equals(mUser.getEmail())) {
			Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Sending friend request").show();
		mDb.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(t -> {
			if (!t.isSuccessful()) {
				dialog.dismiss();
				new MaterialAlertDialogBuilder(this).setTitle("User not found")
					.setPositiveButton(R.string.dismiss_text, (dia, which) -> {
					})
					.show();
				return;
			}
			List<DocumentSnapshot> docs = t.getResult().getDocuments();
			if (docs.size() == 0) {
				dialog.dismiss();
				new MaterialAlertDialogBuilder(this).setTitle("User not found")
					.setPositiveButton(R.string.dismiss_text, (dia, which) -> {
					})
					.show();
				return;
			}
			DocumentSnapshot friendDoc = docs.get(0);
			DocumentReference userRef = mDb.collection("users").document(mUser.getId());
			mDb.collection("friendList")
				.whereEqualTo("user", userRef)
				.whereEqualTo("friend", friendDoc.getReference())
				.get()
				.addOnCompleteListener((friendInFriendList) -> {
					if (!friendInFriendList.getResult().isEmpty()) {
						dialog.dismiss();
						new MaterialAlertDialogBuilder(this).setTitle(
								"You already added this friend")
							.setPositiveButton(R.string.dismiss_text, (dia, which) -> {
							})
							.show();
						return;
					}
					if (!friendInFriendList.isSuccessful()) {
						dialog.dismiss();
						new MaterialAlertDialogBuilder(this).setTitle("Error sending request")
							.setPositiveButton(R.string.dismiss_text, (dia, which) -> {
							})
							.show();
						return;
					}
					Map<String, Object> relationShip = new HashMap<>();
					relationShip.put("user", userRef);
					relationShip.put("friend", friendDoc.getReference());
					relationShip.put("accepted", false);
					mDb.collection("friendList")
						.add(relationShip)
						.addOnCompleteListener((ignored) -> {
							dialog.dismiss();
							Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();
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
