package comp5216.sydney.edu.au.customerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;
import comp5216.sydney.edu.au.customerapp.fragments.FriendsFragment;
import comp5216.sydney.edu.au.customerapp.fragments.MapViewFragment;
import comp5216.sydney.edu.au.customerapp.fragments.ProfileFragment;
import comp5216.sydney.edu.au.customerapp.fragments.WalletFragment;

public class Home extends AppCompatActivity {

	private UserDB userDB;
	private FirebaseAuth mAuth;
	private FirebaseFirestore mDb;
	public int currentView;
	private boolean setInitial = true;
	MarshmallowPermission marshmallowPermission;
	private FusedLocationProviderClient fusedLocationClient;
	private static Integer currentScreen = R.id.mapViewPageNav;

	public void openFragment(Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mAuth = FirebaseAuth.getInstance();
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser == null) {
			Intent intent = new Intent(Home.this, EmailPasswordSignIn.class);
			startActivity(intent);
			return;
		}
		currentView = R.id.mapViewPageNav;
		mDb = FirebaseFirestore.getInstance();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(currentUser.getUid())
			.addSnapshotListener((snapshot, e) -> {
				if (e != null) {
					return;
				}
				if (snapshot != null && snapshot.exists()) {
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

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
		if (!marshmallowPermission.checkPermissionForLocation()) {
			marshmallowPermission.requestPermissionForLocation();
		} else {
			getLocation();
		}
	}

	@SuppressLint("MissingPermission") // we have permission, checked before
	public void getLocation() {
		fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
			.addOnCompleteListener(l -> {
				saveUsersLocation(l.getResult());
			});
	}

	public void saveUsersLocation(@NonNull Location location) {
		Log.d("mylog", "Get Location: " + location.getLatitude() + "," + location.getLongitude());
		GeoPoint geo_point = new GeoPoint(location.getLatitude(), location.getLongitude());
		Map<String, GeoPoint> curr_location = new HashMap<>();
		curr_location.put("location", geo_point);
		Log.d("mylog", "Start add location to firebase");
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(userDB.getId())
			.set(curr_location, SetOptions.merge())
			.addOnCompleteListener((Task<Void> t) -> {
				Log.d("TEST", "Location added to ID");
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w("TEST", "Error adding location", e);
			});
	}

	public boolean selectFragment(Integer id) {
		setInitial = false;
		currentScreen = id;
		switch (id) {
			case R.id.mapViewPageNav:
				currentView = R.id.mapViewPageNav;
				openFragment(MapViewFragment.newInstance(userDB, ""));
				return true;
			case R.id.friendsPageNav:
				currentView = R.id.friendsPageNav;
				openFragment(FriendsFragment.newInstance(userDB, ""));
				return true;
			case R.id.profilePageNav:
				currentView = R.id.profilePageNav;
				openFragment(ProfileFragment.newInstance(userDB, ""));
				return true;
		}
		return false;
	}
}
