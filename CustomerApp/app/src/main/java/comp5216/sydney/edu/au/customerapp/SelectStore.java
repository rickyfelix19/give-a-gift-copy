package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SelectStore extends AppCompatActivity {
	//get user location from the entity
	double userLatitude = 0.0;
	double userLongitude = 0.0;

	//Define variables
	ListView listView;
	ArrayList<Store> stores = new ArrayList<Store>();
	StoreAdapter itemsAdapter;
	FirebaseFirestore db;

	@RequiresApi(api = Build.VERSION_CODES.P)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_store);

		//Reference the "listView" variable, set adapter and listener
		listView = (ListView) findViewById(R.id.lstView);
		db = FirebaseFirestore.getInstance();

		userLatitude = getIntent().getDoubleExtra("receiverLatitude", 0.0);
		userLongitude = getIntent().getDoubleExtra("receiverLongitude", 0.0);

		//Get the store list from the database
		Task<QuerySnapshot> task = db.collection("stores")
			.get()
			.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
				@Override
				public void onSuccess(QuerySnapshot documentSnapshots) {
					if (documentSnapshots.isEmpty()) {
						Log.d("TAG", "onSuccess: LIST EMPTY");
						return;
					} else {
						for (DocumentSnapshot documentSnapshot : documentSnapshots) {
							if (documentSnapshot.exists()) {
								Store store = new Store(documentSnapshot.getString("name"),
									documentSnapshot.getString("category"),
									(float) documentSnapshot.getGeoPoint("location").getLatitude(),
									(float) documentSnapshot.getGeoPoint("location").getLongitude(),
									documentSnapshot.getId(),
									documentSnapshot.getString("imagePath"));
								if (distance(store.getStoreLatitude(), store.getStoreLongitude(),
									userLatitude, userLongitude) < 5) {
									stores.add(store);
								}
							}
						}
						//sort the item
						Collections.sort(stores, new Comparator<Store>() {
							@Override
							public int compare(Store s1, Store s2) {
								double di1 = s1.getDistanceFrom(userLatitude, userLongitude);
								double di2 = s2.getDistanceFrom(userLatitude, userLongitude);
								if (di1 > di2)
									return 1;
								else if (di1 == di2)
									return 0;
								else
									return -1;
							}
						});
						//update the screen
						itemsAdapter.notifyDataSetChanged();
					}
				}
			});

		//Set up the adapter
		itemsAdapter = new StoreAdapter(stores, userLongitude, userLatitude);
		System.out.println(stores.size());
		listView.setAdapter(itemsAdapter);
		setupListViewListener();
	}

	public void setupListViewListener() {
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView adapterView, View view, int position, long l) {
				Store exploreItem = (Store) adapterView.getItemAtPosition(position);
				Intent intent = new Intent(SelectStore.this, SelectProduct.class);
				if (intent != null) {
					//Add extras that will be used in next activity
					intent.putExtra("receiverId", getIntent().getStringExtra("receiverId"));
					intent.putExtra("senderId",getIntent().getStringExtra("senderId"));
					intent.putExtra("storeId", exploreItem.getStoreId());

					intent.putExtra("senderName", getIntent().getStringExtra("senderName"));
					intent.putExtra("receiverName", getIntent().getStringExtra("receiverName"));

					startActivity(intent);
				}
			}
		});
	}

	//source : https://www.geodatasource.com/developers/java
	private static double distance(double lat1, double lon1, double lat2, double lon2) {
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		} else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
					Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;
			return (dist);
		}
	}

}
