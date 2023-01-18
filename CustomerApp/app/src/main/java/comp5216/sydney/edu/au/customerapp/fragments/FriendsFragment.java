package comp5216.sydney.edu.au.customerapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import comp5216.sydney.edu.au.customerapp.AddNewFriend;
import comp5216.sydney.edu.au.customerapp.MainActivity;
import comp5216.sydney.edu.au.customerapp.R;
import comp5216.sydney.edu.au.customerapp.SelectStore;
import comp5216.sydney.edu.au.customerapp.dataHelpers.Friend;
import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class FriendsFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	// TODO: Rename and change types of parameters
	private UserDB mUserDB;
	private String mParam2;
	FirebaseFirestore db;
	private FriendsAdapter friendsAdapter;

	public FriendsFragment() {
		// Required empty public constructor
	}

	public static FriendsFragment newInstance(UserDB userDB, String param2) {
		FriendsFragment fragment = new FriendsFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, userDB);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mUserDB = getArguments().getParcelable(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.friends_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.addNewFriendButton).setOnClickListener(this::onClickAddFriend);
		db = FirebaseFirestore.getInstance();
		friendsAdapter = new FriendsAdapter(requireActivity());
		ListView listView = view.findViewById(R.id.friendListView);
		listView.setAdapter(friendsAdapter);
		getFriends();
		listView.setOnItemClickListener((parent, v, position, id) -> {
			Friend friend = (Friend) friendsAdapter.getItem(position);
			//Add the receiverId and senderId
			Intent intent = new Intent(requireContext(), SelectStore.class);
			intent.putExtra("receiverId", friend.getId());
			intent.putExtra("senderId", mUserDB.getId());

			intent.putExtra("senderName", friend.toString());
			intent.putExtra("receiverName", mUserDB.toString());

			if (friend.getPending()) {
				Toast.makeText(getContext(),
					friend.getName() + " needs to accept your friend request first!",
					Toast.LENGTH_LONG).show();
				return;
			}
			if (friend.getLatitude() == null) {
				Toast toast = Toast.makeText(getContext(),
					"Sorry! No location information for " + friend.getName(), Toast.LENGTH_LONG);
				toast.show();
				return;
			}
			intent.putExtra("receiverLatitude", friend.getLatitude());
			intent.putExtra("receiverLongitude", friend.getLongitude());
			startActivity(intent);

		});
		listView.setOnItemLongClickListener((parent, view1, position, rowId) -> {
			Friend friend = (Friend) friendsAdapter.getItem(position);
			onClickDeleteFriend(position, friend);
			return true;
		});
		EditText searchEditText = view.findViewById(R.id.textSearchFriends);
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				filter(s.toString());
			}
		});
	}

	public void onClickDeleteFriend(int position, Friend friend) {
		Log.d("DELETING", "Deleting: " + friend.toString());
		new MaterialAlertDialogBuilder(requireContext()).setTitle("Remove friend")
			.setMessage("Are you sure you want to remove this friend from your friend list")
			.setPositiveButton("Yes", (dia, which) -> {
				dia.dismiss();
				AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(),
					com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
					R.layout.spinner).setMessage("Removing friend").show();
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
							getFriends();
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
							getFriends();
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

	public void onClickAddFriend(View view) {
		Intent intent = new Intent(getContext(), AddNewFriend.class);
		intent.putExtra("user", mUserDB);
		mLaunchAddFriend.launch(intent);
	}

	ActivityResultLauncher<Intent> mLaunchAddFriend =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				getFriends();
			}
		});

	private void filter(String text) {
		friendsAdapter.getFilter().filter(text.toLowerCase());
	}

	public void getFriends() {
		friendsAdapter = new FriendsAdapter(requireActivity());
		ListView listView = requireView().findViewById(R.id.friendListView);
		listView.setAdapter(friendsAdapter);
		DocumentReference userReference = db.collection("users").document(mUserDB.getId());
		AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(),
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Loading friends").show();
		db.collection("friendList")
			.whereEqualTo("user", userReference)
			.get()
			.addOnCompleteListener(q -> {
				List<DocumentSnapshot> docs = q.getResult().getDocuments();
				AtomicInteger count = new AtomicInteger(docs.size());
				for (DocumentSnapshot doc : docs) {
					DocumentReference friendRef = doc.get("friend", DocumentReference.class);
					if (friendRef == null)
						continue;
					friendRef.get().addOnCompleteListener(ds -> {
						DocumentSnapshot result = ds.getResult();
						Friend friend =
							new Friend(result, Boolean.FALSE.equals(doc.getBoolean("accepted")));
						friendsAdapter.add(friend);
						if (count.decrementAndGet() <= 0)
							friendsAdapter.notifyDataSetChanged();
					});
				}
				dialog.dismiss();
			});
	}
}