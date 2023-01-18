package comp5216.sydney.edu.au.storeapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import comp5216.sydney.edu.au.storeapp.EditAddress;
import comp5216.sydney.edu.au.storeapp.EditStore;
import comp5216.sydney.edu.au.storeapp.GiftListActivity;
import comp5216.sydney.edu.au.storeapp.MainActivity;
import comp5216.sydney.edu.au.storeapp.R;
import comp5216.sydney.edu.au.storeapp.SelectStore;
import comp5216.sydney.edu.au.storeapp.dataHelpers.Store;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;

public class StoreFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	private UserDB mUserDB;
	private Store mStore;
	private String mParam2;
	private TextView mStoreNameText;
	private TextView mPedingGiftsCount;
	private TextView mCompletedGiftsCount;
	private FirebaseFirestore mDb;
	private FirebaseAuth mAuth;

	public StoreFragment() {
		// Required empty public constructor
	}

	public static StoreFragment newInstance(UserDB userDB, String param2) {
		StoreFragment fragment = new StoreFragment();
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
		return inflater.inflate(R.layout.store_profile_fragment, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDb = FirebaseFirestore.getInstance();
		mAuth = FirebaseAuth.getInstance();
		mStoreNameText = view.findViewById(R.id.profileUserNameText);
		mPedingGiftsCount = view.findViewById(R.id.storePendingGifts);
		mCompletedGiftsCount = view.findViewById(R.id.storeCompletedGifts);

		AlertDialog dialog = new MaterialAlertDialogBuilder(view.getContext(),
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Loading the store's information").show();
		getStoreFromDb(view, dialog);

		DocumentReference storeReference =
			mDb.collection("stores").document(mUserDB.getCurrentStoreId());
		getNumberOfCompletedGifts(storeReference);
		getNumberOfPendingGifts(storeReference);

	}

	public void getStoreFromDb(View view, AlertDialog dialog) {
		mDb.collection("stores")
			.document(mUserDB.getCurrentStoreId())
			.get()
			.addOnCompleteListener(t -> {
				if (!t.isSuccessful()) {
					Toast.makeText(view.getContext(), "Error getting the store's information",
						Toast.LENGTH_LONG).show();
					dialog.dismiss();
					return;
				}
				DocumentSnapshot snapshot = t.getResult();
				if (snapshot.getString("name") == null) {
					Toast.makeText(view.getContext(), "Error getting the store's information",
						Toast.LENGTH_LONG).show();
					dialog.dismiss();
					return;
				}
				mStore = new Store();
				mStore.setName(snapshot.getString("name"));
				mStore.setCategory(snapshot.getString("category"));
				mStore.setImageUri(snapshot.getString("imagePath"));
				mStore.setLocation(snapshot.getGeoPoint("location"));
				mStore.setUserRoll(mUserDB.getCurrentStoreType());
				dialog.dismiss();
				setUpDataInView(view);
			});
	}

	public void setUpDataInView(View view) {

		view.findViewById(R.id.profileLogOutButton).setOnClickListener(this::onClickLogOut);
		view.findViewById(R.id.profileEditProfileButton).setOnClickListener(this::onClickEditStore);
		view.findViewById(R.id.profileDeleteProfileButton)
			.setOnClickListener(this::onClickDeleteStore);
		view.findViewById(R.id.profileChangeStoreButton)
			.setOnClickListener(this::onClickChangeStore);
		view.findViewById(R.id.profileChangeStoreLocation)
			.setOnClickListener(this::onClickChangeLocation);
		view.findViewById(R.id.storePendingGiftsLayout).setOnClickListener(this::onClickPending);
		view.findViewById(R.id.storeCompletedGiftsLayout)
			.setOnClickListener(this::onClickCompleted);

		mStoreNameText.setText(mStore.getName());

		if (mStore.getImageUri() != null) {
			StorageReference storageReference =
				FirebaseStorage.getInstance().getReference(mStore.getImageUri());
			// Load the image using Glide
			Glide.with(view.getContext())
				.load(storageReference)
				.into((ImageView) view.findViewById(R.id.profilePictureInProfile));
		}
	}

	public void onClickEditStore(View view) {
		Intent intent = new Intent(view.getContext(), EditStore.class);
		intent.putExtra("storeId", mUserDB.getCurrentStoreId());
		intent.putExtra("name", mStore.getName());
		intent.putExtra("category", mStore.getCategory());
		mLaunchEditStore.launch(intent);
	}

	public void onClickDeleteStore(View view) {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
		builder.setTitle("STORE DELETION");
		builder.setMessage("Are you sure you want to delete your store?");
		builder.setPositiveButton("Yes", (dialogInterface, i) -> {
			mDb.collection("stores").document(mUserDB.getCurrentStoreId()).delete();
			Intent intent = new Intent(requireActivity(), SelectStore.class);
			startActivity(intent);
		});
		builder.setNegativeButton("No", (dialogInterface, i) -> {
			dialogInterface.dismiss();
		});
		builder.show();
	}

	public void onClickChangeStore(View view) {
		Intent intent = new Intent(requireActivity(), SelectStore.class);
		startActivity(intent);
	}

	public void onClickChangeLocation(View view) {
		Intent intent = new Intent(requireActivity(), EditAddress.class);
		intent.putExtra("storeId", mUserDB.getCurrentStoreId());
		mLaunchUpdateAddress.launch(intent);
	}

	ActivityResultLauncher<Intent> mLaunchUpdateAddress =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				Toast.makeText(requireContext(), "Address updated", Toast.LENGTH_SHORT).show();
			}
		});

	ActivityResultLauncher<Intent> mLaunchEditStore =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			Log.d("EDIT", "Callback here");
			if (result.getResultCode() == -1) {
				Log.d("EDIT", "Editing here");
				Intent data = result.getData();
				if (data != null) {
					String newStoreName = data.getStringExtra("name");
					String newCategory = data.getStringExtra("category");
					String newImage = data.getStringExtra("image");
					if (newStoreName == null)
						return;
					mStore.setName(newStoreName);
					mStoreNameText.setText(mStore.getName());
					mStore.setCategory(newCategory);
					if (newImage != null) {
						FirebaseStorage.getInstance()
							.getReference()
							.child(mStore.getImageUri())
							.delete();
						mStore.setImageUri(newImage);
						StorageReference storageReference =
							FirebaseStorage.getInstance().getReference(mStore.getImageUri());
						Glide.with(requireContext())
							.load(storageReference)
							.into((ImageView) requireView().findViewById(
								R.id.profilePictureInProfile));
					}
				}
			}
		});

	public void onClickLogOut(View view) {
		mAuth.signOut();
		Intent intent = new Intent(requireActivity(), MainActivity.class);
		startActivity(intent);
	}

	public void onClickPending(View view) {
		Intent intent = new Intent(view.getContext(), GiftListActivity.class);
		intent.putExtra("storeName", mStore.getName());
		intent.putExtra("user", mUserDB);
		intent.putExtra("used", false);
		mLaunchViewGifts.launch(intent);
	}

	public void onClickCompleted(View view) {
		Intent intent = new Intent(view.getContext(), GiftListActivity.class);
		intent.putExtra("storeName", mStore.getName());
		intent.putExtra("user", mUserDB);
		intent.putExtra("used", true);
		mLaunchViewGifts.launch(intent);
	}

	ActivityResultLauncher<Intent> mLaunchViewGifts =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		});

	public void getNumberOfPendingGifts(DocumentReference storeReference) {
		Query query = mDb.collection("gifts")
			.whereEqualTo("store", storeReference)
			.whereEqualTo("is_used", false);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mPedingGiftsCount.setText(String.valueOf(snapshot.getCount()));
					} catch (Throwable e) {
						mPedingGiftsCount.setText("0");
						e.printStackTrace();
					}
				} else {
					mPedingGiftsCount.setText("0");
				}
			});
	}

	public void getNumberOfCompletedGifts(DocumentReference storeReference) {
		Query query = mDb.collection("gifts")
			.whereEqualTo("store", storeReference)
			.whereEqualTo("is_used", true);
		AggregateQuery countQuery = query.count();
		countQuery.get(AggregateSource.SERVER)
			.addOnCompleteListener((OnCompleteListener<AggregateQuerySnapshot>) task -> {
				if (task.isSuccessful()) {
					try {
						AggregateQuerySnapshot snapshot =
							(AggregateQuerySnapshot) task.getResult();
						mCompletedGiftsCount.setText(String.valueOf(snapshot.getCount()));
					} catch (Throwable e) {
						mCompletedGiftsCount.setText("0");
						e.printStackTrace();
					}
				} else {
					mCompletedGiftsCount.setText("0");
				}
			});
	}
}