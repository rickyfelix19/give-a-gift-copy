package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import comp5216.sydney.edu.au.customerapp.dataHelpers.Friend;
import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class FriendRequestsActivity extends AppCompatActivity {
	private UserDB mUserDB;
	private FirebaseFirestore db;
	private FriendRequestsAdapter requestsAdapter;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_requests);
		Intent data = getIntent();
		mUserDB = data.getParcelableExtra("user");
		db = FirebaseFirestore.getInstance();
		requestsAdapter = new FriendRequestsAdapter(this, this::onClickAccept, this::onClickDeny);
		ListView listView = findViewById(R.id.friendRequestsListView);
		listView.setAdapter(requestsAdapter);
		getFriendRequests();
	}

	public void onClickDeny(Friend friend) {
		new MaterialAlertDialogBuilder(this).setTitle("Deny friend request")
			.setMessage("Are you sure you want to deny this friend request?")
			.setPositiveButton("Yes", (dia, which) -> {
				dia.dismiss();
				AlertDialog dialog = new MaterialAlertDialogBuilder(this,
					com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
					R.layout.spinner).setMessage("Deleting friend request").show();
				DocumentReference userRef = db.collection("users").document(mUserDB.getId());
				DocumentReference friendRef = db.collection("users").document(friend.getId());
				AtomicBoolean remove = new AtomicBoolean(false);
				db.collection("friendList")
					.whereEqualTo("user", userRef)
					.whereEqualTo("friend", friendRef)
					.get()
					.addOnCompleteListener(task -> {
						if (task.isSuccessful()) {
							List<DocumentSnapshot> cons = task.getResult().getDocuments();
							if (cons.size() > 0) {
								for (DocumentSnapshot con : cons) {
									con.getReference().delete();
								}
							}
						}
						if (remove.get()) {
							dialog.dismiss();
							getFriendRequests();
						} else {
							remove.set(true);
						}
					});
				db.collection("friendList")
					.whereEqualTo("user", friendRef)
					.whereEqualTo("friend", userRef)
					.get()
					.addOnCompleteListener(task -> {
						if (task.isSuccessful()) {
							List<DocumentSnapshot> cons = task.getResult().getDocuments();
							if (cons.size() > 0) {
								for (DocumentSnapshot con : cons) {
									con.getReference().delete();
								}
							}
						}
						if (remove.get()) {
							dialog.dismiss();
							getFriendRequests();
						} else {
							remove.set(true);
						}
					});
			})
			.setNegativeButton("No", (dia, which) -> {
				dia.dismiss();
			})
			.show();
	}

	public void onClickAccept(Friend friend) {
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Accepting friend request").show();
		DocumentReference userRef = db.collection("users").document(mUserDB.getId());
		DocumentReference friendRef = db.collection("users").document(friend.getId());
		db.collection("friendList")
			.whereEqualTo("user", friendRef)
			.whereEqualTo("friend", userRef)
			.get()
			.addOnCompleteListener(task -> {
				Map<String, Boolean> info = new HashMap<>();
				info.put("accepted", true);
				if (task.isSuccessful()) {
					List<DocumentSnapshot> cons = task.getResult().getDocuments();
					if (cons.size() > 0) {
						cons.get(0)
							.getReference()
							.set(info, SetOptions.merge())
							.addOnCompleteListener(t -> {
								dialog.dismiss();
								getFriendRequests();
							});
					}
				}
			});
		Map<String, Object> newFriend = new HashMap<>();
		newFriend.put("user", userRef);
		newFriend.put("friend", friendRef);
		newFriend.put("accepted", true);
		db.collection("friendList").add(newFriend);
	}

	public void getFriendRequests() {
		requestsAdapter = new FriendRequestsAdapter(this, this::onClickAccept, this::onClickDeny);
		ListView listView = findViewById(R.id.friendRequestsListView);
		listView.setAdapter(requestsAdapter);
		DocumentReference userReference = db.collection("users").document(mUserDB.getId());
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Loading friend requests").show();
		db.collection("friendList")
			.whereEqualTo("friend", userReference)
			.whereEqualTo("accepted", false)
			.get()
			.addOnCompleteListener(q -> {
				List<DocumentSnapshot> docs = q.getResult().getDocuments();
				AtomicInteger count = new AtomicInteger(docs.size());
				for (DocumentSnapshot doc : docs) {
					DocumentReference friendRef = doc.get("user", DocumentReference.class);
					if (friendRef == null)
						continue;
					friendRef.get().addOnCompleteListener(ds -> {
						DocumentSnapshot result = ds.getResult();
						Friend friend = new Friend(result, true);
						requestsAdapter.add(friend);
						if (count.decrementAndGet() <= 0)
							requestsAdapter.notifyDataSetChanged();
					});
				}
				dialog.dismiss();
			});
	}
}