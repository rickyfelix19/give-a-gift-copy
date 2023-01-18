package comp5216.sydney.edu.au.storeapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAddress extends Activity {
	MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
	private static final String TAG = "AddAddress";
	private EditText storeAddressText;
	private String storeId = null;
	private GeoPoint storeLocation = null;
	private GeoPoint storeAddressLocation = null;
	private FusedLocationProviderClient fusedLocationClient;

	private boolean uploading = false;
	private boolean gettingLocation = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FirebaseAuth.getInstance().getCurrentUser();
		setContentView(R.layout.new_store_location);
		Intent data = getIntent();
		storeId = data.getStringExtra("storeId");
		storeAddressText = findViewById(R.id.storeAddress);
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

	}

	public void onCurrentLocationClick(View view) {
		if (!marshmallowPermission.checkPermissionForLocation()) {
			marshmallowPermission.requestPermissionForLocation();
		} else {
			getLocation();
		}
	}

	public void onTestAddressClick(View view) {
		getLocationFromAddress(storeAddressText.getText().toString(), false);
	}

	public void onSaveAddress(View view) {
		if (uploading) {
			Toast.makeText(this, "We are already saving your address, please wait",
				Toast.LENGTH_SHORT).show();
			return;
		}
		if (!storeAddressText.getText().toString().equals("")) {
			getLocationFromAddress(storeAddressText.getText().toString(), true);
		} else {
			updateStoreLocation();
		}
	}

	public void getLocationFromAddress(String addressStr, boolean upload) {
		Toast.makeText(this, "Getting the store location from the address", Toast.LENGTH_SHORT)
			.show();
		Geocoder geocoder = new Geocoder(this);
		try {
			List<Address> addressList = geocoder.getFromLocationName(addressStr, 1);
			if (addressList.size() == 0) {
				Toast.makeText(this, "We could not get any locations from that address",
					Toast.LENGTH_SHORT).show();
				storeAddressLocation = null;
				return;
			}
			Address address = addressList.get(0);
			storeAddressLocation = new GeoPoint(address.getLatitude(), address.getLongitude());
			Toast.makeText(this, "We got a location for the address", Toast.LENGTH_SHORT).show();
			if (upload) {
				updateStoreLocation();
			} else {
				Uri gmmIntentUri = Uri.parse("geo:" + storeAddressLocation.getLatitude() + "," +
					storeAddressLocation.getLongitude() + "?q=" +
					storeAddressLocation.getLatitude() + "," + storeAddressLocation.getLongitude());
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
				mapIntent.setPackage("com.google.android.apps.maps");
				startActivity(mapIntent);
			}
		} catch (IOException e) {
			Toast.makeText(this, "We could not get any locations from that address",
				Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void updateStoreLocation() {
		if (storeId == null) {
			Toast.makeText(this, "ID missing", Toast.LENGTH_SHORT).show();
			return;
		}
		if (storeLocation == null && storeAddressLocation == null) {
			Toast.makeText(this, "Please select an address or location", Toast.LENGTH_SHORT).show();
			return;
		}
		GeoPoint location;
		if (storeAddressLocation != null) {
			location = storeAddressLocation;
		} else {
			location = storeLocation;
		}
		uploading = true;
		Map<String, GeoPoint> storeForDB = new HashMap<>();
		storeForDB.put("location", location);
		FirebaseFirestore.getInstance()
			.collection("stores")
			.document(storeId)
			.set(storeForDB, SetOptions.merge())
			.addOnCompleteListener((Task<Void> t) -> {
				Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
				uploading = false;
				Intent intent = new Intent(AddAddress.this, Home.class);
				startActivity(intent);
			})
			.addOnFailureListener(e -> {
				Toast.makeText(AddAddress.this, "Error saving the address", Toast.LENGTH_SHORT)
					.show();
				uploading = false;
			});
	}

	@SuppressLint("MissingPermission") // we have permission, checked before
	public void getLocation() {
		if (gettingLocation) {
			Toast.makeText(this, "We are already getting your location, please wait",
				Toast.LENGTH_SHORT).show();
			return;
		}
		gettingLocation = true;
		Toast.makeText(this, "Getting your current location", Toast.LENGTH_SHORT).show();
		fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
			.addOnCompleteListener(l -> {
				storeLocation =
					new GeoPoint(l.getResult().getLatitude(), l.getResult().getLongitude());
				Toast.makeText(this, "We now have your location", Toast.LENGTH_SHORT).show();
				storeAddressText.setText("");
				gettingLocation = false;
			})
			.addOnFailureListener(e -> {
				Toast.makeText(this, "Error getting your location", Toast.LENGTH_SHORT).show();
				gettingLocation = false;
			});
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions,
	                                       int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int result : grantResults) {
			if (result != 0) {
				Toast.makeText(this, "Permissions not granted, so we cannot continue",
					Toast.LENGTH_LONG).show();
				return;
			}
		}
		getLocation();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}
}
