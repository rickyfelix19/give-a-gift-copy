package comp5216.sydney.edu.au.customerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import comp5216.sydney.edu.au.customerapp.R;
import comp5216.sydney.edu.au.customerapp.dataHelpers.UserDB;

public class WalletFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	// TODO: Rename and change types of parameters
	private UserDB mUserDB;
	private String mParam2;
	public WalletFragment() {
		// Required empty public constructor
	}

	public static WalletFragment newInstance(UserDB userDB, String param2) {
		WalletFragment fragment = new WalletFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, userDB);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mUserDB = getArguments().getParcelable(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.wallet_fragment, container, false);
	}
}