package comp5216.sydney.edu.au.storeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class GiftSummaryActivity extends AppCompatActivity {
	FirebaseFirestore db;
	DocumentReference gift;

	//need from the intent
	private String giftId;

	TextView senderTextView;
	TextView receiverTextView;
	TextView giftDescription;
	TextView receivedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gift_summary);

		//Set Attributes
		senderTextView = (TextView) findViewById(R.id.sender_textview);
		receiverTextView = (TextView) findViewById(R.id.receiver_textview);
		giftDescription = (TextView) findViewById(R.id.gift_description);
		receivedDate = (TextView) findViewById(R.id.received_date);
		Intent intent = getIntent();
		giftId = intent.getStringExtra("giftId");
		boolean is_used = intent.getBooleanExtra("is_used", false);
		if (is_used) {
			((MaterialButton) findViewById(R.id.buttonCompleteGift)).setVisibility(View.GONE);
		}
		//Get the gift information
		db = FirebaseFirestore.getInstance();
		gift = db.collection("gifts").document(giftId);
		gift.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
			@Override
			public void onSuccess(DocumentSnapshot documentSnapshot) {
				if (!documentSnapshot.exists()) {
					Log.d("TAG", "onSuccess: LIST EMPTY");
					return;
				} else {
					//Get the receiver's name
					documentSnapshot.getDocumentReference("receiver")
						.get()
						.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
							@Override
							public void onSuccess(DocumentSnapshot documentSnapshot) {
								if (!documentSnapshot.exists()) {
									Log.d("TAG", "onSuccess: LIST EMPTY");
									receiverTextView.setText("user without account");
								} else {
									String receiverName = documentSnapshot.getString("name");
									receiverTextView.setText(receiverName);
								}

							}
						});

					//Get the sender's name
					documentSnapshot.getDocumentReference("sender")
						.get()
						.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
							@Override
							public void onSuccess(DocumentSnapshot documentSnapshot) {
								if (!documentSnapshot.exists()) {
									Log.d("TAG", "onSuccess: LIST EMPTY");
									senderTextView.setText("user without account");
								} else {
									String senderName = documentSnapshot.getString("name");
									senderTextView.setText(senderName);
								}

							}
						});

					//Set expiry Date
					Timestamp t = documentSnapshot.getTimestamp("data_received");
					Date d = new Date(t.getSeconds() * 1000);
					receivedDate.setText(d.toString());

					//Set Item List
					String giftContent = documentSnapshot.getString("description");
					String[] giftContentList = giftContent.split(", ");
					String giftContentText = "";
					for (int i = 0; i < giftContentList.length; i++) {
						giftContentText += giftContentList[i] + "\n";
					}
					giftDescription.setText(giftContentText);
				}
			}
		});


	}

	public void onClickCheck(View view) {
		DocumentReference giftReference = db.collection("gifts").document(giftId);
		giftReference.update("is_used", true);
		Toast.makeText(this, "Consumed the Gift", Toast.LENGTH_SHORT).show();
	}
}
