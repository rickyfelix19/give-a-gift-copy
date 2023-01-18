package comp5216.sydney.edu.au.customerapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MarshmallowPermission {

	public static final int READFILES_PERMISSION_REQUEST_CODE = 4;
	public static final int LOCATION_PERMISSION_REQUEST_CODE = 5;
	Activity activity;

	public MarshmallowPermission(Activity activity) {
		this.activity = activity;
	}


	public boolean checkPermissionForReadfiles() {
		int result =
			ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
		return result == PackageManager.PERMISSION_GRANTED;
	}

	public void requestPermissionForReadfiles() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
			Manifest.permission.READ_EXTERNAL_STORAGE)) {
			Toast.makeText(activity,
				"Read files permission needed. Please allow in App Settings for additional " +
					"functionality.", Toast.LENGTH_LONG).show();
		} else {
			ActivityCompat.requestPermissions(activity,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				READFILES_PERMISSION_REQUEST_CODE);
		}
	}

	public boolean checkPermissionForLocation() {
		int result =
			ContextCompat.checkSelfPermission(activity,
				Manifest.permission.ACCESS_COARSE_LOCATION);
		return result == PackageManager.PERMISSION_GRANTED;
	}

	public void requestPermissionForLocation() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
			Manifest.permission.ACCESS_COARSE_LOCATION) ||
			ActivityCompat.shouldShowRequestPermissionRationale(activity,
				Manifest.permission.ACCESS_FINE_LOCATION)) {
			Toast.makeText(activity,
				"Location permissions needed. Please allow in App Settings for additional " +
					"functionality.", Toast.LENGTH_LONG).show();
		} else {
			ActivityCompat.requestPermissions(activity,
				new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
		}
	}
}