package comp5216.sydney.edu.au.storeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;
import comp5216.sydney.edu.au.storeapp.fragments.ProductsFragment;
import comp5216.sydney.edu.au.storeapp.fragments.ScanFragment;
import comp5216.sydney.edu.au.storeapp.fragments.StaffFragment;
import comp5216.sydney.edu.au.storeapp.fragments.StoreFragment;

public class Home extends AppCompatActivity {

	private FirebaseFirestore db;
	private FirebaseUser currentUser;
	private UserDB userDB;
	private boolean setInitial = true;
	private static Integer currentScreen = R.id.scanPageNav;

	public void openFragment(Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
		bottomNavigation.setSelectedItemId(currentScreen);

		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		db = FirebaseFirestore.getInstance();

		FirebaseFirestore.getInstance()
			.collection("users")
			.document(currentUser.getUid())
			.addSnapshotListener((snapshot, e) -> {
				if (e != null) {
					return;
				}
				if (snapshot != null && snapshot.exists() && snapshot.getString("name") != null) {
					userDB = new UserDB(currentUser, snapshot);
				}
			});
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(currentUser.getUid())
			.get()
			.addOnCompleteListener(task -> {
				DocumentSnapshot user = task.getResult();
				userDB = new UserDB(currentUser, user);
				if (setInitial) {
					selectFragment(currentScreen);
				}
			});

		bottomNavigation.setOnItemSelectedListener(
			(NavigationBarView.OnItemSelectedListener) menuItem -> selectFragment(
				menuItem.getItemId()));
	}

	public boolean selectFragment(Integer id) {
		setInitial = false;
		currentScreen = id;
		switch (id) {
			case R.id.scanPageNav:
				openFragment(ScanFragment.newInstance(userDB, ""));
				return true;
			case R.id.productPageNav:
				openFragment(ProductsFragment.newInstance(userDB, ""));
				return true;
			case R.id.staffPageNav:
				openFragment(StaffFragment.newInstance(userDB, ""));
				return true;
			case R.id.storePageNav:
				openFragment(StoreFragment.newInstance(userDB, ""));
				return true;
		}
		return false;
	}
}
