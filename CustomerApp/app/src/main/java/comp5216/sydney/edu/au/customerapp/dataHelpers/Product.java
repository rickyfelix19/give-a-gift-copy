package comp5216.sydney.edu.au.customerapp.dataHelpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Product {
    private int id;
    private String productName;
    private float productPrice;
    private int productQuantity;
    private String productPhoto;
    public static int count = 0;

    public Product(){
        this.id = count;
        count ++;
    }

    public Product(String productName, float productPrice, int productQuantity, String productPhoto){
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.id = count;
        this.productPhoto = productPhoto;
        count ++;
    }

    public String getProductName(){
        return this.productName;
    }

    public void setProductName(String productName){
        this.productName = productName;
    }

    public float getProductPrice(){
        return this.productPrice;
    }

    public void setProductPrice(float productPrice){
        this.productPrice = productPrice;
    }

    public int getProductQuantity(){
        return this.productQuantity;
    }

    public void setProductQuantity(int productQuantity){
        this.productQuantity = productQuantity;
    }

    public void addProductQuantity(){
        this.productQuantity += 1;
    }
    public void subtractProductQuantity(){
        this.productQuantity -= 1;
        if(this.productQuantity <=0 ){
            this.productQuantity = 0;
        }
    }
    public int getId() {
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }
    public void setProductPhoto(String newProductPhoto){this.productPhoto = newProductPhoto;}

    public String getProductPhoto(){
        return this.productPhoto;
    }



}

