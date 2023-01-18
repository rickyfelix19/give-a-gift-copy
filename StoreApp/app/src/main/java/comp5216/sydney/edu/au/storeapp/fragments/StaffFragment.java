package comp5216.sydney.edu.au.storeapp.fragments;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import comp5216.sydney.edu.au.storeapp.AddNewStaff;
import comp5216.sydney.edu.au.storeapp.R;
import comp5216.sydney.edu.au.storeapp.StaffAdapter;
import comp5216.sydney.edu.au.storeapp.dataHelpers.Staff;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;


public class StaffFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	private UserDB userDB;
	private String mParam2;

	RecyclerView recyclerView;
	FirebaseFirestore mFirestore;
	StaffAdapter staffAdapter;
	ArrayList<Staff> staffs;


	public StaffFragment() {
		// Required empty public constructor
	}

	public static StaffFragment newInstance(UserDB param1, String param2) {
		StaffFragment fragment = new StaffFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			userDB = getArguments().getParcelable(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.staff_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.addStaffBtn).setOnClickListener(this::onClickAddStaff);
		recyclerView = requireView().findViewById(R.id.staffListData);
		recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

		mFirestore = FirebaseFirestore.getInstance();
		staffs = new ArrayList<>();
		staffAdapter = new StaffAdapter(requireActivity(), staffs);

		recyclerView.setAdapter(staffAdapter);

		EventChangeListener();

		EditText searchEditText = requireView().findViewById(R.id.staffSearchInput);
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

	public void onClickAddStaff(View view) {
		Intent intent = new Intent(getContext(), AddNewStaff.class);
		intent.putExtra("user", userDB);
		mLaunchAddStaff.launch(intent);
	}

	ActivityResultLauncher<Intent> mLaunchAddStaff =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				staffs.clear();
				EventChangeListener();
			}
		});

	private void filter(String text) {
		ArrayList<Staff> filteredList = new ArrayList<>();

		for (Staff item : staffs) {
			if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
				item.getLastName().toLowerCase().contains(text.toLowerCase())) {
				filteredList.add(item);
			}
		}

		staffAdapter.filterList(filteredList);
	}

	private void EventChangeListener() {
		DocumentReference storeRef =
			mFirestore.collection("stores").document(userDB.getCurrentStoreId());
		DocumentReference userRef = mFirestore.collection("users").document(userDB.getId());

		Query st = mFirestore.collection("userStores").whereEqualTo("store", storeRef);

		st.get().addOnSuccessListener(queryDocumentSnapshots -> {
			for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
				DocumentReference userRef1 = documentSnapshot.getDocumentReference("user");
				if (userRef1 == null || userRef1.equals(userRef))
					continue;
				userRef1.get().addOnSuccessListener(userSnapshot -> {
					Staff staff = userSnapshot.toObject(Staff.class);
					staff.setDocumentId(userSnapshot.getId());
					staff.setUserStoresId(documentSnapshot.getId());
					staffs.add(staff);
					staffAdapter.notifyDataSetChanged();
				});
			}
		});
	}
}
