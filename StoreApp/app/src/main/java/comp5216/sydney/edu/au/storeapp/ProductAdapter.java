package comp5216.sydney.edu.au.storeapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
	Context context;
	ArrayList<Product> products;
	FirebaseFirestore db;

	public ProductAdapter(Context context, ArrayList<Product> products) {
		this.context = context;
		this.products = products;
		this.db = FirebaseFirestore.getInstance();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new ViewHolder(inflater.inflate(R.layout.product, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Product product = products.get(position);

		holder.productName.setText(product.getName());
		holder.productPrice.setText("$" + String.valueOf(product.getPrice()));

		if (product.getImagePath() != null) {
			StorageReference storageReference =
				FirebaseStorage.getInstance().getReference(product.getImagePath());
			// Load the image using Glide
			Glide.with(context).load(storageReference).into(holder.productImage);
		}

		holder.btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
				builder.setTitle("Delete a Product");
				builder.setMessage("Do you want to delete this product?");
				builder.setPositiveButton("Yes", (dialogInterface, i) -> {
					db.collection("stores")
						.document(product.getStoreId())
						.collection("products")
						.document(product.getDocumentId())
						.delete()
						.addOnCompleteListener(task -> {
							if (task.isSuccessful()) {
								FirebaseStorage.getInstance()
									.getReference(product.getImagePath())
									.delete();
								products.remove(holder.getAdapterPosition());
								notifyDataSetChanged();
							} else {
								Log.d("Error",
									"Error remove product: " + task.getException().getMessage());
							}
						});
				});
				builder.setNegativeButton("No", (dialogInterface, i) -> {
					dialogInterface.dismiss();
				});
				builder.show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return products.size();
	}

	public void filterList(ArrayList<Product> filteredList) {
		products = filteredList;
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView productName, productPrice;
		public ImageView productImage;
		public FloatingActionButton btn;

		public ViewHolder(View itemView) {
			super(itemView);
			productName = (TextView) itemView.findViewById(R.id.productName);
			productPrice = (TextView) itemView.findViewById(R.id.productPrice);
			productImage = (ImageView) itemView.findViewById(R.id.productImage);
			btn = (FloatingActionButton) itemView.findViewById(R.id.removeProduct);
		}
	}
}
