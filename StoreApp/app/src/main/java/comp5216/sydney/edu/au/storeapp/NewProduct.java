package comp5216.sydney.edu.au.storeapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;

public class NewProduct extends FragmentActivity {
	private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
	MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
	private static final String TAG = "NewStore";
	private FirebaseAuth mAuth;

	private Uri imageUri;
	private EditText productNameText;
	private EditText productPriceText;
	private UserDB mUser;

	private boolean uploading = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_product);
		mAuth = FirebaseAuth.getInstance();
		productNameText = findViewById(R.id.productName);
		productPriceText = findViewById(R.id.productPrice);
		Intent data = getIntent();
		mUser = data.getParcelableExtra("user");
	}

	public void onSelectImageClick(View view) {
		if (!marshmallowPermission.checkPermissionForReadfiles()) {
			marshmallowPermission.requestPermissionForReadfiles();
		} else {
			launchGetImages();
		}
	}

	public void onSaveClick(View view) {
		if (productNameText.getText().toString().equals("") ||
			productPriceText.getText().toString().equals("") || imageUri == null) {
			Toast.makeText(this, "A product name, price and image are required",
					Toast.LENGTH_SHORT)
				.show();
			return;
		}
		if (uploading) {
			Toast.makeText(this, "We are already creating your store, please wait",
				Toast.LENGTH_SHORT).show();
			return;
		}
		createProductInDb();
	}

	public void onCancelClick(View view) {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		finish();
	}

	public void createProductInDb() {
		uploading = true;
		if (mUser == null || !Objects.equals(mUser.getCurrentStoreType(), "admin") ||
			mUser.getCurrentStoreId() == null) {
			Toast.makeText(this, "You need to be signed in to do this action", Toast.LENGTH_SHORT)
				.show();
			Intent intent = new Intent(NewProduct.this, MainActivity.class);
			uploading = false;
			startActivity(intent);
			return;
		}
		double price = Double.parseDouble(productPriceText.getText().toString());
		if (price <= 0) {
			Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
			uploading = false;
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Saving your new product").show();
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		Map<String, Object> productForDB = new HashMap<>();
		productForDB.put("name", productNameText.getText().toString());
		productForDB.put("price", price);
		FirebaseFirestore.getInstance()
			.collection("stores")
			.document(mUser.getCurrentStoreId())
			.collection("products")
			.add(productForDB)
			.addOnCompleteListener((Task<DocumentReference> t) -> {
				Log.d(TAG, "DocumentSnapshot added with ID: " + t.getResult().getId());
				submitImage(imageUri, t.getResult().getId(), dialog);
				imageUri = null;
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w(TAG, "Error adding document", e);
				Toast.makeText(this, "There was an error creating your product", Toast.LENGTH_LONG)
					.show();
				dialog.dismiss();
				uploading = false;
			});
	}

	public void submitImage(Uri uri, String id, AlertDialog dialog) {
		ContentResolver resolver = getApplicationContext().getContentResolver();
		try {
			InputStream stream = resolver.openInputStream(uri);
			StorageReference storageRef = FirebaseStorage.getInstance().getReference();
			StorageReference cloudFileRef = storageRef.child("productImages/" + id + ".png");
			UploadTask uploadTask = cloudFileRef.putStream(stream);
			uploadTask.addOnFailureListener(exception -> {
				Log.e(TAG, "Error submitting the file for ID: " + id);
				Toast.makeText(this, "There was an error saving your image", Toast.LENGTH_LONG)
					.show();
				dialog.dismiss();
				uploading = false;
			}).addOnSuccessListener(taskSnapshot -> {
				Log.d(TAG,
					"Remote file: " + Objects.requireNonNull(taskSnapshot.getMetadata()).getPath());
				Map<String, String> productForDB = new HashMap<>();
				productForDB.put("imagePath", taskSnapshot.getMetadata().getPath());
				FirebaseFirestore.getInstance()
					.collection("stores")
					.document(mUser.getCurrentStoreId())
					.collection("products")
					.document(id)
					.set(productForDB, SetOptions.merge())
					.addOnCompleteListener((Task<Void> t) -> {
						dialog.dismiss();
						uploading = false;
						Intent returnIntent = new Intent();
						setResult(RESULT_OK, returnIntent);
						finish();
					})
					.addOnFailureListener(e -> {
						Toast.makeText(this, "Error saving the image for your product",
							Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						uploading = false;
					});
				;
			});
		} catch (IOException e) {
			e.printStackTrace();
			uploading = false;
			dialog.dismiss();
		}
	}

	public void launchGetImages() {
		// Create intent for picking a photo from the gallery
		Intent intent =
			new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		// Bring up gallery to select a photo
		startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_PHOTOS);
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions,
	                                       int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int result : grantResults) {
			if (result != 0) {
				Toast.makeText(this, "Permissions not granted, so we cannot continue",
					Toast.LENGTH_LONG).show();
				return;
			}
		}
		launchGetImages();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ImageView ivPreview = findViewById(R.id.imagePreview);
		ivPreview.setVisibility(View.GONE);

		if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
			if (resultCode == RESULT_OK) {
				Uri photoUri = data.getData();
				// Do something with the photo based on Uri
				Bitmap selectedImage;
				try {
					selectedImage =
						MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);

					// Load the selected image into a preview
					ivPreview.setImageBitmap(selectedImage);
					ivPreview.setVisibility(View.VISIBLE);
					imageUri = photoUri;
				} catch (FileNotFoundException e) {
					Toast.makeText(this, "Image not found", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(this, "Could not load the image", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}
	}
}
