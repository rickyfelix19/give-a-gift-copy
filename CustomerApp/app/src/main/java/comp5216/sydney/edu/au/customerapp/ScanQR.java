package comp5216.sydney.edu.au.customerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;

import java.util.Objects;

public class ScanQR extends AppCompatActivity {

    private TextView sender_textview;
    private TextView receiver_textview;
    private TextView store_name_textview;
    private TextView item_description_textview;
    private ImageView qr_view;

    private Double lat;
    private Double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_qr_page);

        qr_view = (ImageView) findViewById(R.id.qr_image_view);
        sender_textview  = (TextView) findViewById(R.id.sender_textview);
        receiver_textview  = (TextView) findViewById(R.id.receiver_textview);
        store_name_textview = (TextView) findViewById(R.id.storename_textview);
        item_description_textview = (TextView) findViewById(R.id.item_description_textview);

        Bundle extras = getIntent().getExtras();
        String qr_string_code = extras.getString("qr");
        String sender_string = extras.getString("sender");
        String receiver_string = extras.getString("receiver");
        String store_string = extras.getString("store");
        String description_string = extras.getString("description");
        lat = extras.getDouble("latitude");
        lon = extras.getDouble("longitude");

        sender_textview.setText(sender_string);
        receiver_textview.setText(receiver_string);
        store_name_textview.setText(store_string);
        item_description_textview.setText(description_string);

        Bitmap generated_bitmap = QRCode.from(qr_string_code).bitmap();
        qr_view.setImageBitmap(generated_bitmap);

    }

    public void onFindOnMapsClick(View view){
        if (Objects.nonNull(lat) && Objects.nonNull(lon)) {
            String stringToParse = "geo:0,0" + "?q=" + lat + "," + lon;
            Uri gmmIntentUri = Uri.parse(stringToParse);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }
}

