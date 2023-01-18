package comp5216.sydney.edu.au.customerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class ProductAdapter extends BaseAdapter{
    //Define variables
    ArrayList<Product> products;
    float totalPrice = 0;

    public ProductAdapter(ArrayList<Product> products){
        this.products = products;
    }

    @Override
    public int getCount(){
        return products.size();
    }

    @Override
    public Object getItem(int i){
        return products.get(i);
    }

    @Override
    public long getItemId(int i){
        return products.indexOf(getItem(i));
    }

    public void addItem(String productName, float productPrice, String productPhoto){
        Product p = new Product(productName, productPrice, 0, productPhoto);
        products.add(p);
    }

    public void addItem(Product p){
        products.add(p);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final Context context = parent.getContext();

        //If the view to reuse does not exist, we create new View
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_in_product_list, parent, false);
        }

        //Reference the view objects
        ImageView productImage = (ImageView) convertView.findViewById(R.id.productImage);
        TextView productName = (TextView) convertView.findViewById(R.id.productName);
        TextView productPrice = (TextView) convertView.findViewById(R.id.productPrice);
        EditText productQuantity = (EditText) convertView.findViewById(R.id.productQuantity);
        Button decreaseQuantity = (Button) convertView.findViewById(R.id.btn_less);
        Button increaseQuantity = (Button) convertView.findViewById(R.id.btn_more);

        //Set up the current item we are working on
        Product targetItem = products.get(position);

        //set up the image of the product
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference(targetItem.getProductPhoto());
        // Load the image using Glide
        Glide.with(context).load(storageReference).into(productImage);


        //Set up the contents of TextView
        productName.setText(targetItem.getProductName());
        productPrice.setText(String.valueOf(targetItem.getProductPrice()));
        productQuantity.setText(String.valueOf(targetItem.getProductQuantity()));

        //Set up the button listener of Buttons
        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = true;
                if(targetItem.getProductQuantity() == 0){
                    flag = false;
                }
                targetItem.subtractProductQuantity();
                productQuantity.setText(String.valueOf(targetItem.getProductQuantity()));
                if(flag && context instanceof CalculatePriceCaller){
                    ((CalculatePriceCaller)context).calculatePriceCaller(-targetItem.getProductPrice(),
                            targetItem.getProductName(), targetItem.getProductQuantity());
                }
            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetItem.addProductQuantity();
                productQuantity.setText(String.valueOf(targetItem.getProductQuantity()));
                if(context instanceof CalculatePriceCaller){
                    ((CalculatePriceCaller)context).calculatePriceCaller(targetItem.getProductPrice(),
                            targetItem.getProductName(), targetItem.getProductQuantity());
                }
            }
        });
        return convertView;
    }


}

