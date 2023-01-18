package comp5216.sydney.edu.au.customerapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import comp5216.sydney.edu.au.customerapp.EditProfile;
import comp5216.sydney.edu.au.customerapp.GiftListActivity;
import comp5216.sydney.edu.au.customerapp.FriendRequestsActivity;
import comp5216.sydney.edu.au.customerapp.MainActivity;
import comp5216.sydney.edu.au.customerapp.R;
import comp5216.sydney.edu.au.customerapp.SubmitProfilePicture;
import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class ProfileFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	private UserDB mUserDB;
	private String mParam2;
	private TextView mUsernameTextView;
	private TextView mFriendCountText;
	private TextView mGiftSentCountText;
	private TextView mGiftReceivedCountText;
	private TextView mGiftPendingText;
	private FirebaseFirestore mDb;
	private FirebaseAuth mAuth;

	public ProfileFragment() {
		// Required empty public constructor
	}

	public static ProfileFragment newInstance(UserDB userDB, String param2) {
		ProfileFragment fragment = new ProfileFragment();
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
		return inflater.inflate(R.layout.profile_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		mDb = FirebaseFirestore.getInstance();
		mAuth = FirebaseAuth.getInstance();
		super.onViewCreated(view, savedInstanceState);
		mUsernameTextView = view.findViewById(R.id.profileUserNameText);
		mFriendCountText = view.findViewById(R.id.profileUserFriendsNumberText);
		mGiftSentCountText = view.findViewById(R.id.profileUserGiftsSentNumberText);
		mGiftReceivedCountText = view.findViewById(R.id.profileUserGiftsReceiveNumberText);
		mGiftPendingText = view.findViewById(R.id.profileUserCollectGiftText);
		mGiftPendingText.setOnClickListener(this::onClickGiftsToCollect);

		view.findViewById(R.id.profileLogOutButton).setOnClickListener(this::onClickLogOut);
		view.findViewById(R.id.profilePictureInProfile)
			.setOnClickListener(this::onClickEditProfilePicture);
		view.findViewById(R.id.profileEditProfileButton)
			.setOnClickListener(this::onClickEditProfile);
		view.findViewById(R.id.profileDeleteProfileButton)
			.setOnClickListener(this::onClickDeleteAccount);
		view.findViewById(R.id.profileFriendRequestsLayout)
			.setOnClickListener(this::onClickFriendRequest);

		mUsernameTextView.setText(getString(R.string.profile_name_string, mUserDB.getName()));
		DocumentReference userReference = mDb.collection("users").document(mUserDB.getId());
		getNumberOfFriendRequests(userReference);
		getNumberOfGiftSent(userReference);
		getNumberOfGiftReceived(userReference);
		getNumberOfPendingGifts(userReference);
		if (mUserDB.getProfileImage() != null) {
			StorageReference storageReference =
				FirebaseStorage.getInstance().getReference(mUserDB.getProfileImage());
			// Load the image using Glide
			Glide.with(requireContext())
				.load(storageReference)
				.into((ImageView) view.findViewById(R.id.profilePictureInProfile));
		}
	}

	private void onClickGiftsToCollect(View view) {
		Intent intent = new Intent(getContext(), GiftListActivity.class);
		startActivity(intent);
	}

	public void onClickEditProfile(View view) {
		Intent intent = new Intent(getContext(), EditProfile.class);
		intent.putExtra("user", mUserDB);
		mLaunchEditProfile.launch(intent);
	}

	public void onClickEditProfilePicture(View view) {
		Intent intent = new Intent(getContext(), SubmitProfilePicture.class);
		intent.putExtra("userId", mUserDB.getId());
		mLauncher.launch(intent);
	}

	public void onClickDeleteAccount(View view) {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
		builder.setTitle("ACCOUNT DELETION");
		builder.setMessage("Are you sure you want to delete your account?");
		builder.setPositiveButton("Yes", (dialogInterface, i) -> {
			mDb.collection("users").document(mUserDB.getId()).delete();
			Objects.requireNonNull(mAuth.getCurrentUser()).delete().addOnCompleteListener(t -> {
				mAuth.signOut();
				Intent intent = new Intent(requireActivity(), MainActivity.class);
				startActivity(intent);
			});
		});
		builder.setNegativeButton("No", (dialogInterface, i) -> {
			dialogInterface.dismiss();
		});
		builder.show();
	}

	ActivityResultLauncher<Intent> mLauncher =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				Intent data = result.getData();
				if (data != null) {
					if (mUserDB.getProfileImage() != null) {
						FirebaseStorage.getInstance()
							.getReference()
							.child(mUserDB.getProfileImage())
							.delete();
					}
					String newPath = data.getStringExtra("newPath");
					if (newPath != null) {
						mUserDB.setProfileImage(newPath);
						StorageReference storageReference =
							FirebaseStorage.getInstance().getReference(mUserDB.getProfileImage());
						Glide.with(requireContext())
							.load(storageReference)
							.into((ImageView) requireView().findViewById(
								R.id.profilePictureInProfile));
					}
				}
			}
		});

	ActivityResultLauncher<Intent> mLaunchEditProfile =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				Intent data = result.getData();
				if (data != null) {
					UserDB aux = data.getParcelableExtra("user");
					if (aux == null)
						return;
					mUserDB.setName(aux.getName());
					mUserDB.setLastname(aux.getLastname());
					mUserDB.setBirthday(aux.getBirthday());
					mUsernameTextView.setText(
						getString(R.string.profile_name_string, mUserDB.getName()));
				}
			}
		});

	ActivityResultLauncher<Intent> mLaunchFriendRequests =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				Intent data = result.getData();
				if (data != null) {
					getNumberOfFriendRequests(mDb.collection("users").document(mUserDB.getId()));
				}
			}
		});

	public void onClickFriendRequest(View view) {
		Intent intent = new Intent(requireContext(), FriendRequestsActivity.class);
		intent.putExtra("user", mUserDB);
		mLaunchFriendRequests.launch(intent);
	}

	public void onClickLogOut(View view) {
		mAuth.signOut();
		Intent intent = new Intent(requireActivity(), MainActivity.class);
		startActivity(intent);
	}

	public void getNumberOfPendingGifts(DocumentReference userReference) {
		Query query = mDb.collection("gifts")
			.whereEqualTo("receiver", userReference)
			.whereEqualTo("is_used", false);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mGiftPendingText.setText(
							getString(R.string.profile_gifts_to_collect, snapshot.getCount()));
					} catch (Throwable e) {
						mGiftPendingText.setText(getString(R.string.profile_gifts_to_collect, 0));
						e.printStackTrace();
					}
				} else {
					mGiftPendingText.setText(getString(R.string.profile_gifts_to_collect, 0));
				}
			});
	}

	public void getNumberOfFriendRequests(DocumentReference userReference) {
		Query query = mDb.collection("friendList")
			.whereEqualTo("friend", userReference)
			.whereEqualTo("accepted", false);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mFriendCountText.setText(String.valueOf(snapshot.getCount()));
					} catch (Throwable e) {
						mFriendCountText.setText("0");
						e.printStackTrace();
					}
				} else {
					mFriendCountText.setText("0");
				}
			});
	}

	public void getNumberOfGiftReceived(DocumentReference userReference) {
		Query query = mDb.collection("gifts").whereEqualTo("receiver", userReference);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mGiftReceivedCountText.setText(String.valueOf(snapshot.getCount()));
					} catch (Throwable e) {
						mGiftReceivedCountText.setText("0");
						e.printStackTrace();
					}
				} else {
					mGiftReceivedCountText.setText("0");
				}
			});
	}

	public void getNumberOfGiftSent(DocumentReference userReference) {
		Query query = mDb.collection("gifts").whereEqualTo("sender", userReference);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mGiftSentCountText.setText(String.valueOf(snapshot.getCount()));
					} catch (Throwable e) {
						mGiftSentCountText.setText("0");
						e.printStackTrace();
					}
				} else {
					mGiftSentCountText.setText("0");
				}
			});
	}
}