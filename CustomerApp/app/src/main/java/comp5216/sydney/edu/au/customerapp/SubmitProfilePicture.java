package comp5216.sydney.edu.au.customerapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SubmitProfilePicture extends FragmentActivity {
	private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
	MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
	private String userId;
	private Uri photoUri;
	private boolean uploading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_profile_picture);
		uploading = false;
		photoUri = null;
		userId = null;
		Intent data = getIntent();
		userId = data.getStringExtra("userId");
		if (userId == null) {
			Toast.makeText(this, "Did not get the user ID", Toast.LENGTH_LONG).show();
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED,
				returnIntent); // Set result code and bundle data for response
			finish(); // Close the activity, pass data to parent }
		}
	}

	public void onSelectImageClick(View view) {
		if (!marshmallowPermission.checkPermissionForReadfiles()) {
			marshmallowPermission.requestPermissionForReadfiles();
		} else {
			launchGetImages();
		}
	}

	public void launchGetImages() {
		// Create intent for picking a photo from the gallery
		Intent intent =
			new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		// Bring up gallery to select a photo
		startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_PHOTOS);
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
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

	public void onCancelSelectProfilePicture(View view) {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent); // Set result code and bundle data for response
		finish(); // Close the activity, pass data to parent }
	}

	public void onConfirmProfilePicture(View view) {
		if (userId == null || photoUri == null) {
			Toast.makeText(this, "Please select a picture", Toast.LENGTH_LONG).show();
			return;
		}
		if (uploading) {
			Toast.makeText(this, "We are already submitting your image, please wait",
				Toast.LENGTH_LONG).show();
			return;
		}
		uploading = true;
		AlertDialog dialog = new MaterialAlertDialogBuilder(this,
			com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered).setView(
			R.layout.spinner).setMessage("Updating your profile picture").show();
		Toast.makeText(this, "Uploading your profile picture", Toast.LENGTH_SHORT).show();
		ContentResolver resolver = getApplicationContext().getContentResolver();
		try {
			InputStream stream = resolver.openInputStream(photoUri);

			StorageReference storageRef = FirebaseStorage.getInstance().getReference();
			StorageReference cloudFileRef =
				storageRef.child("profilePictures/" + userId + new Date().getTime() + ".png");
			UploadTask uploadTask = cloudFileRef.putStream(stream);
			uploadTask.addOnFailureListener(exception -> {
				Toast.makeText(this, "There was an error saving your image", Toast.LENGTH_LONG)
					.show();
				dialog.dismiss();
				uploading = false;
			}).addOnSuccessListener(taskSnapshot -> {
				Map<String, Object> userForDb = new HashMap<>();
				userForDb.put("profileImage",
					Objects.requireNonNull(taskSnapshot.getMetadata()).getPath());
				FirebaseFirestore.getInstance()
					.collection("users")
					.document(userId)
					.set(userForDb, SetOptions.merge())
					.addOnCompleteListener((Task<Void> t) -> {
						Toast.makeText(this, "Profile picture saved", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
						uploading = false;
						Intent returnIntent = new Intent();
						returnIntent.putExtra("newPath", taskSnapshot.getMetadata().getPath());
						setResult(RESULT_OK,
							returnIntent); // Set result code and bundle data for response
						finish(); // Close the activity, pass data to parent }
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
			Toast.makeText(this, "Error updating the image of your store", Toast.LENGTH_SHORT)
				.show();
			dialog.dismiss();
			uploading = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
			if (resultCode == RESULT_OK) {
				photoUri = data.getData();
				Toast.makeText(this, "Picture ready", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
