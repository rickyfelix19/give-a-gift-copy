package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import comp5216.sydney.edu.au.customerapp.fragments.WalletFragment;

public class SelectProduct extends AppCompatActivity implements CalculatePriceCaller {
    //Define variables
    private static final DecimalFormat df = new DecimalFormat("0.00");
    GridView gridView;
    ArrayList<Product> products = new ArrayList<Product>();
    ProductAdapter itemsAdapter;
    FirebaseFirestore db;
    float totalPrice = 0.0F;
    Button totalPriceView;
    TextView checkout;
    Map<String, Integer> userSelectedProducts = new HashMap<String, Integer>();

    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void onCreate(Bundle savedInstanceState) {
        String storeId = getIntent().getStringExtra("storeId");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_product);
        itemsAdapter = new ProductAdapter(products);
        gridView = (GridView) findViewById(R.id.gridView);
        totalPriceView = (Button) findViewById(R.id.totalprice);
        checkout = (TextView) findViewById(R.id.checkout);

        //Get the 'products' collection from the store document then put it into the products
        db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> task = db.collection("stores").document(storeId).collection("products")
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
                                    Product product = new Product(documentSnapshot.getString("name"), documentSnapshot.getDouble("price").floatValue(), 0,documentSnapshot.getString("imagePath") );
                                    products.add(product);
                                }
                            }
                        }
                    }
                })
                .addOnCompleteListener(this, new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                            //update the screen
                            itemsAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

        gridView.setAdapter(itemsAdapter);
        setupCheckoutListener();
    }

    public void setupCheckoutListener(){
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SelectProduct.this, Checkout.class);
                intent.putExtra("receiverId", getIntent().getStringExtra("receiverId"));
                intent.putExtra("senderId", getIntent().getStringExtra("senderId"));
                intent.putExtra("storeId", getIntent().getStringExtra("storeId"));
                intent.putExtra("senderName", getIntent().getStringExtra("senderName"));
                intent.putExtra("receiverName", getIntent().getStringExtra("receiverName"));

                String items = "";
                for(String itemName : userSelectedProducts.keySet()){
                    if(userSelectedProducts.get(itemName) > 0){
                        items += String.valueOf(userSelectedProducts.get(itemName))+"x "+itemName+ ", ";
                    }
                }
	            if (items.length() <= 2) {
		            Toast.makeText(v.getContext(), "You cannot checkout with 0 items",
			            Toast.LENGTH_SHORT).show();
		            return;
	            }
                items = items.substring(0, items.length() - 2);
                intent.putExtra("giftDescription", items);
                intent.putExtra("totalPrice", totalPrice);
                startActivity(intent);
            }
        });
    }

    @Override
    public void calculatePriceCaller(float addedValue, String productName, int productQuantity) {
        totalPrice += addedValue;
        totalPriceView.setText("Total Price: \t\t\t\t\t\t " + df.format(totalPrice)+ "$");
        userSelectedProducts.put(productName, productQuantity);

    }
}

