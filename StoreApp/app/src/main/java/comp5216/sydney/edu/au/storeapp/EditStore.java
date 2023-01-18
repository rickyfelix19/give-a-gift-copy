package comp5216.sydney.edu.au.storeapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Store;

public class EditStore extends FragmentActivity {
	private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
	MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
	private static final String TAG = "EditStore";
	private FirebaseAuth mAuth;

	private Uri imageUri;
	private EditText storeNameText;
	private EditText storeCategoryText;
	private String storeId;

	private boolean uploading = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_store);
		mAuth = FirebaseAuth.getInstance();
		Intent intent = getIntent();
		storeId = intent.getStringExtra("storeId");
		String name = intent.getStringExtra("name");
		String category = intent.getStringExtra("category");
		imageUri = null;

		storeNameText = findViewById(R.id.storeName);
		storeCategoryText = findViewById(R.id.storeCategory);

		storeNameText.setText(name);
		storeCategoryText.setText(category);

		((TextView) findViewById(R.id.createNewStoreTitle)).setText("Edit store");
		((MaterialButton) findViewById(R.id.cancelButtonEditStore)).setVisibility(View.VISIBLE);
		((MaterialButton) findViewById(R.id.saveNewStoreButton)).setText("SAVE");
	}

	public void onSelectImageClick(View view) {
		if (!marshmallowPermission.checkPermissionForReadfiles()) {
			marshmallowPermission.requestPermissionForReadfiles();
		} else {
			launchGetImages();
		}
	}

	public void onClickCancel(View view) {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		finish();
	}

	public void onNewStoreClick(View view) {
		if (storeNameText.getText().toString().equals("") ||
			storeCategoryText.getText().toString().equals("")) {
			Toast.makeText(this, "A store name and category", Toast.LENGTH_SHORT).show();
			return;
		}
		// we have all the info needed
		// create new store and save
		if (uploading) {
			Toast.makeText(this, "We are already creating your store, please wait",
				Toast.LENGTH_SHORT).show();
			return;
		}
		updateStoreInDB();
	}

	public void updateStoreInDB() {
		uploading = true;
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			Toast.makeText(this, "You need to be signed in to do this action", Toast.LENGTH_SHORT)
				.show();
			Intent intent = new Intent(EditStore.this, MainActivity.class);
			startActivity(intent);
			uploading = false;
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Updating the store details").show();
		Store store =
			new Store(storeNameText.getText().toString(), storeCategoryText.getText().toString(),
				null, null);
		Map<String, Object> storeForDB = store.getStoreForDB();
		FirebaseFirestore.getInstance()
			.collection("stores")
			.document(storeId)
			.set(storeForDB, SetOptions.merge())
			.addOnCompleteListener(t -> {
				if (!t.isSuccessful()) {
					Toast.makeText(this, "Error updating your store information",
							Toast.LENGTH_LONG)
						.show();
					dialog.dismiss();
					return;
				}
				if (imageUri != null) {
					submitImage(imageUri, storeId, dialog, store);
					imageUri = null;
				} else {
					dialog.dismiss();
					Intent returnIntent = new Intent();
					returnIntent.putExtra("name", store.getName());
					returnIntent.putExtra("category", store.getCategory());
					setResult(RESULT_OK, returnIntent);
					finish();
				}
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w(TAG, "Error adding document", e);
				uploading = false;
				dialog.dismiss();
			});
	}

	public void submitImage(Uri uri, String id, AlertDialog dialog, Store store) {
		ContentResolver resolver = getApplicationContext().getContentResolver();
		try {
			InputStream stream = resolver.openInputStream(uri);
			StorageReference storageRef = FirebaseStorage.getInstance().getReference();
			StorageReference cloudFileRef =
				storageRef.child("storeImages/" + id + new Date().getTime() + ".png");
			UploadTask uploadTask = cloudFileRef.putStream(stream);
			uploadTask.addOnFailureListener(exception -> {
				Log.e(TAG, "Error submitting the file for ID: " + id);
				Toast.makeText(this, "There was an error saving your image", Toast.LENGTH_LONG)
					.show();
				uploading = false;
				dialog.dismiss();
			}).addOnSuccessListener(taskSnapshot -> {
				Log.d(TAG,
					"Remote file: " + Objects.requireNonNull(taskSnapshot.getMetadata()).getPath());
				Map<String, String> storeForDB = new HashMap<>();
				storeForDB.put("imagePath", taskSnapshot.getMetadata().getPath());
				FirebaseFirestore.getInstance()
					.collection("stores")
					.document(id)
					.set(storeForDB, SetOptions.merge())
					.addOnCompleteListener((Task<Void> t) -> {
						Toast.makeText(this, "Store updated", Toast.LENGTH_SHORT).show();
						uploading = false;
						dialog.dismiss();
						Intent returnIntent = new Intent();
						returnIntent.putExtra("name", store.getName());
						returnIntent.putExtra("category", store.getCategory());
						returnIntent.putExtra("image", taskSnapshot.getMetadata().getPath());
						setResult(RESULT_OK, returnIntent);
						finish();
					})
					.addOnFailureListener(e -> {
						Toast.makeText(this, "Error updating the image of your store",
							Toast.LENGTH_SHORT).show();
						uploading = false;
						dialog.dismiss();
					});
				;
			});
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "Error updating the image of your store", Toast.LENGTH_SHORT)
				.show();
			dialog.dismiss();
			uploading = false;
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
