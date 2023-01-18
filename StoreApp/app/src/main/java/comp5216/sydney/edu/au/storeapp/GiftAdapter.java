package comp5216.sydney.edu.au.storeapp;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import comp5216.sydney.edu.au.storeapp.dataHelpers.Gift;


interface OnClickGift {
	void onClickGift(Gift gift);
}

public class GiftAdapter extends ArrayAdapter<Gift> {

	private final Activity context;
	private final List<Gift> gifts;
	private final OnClickGift onClickGift;

	static class ViewHolder {
		public TextView receiverName;
		public TextView senderName;
		public LinearLayout buttonView;
	}

	public GiftAdapter(Activity context, List<Gift> gifts, OnClickGift onClickGift) {
		super(context, 0, gifts);
		this.onClickGift = onClickGift;
		this.context = context;
		this.gifts = gifts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		View rowView = convertView;

		if (rowView == null) {
			rowView =
				LayoutInflater.from(context).inflate(R.layout.gift_in_giftlist, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.receiverName = rowView.findViewById(R.id.receiverName);
			viewHolder.senderName = rowView.findViewById(R.id.senderName);
			viewHolder.buttonView = rowView.findViewById(R.id.openGiftButton);

			Gift gift = gifts.get(position);

			viewHolder.buttonView.setOnClickListener(view -> {
				onClickGift.onClickGift(gift);
			});

			rowView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) rowView.getTag();
		}

		Gift gift = gifts.get(position);
		viewHolder.receiverName.setText(gift.getReceiver());
		viewHolder.senderName.setText(gift.getSender());

		return rowView;
	}

	@Override
	public void add(Gift gift) {
		gifts.add(gift);
		notifyDataSetChanged();
	}
}
