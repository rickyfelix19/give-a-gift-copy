package comp5216.sydney.edu.au.customerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Checkout extends AppCompatActivity {

    TextView sender;
    TextView receiver;
    TextView productname;
    TextView price;

    ImageView dropitem;

    TextView subtotal;
    TextView fees;
    TextView total;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_screen);

        sender = findViewById(R.id.sender);
        receiver = findViewById(R.id.receiver);
        productname = findViewById(R.id.product_name);
        price = findViewById(R.id.price_view);
        dropitem = findViewById(R.id.remove_items_image);
        subtotal = findViewById(R.id.subtotal);
        fees = findViewById(R.id.fees);
        total = findViewById(R.id.total);

        sender.setText(getIntent().getStringExtra("senderName"));
        receiver.setText(getIntent().getStringExtra("receiverName"));

        productname.setText(getIntent().getStringExtra("giftDescription"));
        price.setText("$"+String.valueOf(getIntent().getFloatExtra("totalPrice", 0)));
        subtotal.setText("$"+String.valueOf(getIntent().getFloatExtra("totalPrice", 0)));
        fees.setText("$"+"0");
        total.setText("$"+String.valueOf(getIntent().getFloatExtra("totalPrice", 0)));
    }

    public void onProceedPayment(View view) {
        Intent intent = new Intent(Checkout.this, PaymentDetails.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    public void onClickRemoveItems(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Checkout.this);
        builder.setTitle("Remove Item")
                .setMessage("Are you sure that you want to remove" +
                        " the item and return to main menu?")
                .setPositiveButton(R.string.Yes, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Checkout.this, Home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                // User cancelled the dialog and nothing happens
                .setNegativeButton(R.string.No, new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
        builder.create().show();
    }
}
