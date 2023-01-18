package comp5216.sydney.edu.au.storeapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import comp5216.sydney.edu.au.storeapp.NewProduct;
import comp5216.sydney.edu.au.storeapp.ProductAdapter;
import comp5216.sydney.edu.au.storeapp.R;
import comp5216.sydney.edu.au.storeapp.dataHelpers.Product;
import comp5216.sydney.edu.au.storeapp.dataHelpers.UserDB;


public class ProductsFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "user";
	private static final String ARG_PARAM2 = "param2";
	private UserDB userDB;
	private String mParam2;

	RecyclerView recyclerView;
	FirebaseFirestore mFirestore;
	ProductAdapter productAdapter;
	ArrayList<Product> products;

	public ProductsFragment() {
		// Required empty public constructor
	}

	public static ProductsFragment newInstance(UserDB user, String param2) {
		ProductsFragment fragment = new ProductsFragment();
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
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		recyclerView = requireView().findViewById(R.id.productListData);
		recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

		mFirestore = FirebaseFirestore.getInstance();

		products = new ArrayList<Product>();
		productAdapter = new ProductAdapter(requireActivity(), products);
		recyclerView.setAdapter(productAdapter);
		view.findViewById(R.id.addNewProductButton).setOnClickListener(this::onClickAddNewProduct);

		EventChangeListener();

		EditText searchEditText = requireView().findViewById(R.id.productSearchInput);
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				filter(s.toString());
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.product_list, container, false);
	}

	private void filter(String text) {
		ArrayList<Product> filteredList = new ArrayList<>();

		for (Product item : products) {
			if (item.getName().toLowerCase().contains(text.toLowerCase())) {
				filteredList.add(item);
			}
		}

		productAdapter.filterList(filteredList);
	}

	public void onClickAddNewProduct(View view) {
		Intent intent = new Intent(getContext(), NewProduct.class);
		intent.putExtra("user", userDB);
		mLaunchAddProduct.launch(intent);
	}

	ActivityResultLauncher<Intent> mLaunchAddProduct =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
			if (result.getResultCode() == -1) {
				EventChangeListener();
			}
		});

	private void EventChangeListener() {
		products = new ArrayList<Product>();
		productAdapter = new ProductAdapter(requireActivity(), products);
		recyclerView.setAdapter(productAdapter);

		Query pr = mFirestore.collection("stores")
			.document(userDB.getCurrentStoreId())
			.collection("products");

		pr.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
			@Override
			public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
				for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
					String documentId = dc.getDocument().getId();

					Product product = dc.getDocument().toObject(Product.class);
					product.setDocumentId(documentId);
					product.setStoreId(userDB.getCurrentStoreId());
					products.add(product);
				}

				productAdapter.notifyDataSetChanged();
			}
		});
	}
}