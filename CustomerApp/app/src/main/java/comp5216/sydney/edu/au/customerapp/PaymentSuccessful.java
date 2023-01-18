package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentSuccessful extends AppCompatActivity {

    private FirebaseFirestore db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_succesful);
        db = FirebaseFirestore.getInstance();

        CreateGift();
    }

    private void CreateGift() {

        // Add a new document with a generated id.
        Map<String, Object> gift = new HashMap<>();

        DocumentReference senderReference = db.collection("users")
                .document(getIntent().getStringExtra("senderId"));

        DocumentReference receiverReference = db.collection("users")
                .document(getIntent().getStringExtra("receiverId"));

        DocumentReference storeReference = db.collection("stores")
                .document(getIntent().getStringExtra("storeId"));

        String description = getIntent().getStringExtra("giftDescription");

        gift.put("sender", senderReference);
        gift.put("receiver", receiverReference);
        gift.put("store", storeReference);
        gift.put("description", description);
        gift.put("is_used", false);
        gift.put("data_received", new Timestamp(new Date()));

        db.collection("gifts")
                .add(gift)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PaymentSuccessful.this,
                                "The gift has successfully been delivered.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PaymentSuccessful.this,
                                "Something went wrong.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onReturnHome(View view) {
        Intent intent = new Intent(PaymentSuccessful.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
