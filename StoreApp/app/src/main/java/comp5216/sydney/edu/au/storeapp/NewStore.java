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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Store;

public class NewStore extends Activity {
	private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
	MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
	private static final String TAG = "NewStore";
	private FirebaseAuth mAuth;

	private Uri imageUri;
	private EditText storeNameText;
	private EditText storeCategoryText;

	private boolean uploading = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_store);
		mAuth = FirebaseAuth.getInstance();
		storeNameText = findViewById(R.id.storeName);
		storeCategoryText = findViewById(R.id.storeCategory);
	}

	public void onSelectImageClick(View view) {
		if (!marshmallowPermission.checkPermissionForReadfiles()) {
			marshmallowPermission.requestPermissionForReadfiles();
		} else {
			launchGetImages();
		}
	}

	public void onNewStoreClick(View view) {
		if (storeNameText.getText().toString().equals("") ||
			storeCategoryText.getText().toString().equals("") || imageUri == null) {
			Toast.makeText(this, "A store name, category and image are required",
				Toast.LENGTH_SHORT).show();
			return;
		}
		// we have all the info needed
		// create new store and save
		if (uploading) {
			Toast.makeText(this, "We are already creating your store, please wait",
				Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Creating your new store").show();
		createStoreInDB(dialog);
	}

	public void createStoreInDB(AlertDialog dialog) {
		uploading = true;
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			Toast.makeText(this, "You need to be signed in to do this action", Toast.LENGTH_SHORT)
				.show();
			dialog.dismiss();
			Intent intent = new Intent(NewStore.this, MainActivity.class);
			startActivity(intent);
			uploading = false;
			return;
		}
		Toast.makeText(this, "Creating your new Store", Toast.LENGTH_LONG).show();
		Store store =
			new Store(storeNameText.getText().toString(), storeCategoryText.getText().toString(),
				null, null);
		Map<String, Object> storeForDB = store.getStoreForDB();
		FirebaseFirestore.getInstance()
			.collection("stores")
			.add(storeForDB)
			.addOnCompleteListener((Task<DocumentReference> t) -> {
				Log.d(TAG, "DocumentSnapshot added with ID: " + t.getResult().getId());
				Toast.makeText(this, "Saving your Store image", Toast.LENGTH_LONG).show();
				assignUserToStore(t.getResult(), currentUser);
				submitImage(imageUri, t.getResult().getId(), dialog);
				imageUri = null;
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w(TAG, "Error adding document", e);
				dialog.dismiss();
				uploading = false;
			});
	}

	public void assignUserToStore(DocumentReference storeReference, FirebaseUser currentUser) {
		Map<String, Object> userStore = new HashMap<>();
		DocumentReference userReference =
			FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());

		userStore.put("user", userReference);
		userStore.put("store", storeReference);
		userStore.put("type", "admin");
		FirebaseFirestore.getInstance()
			.collection("userStores")
			.add(userStore)
			.addOnCompleteListener((Task<DocumentReference> t) -> {
				Log.d(TAG, "DocumentSnapshot<userStore> added with ID: " + t.getResult().getId());
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w(TAG, "Error adding document userStore", e);
			});
		Map<String, Object> userCurrentStore = new HashMap<>();
		userCurrentStore.put("currentStore", storeReference);
		FirebaseFirestore.getInstance()
			.collection("users")
			.document(currentUser.getUid())
			.set(userCurrentStore, SetOptions.merge())
			.addOnCompleteListener((Task<Void> t) -> {
				Log.d(TAG, "DocumentSnapshot<users> updated with ID: " + currentUser.getUid());
			})
			.addOnFailureListener((@NonNull Exception e) -> {
				Log.w(TAG, "Error adding document userStore", e);
			});
	}

	public void submitImage(Uri uri, String id, AlertDialog dialog) {
		ContentResolver resolver = getApplicationContext().getContentResolver();
		try {
			InputStream stream = resolver.openInputStream(uri);

			StorageReference storageRef = FirebaseStorage.getInstance().getReference();
			StorageReference cloudFileRef = storageRef.child("storeImages/" + id + ".png");
			UploadTask uploadTask = cloudFileRef.putStream(stream);
			uploadTask.addOnFailureListener(exception -> {
				Log.e(TAG, "Error submitting the file for ID: " + id);
				dialog.dismiss();
				Toast.makeText(this, "There was an error saving your image", Toast.LENGTH_LONG)
					.show();
				uploading = false;
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
						Toast.makeText(this, "Store created", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						uploading = false;
						// got to add address
						Intent intent = new Intent(NewStore.this, AddAddress.class);
						intent.putExtra("storeId", id);
						startActivity(intent);
					})
					.addOnFailureListener(e -> {
						Toast.makeText(this, "Error updating the image of your store",
							Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						uploading = false;
					});
				;
			});
		} catch (IOException e) {
			e.printStackTrace();
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
