package comp5216.sydney.edu.au.customerapp;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;

import comp5216.sydney.edu.au.customerapp.dataHelpers.Friend;


public class FriendRequestsAdapter extends ArrayAdapter<Friend> {
	interface IOnClickAccept {
		void onClickAccept(Friend friend);
	}

	interface IOnClickDeny {
		void onClickDeny(Friend friend);
	}

	private final Activity context;
	private IOnClickAccept iOnClickAccept;
	private IOnClickDeny iOnClickDeny;

	static class ViewHolder {
		public TextView nameView;
		public TextView birthdate;
		public ImageView imageView;
		public ImageView acceptView;
		public ImageView denyView;
	}

	public FriendRequestsAdapter(Activity context, IOnClickAccept ia, IOnClickDeny id) {
		super(context, 0);
		this.context = context;
		this.iOnClickAccept = ia;
		this.iOnClickDeny = id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		View rowView = convertView;

		if (rowView == null) {
			rowView = LayoutInflater.from(context)
				.inflate(R.layout.friend_request_in_list, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.nameView = rowView.findViewById(R.id.friendRequestNameAdapter);
			viewHolder.birthdate = rowView.findViewById(R.id.friendRequestBirthdate);
			viewHolder.imageView = rowView.findViewById(R.id.friendRequestProfilePicture);
			viewHolder.acceptView = rowView.findViewById(R.id.friendRequestAccept);
			viewHolder.denyView = rowView.findViewById(R.id.friendRequestDeny);
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

		viewHolder.acceptView.setOnClickListener(v -> {
			Friend friendInCB = getItem(position);
			iOnClickAccept.onClickAccept(friendInCB);
		});
		viewHolder.denyView.setOnClickListener(v -> {
			Friend friendInCB = getItem(position);
			iOnClickDeny.onClickDeny(friendInCB);
		});

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
