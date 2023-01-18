package comp5216.sydney.edu.au.storeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Gift;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;


public class GiftListActivity extends AppCompatActivity {

	private FirebaseFirestore db;
	private FirebaseUser currentUser;
	private UserDB mUser;
	private boolean used;
	private String storeName;
	private ListView listView;
	private ArrayList<Gift> gifts;
	private GiftAdapter giftAdapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_gifts_view);

		Intent data = getIntent();
		storeName = data.getStringExtra("storeName");
		mUser = data.getParcelableExtra("user");
		used = data.getBooleanExtra("used", false);
		((TextView) findViewById(R.id.viewGiftsTitle)).setText(
			used ? "COMPLETED GIFTS" : "PENDING GIFTS");

		listView = findViewById(R.id.selectGiftListView);
		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		db = FirebaseFirestore.getInstance();

		gifts = new ArrayList<>();
		giftAdapter = new GiftAdapter(this, gifts, this::onClickGift);
		listView.setAdapter(giftAdapter);
		getGifts();
	}

	private void onClickGift(Gift gift) {
		Intent intent = new Intent(this, GiftSummaryActivity.class);
		intent.putExtra("giftId", gift.getRef().getId());
		intent.putExtra("is_used", used);
		startActivity(intent);
	}

	private void getGifts() {
		DocumentReference storeReference =
			db.collection("stores").document(mUser.getCurrentStoreId());
		db.collection("gifts")
			.whereEqualTo("store", storeReference)
			.whereEqualTo("is_used", used)
			.get()
			.addOnCompleteListener(q -> {
				List<DocumentSnapshot> docs = q.getResult().getDocuments();

				for (DocumentSnapshot doc : docs) {

					DocumentReference senderRef = doc.get("sender", DocumentReference.class);
					DocumentReference receiverRef = doc.get("receiver", DocumentReference.class);

					senderRef.get().addOnCompleteListener(se -> {
						receiverRef.get().addOnCompleteListener(re -> {
							DocumentSnapshot sender = se.getResult();
							DocumentSnapshot receiver = re.getResult();
							// Creating each gift
							Gift gift = new Gift();
							if (sender != null) {
								String senderName = sender.get("name", String.class) + " " +
									sender.get("lastname", String.class);
								gift.setSender(senderName);
							}
							if (receiver != null) {
								String receiverName = receiver.get("name", String.class) + " " +
									receiver.get("lastname", String.class);
								gift.setReceiver(receiverName);
							}
							gift.setStore(storeName);
							String description = doc.get("description", String.class);
							gift.setDescription(description);
							gift.setRef(doc.getReference());
							gifts.add(gift);
							giftAdapter.notifyDataSetChanged();
						});
					});
				}
			});
	}
}
