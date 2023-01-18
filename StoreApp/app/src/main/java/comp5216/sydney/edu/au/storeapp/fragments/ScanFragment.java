package comp5216.sydney.edu.au.storeapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firestore.v1.WriteResult;
import com.google.zxing.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import comp5216.sydney.edu.au.storeapp.GiftSummaryActivity;
import comp5216.sydney.edu.au.storeapp.Home;
import comp5216.sydney.edu.au.storeapp.R;
import comp5216.sydney.edu.au.storeapp.SelectStore;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;


public class ScanFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	private UserDB userDB;
	private String mParam2;

	private static final int CAMERA_REQUEST_CODE = 101;

	private FirebaseFirestore db;
	private CodeScanner mCodeScanner;

	public ScanFragment() {
		// Required empty public constructor
	}

	public static ScanFragment newInstance(UserDB user, String param2) {
		ScanFragment fragment = new ScanFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, user);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			userDB = getArguments().getParcelable(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.scanner_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		db = FirebaseFirestore.getInstance();
		// Get vibrator instance
		Vibrator vibrator =
			(Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

		requestPermissionForCamera();
		CodeScannerView scannerView = requireView().findViewById(R.id.qr_scanner_view);
		mCodeScanner = new CodeScanner(requireContext(), scannerView);
		mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
		mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
		mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);
		mCodeScanner.setAutoFocusEnabled(true);
		mCodeScanner.setFlashEnabled(false);
		mCodeScanner.setDecodeCallback(new DecodeCallback() {
			@Override
			public void onDecoded(@NonNull final Result result) {
				requireActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {

							// Vibrate for 200 milliseconds
							vibrator.vibrate(200);

							Toast.makeText(getContext(), "Checking Permissions",
									Toast.LENGTH_SHORT)
								.show();

							// Obtain the gift from the QR code
							DocumentReference giftReference =
								db.collection("gifts").document(result.getText());

							// Obtain the ID for the store, where the gift can be used
							giftReference.get()
								.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
									@Override
									public void onSuccess(DocumentSnapshot documentSnapshot) {
										DocumentReference store =
											documentSnapshot.getDocumentReference("store");
										boolean is_used = Boolean.TRUE.equals(
											documentSnapshot.getBoolean("is_used"));

										// Check if the gift does not belong the the store
										if (store == null ||
											!userDB.getCurrentStoreId().equals(store.getId())) {
											Toast.makeText(getContext(),
												"This gift does not belong to this store",
												Toast.LENGTH_SHORT).show();
										}
										// Check if the gift has been used
										else if (is_used) {
											Toast.makeText(getContext(),
												"This gift has already been used",
												Toast.LENGTH_SHORT).show();
											// Handle the transaction and consume the gift
										} else {
											handleQr(giftReference, result.getText());
										}
									}
								});
						} catch (Exception e) {
							Toast.makeText(getContext(), "This gift does not belong to this store",
								Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
		mCodeScanner.setErrorCallback(new ErrorCallback() {
			@Override
			public void onError(@NonNull Throwable thrown) {
				Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
			}
		});
		scannerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCodeScanner.startPreview();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mCodeScanner.startPreview();
	}

	@Override
	public void onPause() {
		mCodeScanner.releaseResources();
		super.onPause();
	}


	public void handleQr(DocumentReference giftReference, String id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
		builder.setTitle("QR Scanning")
			.setMessage("Do you want to complete the transaction")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					giftReference.update("is_used", true);
					Toast.makeText(getContext(), "Consumed the Gift", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getContext(), GiftSummaryActivity.class);
					intent.putExtra("giftId", id);
					intent.putExtra("is_used", true);
					startActivity(intent);
				}
			})
			// User cancelled the dialog and nothing happens
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					// Release the former QR scan image
					mCodeScanner.startPreview();
				}
			});
		builder.create().show();
	}


	public void requestPermissionForCamera() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
			Manifest.permission.CAMERA)) {
			Toast.makeText(requireContext(), "Camera permission needed. " +
					"Please allow in App Settings for additional functionality.",
					Toast.LENGTH_LONG)
				.show();
		} else {
			ActivityCompat.requestPermissions(requireActivity(),
				new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
		}
	}
}
