package comp5216.sydney.edu.au.storeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Store;


public class SelectStore extends AppCompatActivity {
	private static final String TAG = "SelectStore";
	private FirebaseFirestore db;
	private FirebaseUser currentUser;
	private ListView listView;
	private ArrayList<Store> stores;
	private StoresAdapter storesAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_store);

		listView = findViewById(R.id.selectStoreListView);

		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		db = FirebaseFirestore.getInstance();
		stores = new ArrayList<>();

		storesAdapter = new StoresAdapter(this, stores, this::onClickSelectStore);
		listView.setAdapter(storesAdapter);
		getStores();
	}

	public void onClickSelectStore(Store store) {
		Map<String, Object> userCurrentStore = new HashMap<>();
		userCurrentStore.put("currentStore", store.getRef());
		userCurrentStore.put("currentStoreType", store.getUserRoll());
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(currentUser.getUid())
			.set(userCurrentStore, SetOptions.merge())
			.addOnCompleteListener((Task<Void> t) -> {
				Log.d(TAG, "DocumentSnapshot<users> updated with ID: " + currentUser.getUid());
				Intent intent = new Intent(SelectStore.this, Home.class);
				startActivity(intent);
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.e(TAG, "Error updating document userStore", e);
				Toast.makeText(this, "There was an error selecting this store", Toast.LENGTH_SHORT)
					.show();
			});
	}

	public void getStores() {
		DocumentReference userReference = db.collection("users").document(currentUser.getUid());
		db.collection("userStores")
			.whereEqualTo("user", userReference)
			.get()
			.addOnCompleteListener(q -> {
				List<DocumentSnapshot> docs = q.getResult().getDocuments();
				for (DocumentSnapshot doc : docs) {
					DocumentReference storeRef = doc.get("store", DocumentReference.class);
					if (storeRef == null)
						continue;
					storeRef.get().addOnCompleteListener(ds -> {
						DocumentSnapshot result = ds.getResult();
						if (result != null && result.getString("name") != null) {
							Store store = new Store();
							store.setName(result.get("name", String.class));
							store.setCategory(result.get("category", String.class));
							store.setLocation(result.get("location", GeoPoint.class));
							store.setImageUri(result.get("imagePath", String.class));
							store.setRef(result.getReference());
							store.setUserRoll(doc.getString("type"));
							stores.add(store);
							storesAdapter.notifyDataSetChanged();
							Log.d(TAG, "Doc: " + store.getName());
						}
					});
				}
			});
	}

	public void onNewStoreClick(View view) {
		Intent intent = new Intent(SelectStore.this, NewStore.class);
		startActivity(intent);
	}

}