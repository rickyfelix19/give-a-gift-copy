package comp5216.sydney.edu.au.customerapp.dataHelpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Store {
    private String storeName;
    private String storeCategory;
    private float storeLatitude;
    private float storeLongitude;
    private String storePhoto;
    private String storeId;

    public Store(){
    }

    public Store(String storeName, String storeCategory, float storeLatitude, float storeLongitude, String storeId, String storePhoto){
        this.storeName = storeName;
        this.storeCategory = storeCategory;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.storeId= storeId;
        this.storePhoto = storePhoto;
    }

    public String getStoreName(){
        return this.storeName;
    }

    public void setStoreName(String newStoreName){
        this.storeName = newStoreName;
    }

    public String getStoreCategory(){
        return this.storeCategory;
    }

    public void setStoreCategory(String newStoreCategory){
        this.storeCategory = newStoreCategory;
    }

    public float getStoreLongitude(){
        return this.storeLongitude;
    }

    public void setStoreLongitude(float newStoreLongitude){
        this.storeLongitude = newStoreLongitude;
    }

    public float getStoreLatitude(){
        return storeLatitude;
    }

    public void setStoreLatitude(float newStoreLatitude){
        this.storeLatitude = newStoreLatitude;
    }


    public String getStoreId(){
        return this.storeId;
    }
    public void setStoreId(String newStoreId){
        this.storeId = newStoreId;
    }
    public void getStorePhoto(String newStorePhoto){this.storePhoto = newStorePhoto;}

    public String getStorePhoto(){
        return this.storePhoto;
    }

    //source : https://www.geodatasource.com/developers/java
    public double getDistanceFrom(double lat2, double lon2) {
        if ((this.storeLatitude == lat2) && (this.storeLongitude == lon2)) {
            return 0;
        }
        else {
            double theta = this.storeLongitude - lon2;
            double dist = Math.sin(Math.toRadians(this.storeLatitude)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(this.storeLatitude)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }
}





