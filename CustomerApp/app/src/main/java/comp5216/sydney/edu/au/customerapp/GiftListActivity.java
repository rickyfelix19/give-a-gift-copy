package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.customerapp.dataHelpers.Gift;

public class GiftListActivity extends AppCompatActivity {

	private FirebaseFirestore db;
	private FirebaseUser currentUser;
	private ListView listView;
	private ArrayList<Gift> gifts;
	private GiftAdapter giftAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_gifts_view);

		listView = findViewById(R.id.selectGiftListView);

		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		db = FirebaseFirestore.getInstance();

		gifts = new ArrayList<>();
		giftAdapter = new GiftAdapter(this, gifts, this::onClickGift);
		listView.setAdapter(giftAdapter);

		getGifts();
	}

	private void onClickGift(Gift gift) {
		Intent intent = new Intent(GiftListActivity.this, ScanQR.class);
		intent.putExtra("qr", gift.getRef().getId());
		intent.putExtra("sender", gift.getSender());
		intent.putExtra("receiver", gift.getReceiver());
		intent.putExtra("store", gift.getStore());
		intent.putExtra("description", gift.getDescription());
		intent.putExtra("latitude", gift.getLatitude());
		intent.putExtra("longitude", gift.getLongitude());
		startActivity(intent);
	}


	private void getGifts() {
		DocumentReference userReference = db.collection("users").document(currentUser.getUid());

		db.collection("gifts")
			.whereEqualTo("receiver", userReference)
			.whereEqualTo("is_used", false)
			.get()
			.addOnCompleteListener(q -> {
				List<DocumentSnapshot> docs = q.getResult().getDocuments();

				for (DocumentSnapshot doc : docs) {

					DocumentReference senderRef = doc.get("sender", DocumentReference.class);
					DocumentReference storeRef = doc.get("store", DocumentReference.class);
					DocumentReference receiverRef = doc.get("receiver", DocumentReference.class);

					senderRef.get().addOnCompleteListener(se -> {
						storeRef.get().addOnCompleteListener(st -> {
							receiverRef.get().addOnCompleteListener(re -> {

								DocumentSnapshot sender = se.getResult();
								DocumentSnapshot store = st.getResult();
								DocumentSnapshot receiver = re.getResult();

								// Creating each gift
								Gift gift = new Gift();

								if (sender != null) {
									String senderName = sender.get("name", String.class) + " " +
										sender.get("lastname", String.class);
									gift.setSender(senderName);
								}

								if (receiver != null) {
									String receiverName = receiver.get("name", String.class) +
										" " +
										receiver.get("lastname", String.class);
									gift.setReceiver(receiverName);
								}

								if (store != null) {
									gift.setStore(store.get(("name"), String.class));
									GeoPoint location = store.get("location", GeoPoint.class);
									gift.setLatLon(location);
								}

								String description = doc.get("description", String.class);
								gift.setDescription(description);

								gift.setRef(doc.getReference());

								gifts.add(gift);
								giftAdapter.notifyDataSetChanged();
							});
						});
					});
				}
			});
	}
}
