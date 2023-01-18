package comp5216.sydney.edu.au.customerapp.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.customerapp.R;
import comp5216.sydney.edu.au.customerapp.dataHelpers.Friend;

public class FriendsAdapter extends ArrayAdapter<Friend> {
	private final Activity context;

	static class ViewHolder {
		public TextView nameView;
		public TextView birthdate;
		public TextView isPending;
		public ImageView imageView;
	}

	public FriendsAdapter(Activity context) {
		super(context, 0);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		View rowView = convertView;

		if (rowView == null) {
			rowView = LayoutInflater.from(context).inflate(R.layout.friend_in_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.nameView = rowView.findViewById(R.id.friendNameAdapter);
			viewHolder.birthdate = rowView.findViewById(R.id.friendBirthdate);
			viewHolder.isPending = rowView.findViewById(R.id.isPendingText);
			viewHolder.imageView = rowView.findViewById(R.id.friendProfilePicture);
			rowView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) rowView.getTag();
		}

		Friend friend = getItem(position);
		viewHolder.nameView.setText(
			context.getString(R.string.friend_name_string, friend.getName(),
				friend.getLastname()));
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
		viewHolder.birthdate.setText(dateFormatter.format(friend.getBirthdate()));
		viewHolder.isPending.setVisibility(friend.getPending() ? View.VISIBLE : View.INVISIBLE);
		if (friend.getProfileImage() != null) {
			try {
				StorageReference storageReference =
					FirebaseStorage.getInstance().getReference(friend.getProfileImage());
				// Load the image using Glide
				Glide.with(context).load(storageReference).into(viewHolder.imageView);
			} catch (Exception e) {
				Drawable defaultImage =
					context.getResources().getDrawable(R.drawable.ic_person_inactivegift);
				viewHolder.imageView.setImageDrawable(defaultImage);
			}
		}
		return rowView;
	}

}
