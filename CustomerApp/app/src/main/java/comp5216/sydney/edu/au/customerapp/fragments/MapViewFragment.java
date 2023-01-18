package comp5216.sydney.edu.au.customerapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import comp5216.sydney.edu.au.customerapp.R;
import comp5216.sydney.edu.au.customerapp.SelectStore;
import comp5216.sydney.edu.au.customerapp.dataHelpers.Friend;
import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, LocationListener {
	private GoogleMap map;
	LocationManager locationManager;
//	private ArrayList<Friend> friendList = new ArrayList<Friend>();

	final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
	final static int PERMISSIONS_ALL = 1;

	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	FirebaseFirestore db;
	boolean check = false;
	private Map<String, Friend> makerFriendMap;

	private UserDB mUserDB;
	private String mParam2;

	public MapViewFragment(){}

	public static MapViewFragment newInstance(UserDB userDB, String param2) {
		MapViewFragment fragment = new MapViewFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, userDB);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Log.i("mylog","TIMES");
		super.onCreate(savedInstanceState);
		db = FirebaseFirestore.getInstance();
//		this.friendList = new ArrayList<Friend>();
		if (getArguments() != null) {
			mUserDB = getArguments().getParcelable(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
		//getFriendLocation();

//		locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
//		if(Build.VERSION.SDK_INT>=23){
//			requestPermissions(PERMISSIONS, PERMISSIONS_ALL);
//		}else{
//			requestLocation();
//		}

//		requestPermissions(PERMISSIONS, PERMISSIONS_ALL);
//		requestLocation();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
//		SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
//				.findFragmentById(R.id.map);
//		if (mapFragment != null) {
//			mapFragment.getMapAsync(this);
//		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
		if (mapFragment == null) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			mapFragment = SupportMapFragment.newInstance();
			fragmentTransaction.replace(R.id.map, mapFragment).commit();
		}
		makerFriendMap = new HashMap<>();
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@SuppressLint("PotentialBehaviorOverride")
			@Override
			public void onMapReady(GoogleMap mMap) {
				map = mMap;

				//Getting the user's location
				getFriendLocation();
//				if(check == true){
//					setUpLocation();
//				}
				map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(@NonNull Marker marker) {
						Intent intent = new Intent(getActivity(), SelectStore.class);
						Friend friend = makerFriendMap.get(marker.getId());
						if (friend != null) {
							Log.d("santiago", "Friend: " + friend.getName());
							intent.putExtra("receiverLatitude", marker.getPosition().latitude);
							intent.putExtra("receiverLongitude", marker.getPosition().longitude);
							intent.putExtra("receiverId", friend.getId());
							intent.putExtra("senderId", mUserDB.getId());

							intent.putExtra("senderName", friend.toString());
							intent.putExtra("receiverName", mUserDB.toString());

							startActivity(intent);
							return true;
						}
						return true;
					}
				});
			}
		});

		locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		if(Build.VERSION.SDK_INT>=23){
			requestPermissions(PERMISSIONS, PERMISSIONS_ALL);
		}else{
			requestLocation();
		}
		return inflater.inflate(R.layout.map_view_fragment, container, false);
	}


	public void setUpLocation(ArrayList<Friend> friendList){
		Log.d("mylog","friend list achieved");
		ArrayList<Marker> markers = new ArrayList<Marker>();
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		if(friendList!=null) {
			boolean showCoords = false;
			Log.d("mylog","friend list not null");
			for (Friend friend : friendList) {
				Log.d("mylog","friend location: "+friend.getLocation());
				if (friend.getLocation()!=null) {
					showCoords =true;
					String name = friend.getName() + " " + friend.getLastname();
					LatLng friendLocation = new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude());
					Log.d("mylog", ""+friendLocation);
					IconGenerator icg = new IconGenerator(getContext());
					icg.setColor(Color.WHITE);
					icg.setTextAppearance(R.style.TextStyle2);
					Bitmap bm = icg.makeIcon(name);

					MarkerOptions mo = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bm)).position(friendLocation).title(name).visible(true);
					Marker markerFri = map.addMarker(mo);
					if (markerFri != null)
						makerFriendMap.put(markerFri.getId(), friend);
					builder.include(markerFri.getPosition());
					markers.add(markerFri);
					Log.d("mylog","add marker");
					// below lin is use to zoom our camera on map.
					map.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
					// below line is use to move our camera to the specific location.
					map.moveCamera(CameraUpdateFactory.newLatLng(friendLocation));
				}
			}
			if (showCoords) {
				LatLngBounds bounds = builder.build();
				int width = getResources().getDisplayMetrics().widthPixels;
				int height = getResources().getDisplayMetrics().heightPixels;
				int padding = (int) (width * 0.3); // offset from edges of the map in pixels
				try {
					 CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
					 map.animateCamera(cu);
				} catch (Exception e) {

				}
			}

		}
		Log.d("mylog","exit friendlist");
//		LatLng loc1 = new LatLng( -33.86, 151.20);
//		LatLng loc2 = new LatLng( -33.87, 151.25);
//		Marker m1 = map.addMarker(new MarkerOptions().position(loc1).title("YOU"));
//		markers.add(m1);
//		Marker m2 = map.addMarker(new MarkerOptions().position(loc2).title("test2"));
//		markers.add(m2);


//		for (Marker marker : markers) {
//			builder.include(marker.getPosition());
//		}

		// For zooming automatically to the location of the marker
//		CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLoc).zoom(10).build();
//		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {}

	public void getFriendLocation(){
		Log.d("mylog", "start get friend list");
		DocumentReference userReference = db.collection("users").document(mUserDB.getId());
		db.collection("friendList")
				.whereEqualTo("user", userReference)
				.whereEqualTo("accepted",true)
				.get()
				.addOnCompleteListener(q -> {
					ArrayList<Friend> friList = new ArrayList<Friend>();
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
							Log.d("mylog", friend.toString());
//
							friList.add(new Friend(result, Boolean.FALSE.equals(doc.getBoolean("accepted"))));
							if (count.decrementAndGet() <= 0)
								Log.d("mylog", "end get friend list");
								Log.d("mylog",friList.toString());
								setUpLocation(friList);
								return;
						});
					}
				});

	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		Log.d("mylog", "Get Location: "+location.getLatitude() + "," + location.getLongitude());
		GeoPoint geo_point = new GeoPoint(location.getLatitude(), location.getLongitude());
		saveUserLocation(geo_point);
//		locationManager.removeUpdates(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
			requestLocation();
		}
	}

	public void requestLocation(){
		if(locationManager == null){
			locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		}
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000, this);
			}
		}
	}

	public void saveUserLocation(GeoPoint geo_point) {
		Map<String, GeoPoint> curr_location = new HashMap<>();
		curr_location.put("location", geo_point);
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null){
			Log.d("mylog", "Null user");
			return;
		}else {
			Log.d("mylog", "Start add location to firebase");
			FirebaseFirestore.getInstance()
					.collection("users")
					.document(currentUser.getUid())
					.set(curr_location, SetOptions.merge())
					.addOnCompleteListener((Task<Void> t) -> {
						Log.d("TEST", "Location added to ID");
					})
					.addOnFailureListener((@NonNull Exception e) -> {
						Log.w("TEST", "Error adding location", e);
					});
		}
	}

}