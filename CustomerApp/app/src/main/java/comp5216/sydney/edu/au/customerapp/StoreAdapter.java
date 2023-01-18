package comp5216.sydney.edu.au.customerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import com.bumptech.glide.Glide;
public class StoreAdapter extends BaseAdapter {
    //Define variables
    ArrayList<Store> stores;
    double receiverLongitude = 0.0;
    double receiverLatitude = 0.0;


    public StoreAdapter(ArrayList<Store> stores, double receiverLongitude, double receiverLatitude){
        this.stores = stores;
        this.receiverLongitude = receiverLongitude;
        this.receiverLatitude = receiverLatitude;
    }

    @Override
    public int getCount() {
        return stores.size();
    }

    @Override
    public Object getItem(int i) {
        return stores.get(i);
    }

    @Override
    public long getItemId(int i) {
        return stores.indexOf(getItem(i));
    }

    public void addItem(String storeName, String storeCategory, float storeLatitude, float storeLongitude, String storeId, String storePhoto){
        Store s = new Store(storeName, storeCategory, storeLatitude, storeLongitude, storeId, storePhoto);
        stores.add(s);
    }

    public void addItem(Store newItem){
        stores.add(newItem);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final Context context = parent.getContext();

        //If the view to reuse does not exist, we create new View
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_in_store_list, parent, false);
        }

        //Reference the ImageView and TextView variable
        ImageView storeImage = (ImageView) convertView.findViewById(R.id.storeImage);
        TextView storeName = (TextView) convertView.findViewById(R.id.storeName);
        TextView storeCategory = (TextView) convertView.findViewById(R.id.storeCategory);
        TextView storeDistance = (TextView) convertView.findViewById(R.id.storeDistance);

        //Set up the current item we are working on
        Store targetItem = stores.get(position);

        //set up the image of the store
        StorageReference storageReference =
                FirebaseStorage.getInstance().getReference(targetItem.getStorePhoto());
        // Load the image using Glide
        Glide.with(context).load(storageReference).into(storeImage);

        //Set up the contents of TextView
        storeName.setText(targetItem.getStoreName());
        storeCategory.setText(targetItem.getStoreCategory());
        storeDistance.setText(String.format("%.2f", distance(targetItem.getStoreLatitude(), targetItem.getStoreLongitude(), receiverLatitude, receiverLongitude))+"km");

        return convertView;
    }

    //source : https://www.geodatasource.com/developers/java
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }

}
