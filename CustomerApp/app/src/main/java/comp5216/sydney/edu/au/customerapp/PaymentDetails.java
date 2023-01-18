package comp5216.sydney.edu.au.customerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentDetails extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_details);

    }

    public void onMakePayment(View view) {
        // ALWAYS VALIDATE SUCCESSFUL
        if (ValidateCardDetails()) {
            Intent intent = new Intent(PaymentDetails.this, PaymentSuccessful.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        }
    }

    public boolean ValidateCardDetails() {
        return true;
    }

}

