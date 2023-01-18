package comp5216.sydney.edu.au.storeapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Store;


interface OnClickStore {
	void onClickStore(Store store);
}

public class StoresAdapter extends ArrayAdapter<Store> {
	private final Activity context;
	private final List<Store> stores;
	private final OnClickStore onClickStore;

	static class ViewHolder {
		public TextView nameView;
		public TextView categoryView;
		public ImageView imageView;
		public MaterialButton button;
	}

	public StoresAdapter(Activity context, List<Store> stores, OnClickStore onClickStore) {
		super(context, 0, stores);
		this.onClickStore = onClickStore;
		this.context = context;
		this.stores = stores;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		View rowView = convertView;

		if (rowView == null) {
			rowView =
				LayoutInflater.from(context).inflate(R.layout.store_in_store_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.nameView = rowView.findViewById(R.id.storeNameAdapter);
			viewHolder.categoryView = rowView.findViewById(R.id.storeCategoryAdapter);
			viewHolder.imageView = rowView.findViewById(R.id.storeImage);
			viewHolder.button = rowView.findViewById(R.id.selectStoreButton);
			Store store = stores.get(position);
			viewHolder.button.setOnClickListener(view -> {
				onClickStore.onClickStore(store);
			});
			rowView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) rowView.getTag();
		}
		Store store = stores.get(position);
		viewHolder.nameView.setText(store.getName());
		viewHolder.categoryView.setText(store.getCategory());
		StorageReference storageReference =
			FirebaseStorage.getInstance().getReference(store.getImageUri());
		// Load the image using Glide
		Glide.with(context).load(storageReference).into(viewHolder.imageView);
		return rowView;
	}

	@Override
	public void add(Store store) {
		stores.add(store);
		notifyDataSetChanged();
	}

}
