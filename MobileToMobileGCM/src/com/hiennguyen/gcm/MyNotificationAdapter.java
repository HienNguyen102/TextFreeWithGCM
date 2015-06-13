package com.hiennguyen.gcm;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyNotificationAdapter extends ArrayAdapter<MyNotification> {
	Context context;
	ArrayList<MyNotification> notifications;

	public MyNotificationAdapter(Context context, ArrayList<MyNotification> objects) {
		super(context, R.layout.list_noti_item, objects);
		this.context = context;
		this.notifications = objects;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.list_noti_item, parent, false);
		TextView tvName = (TextView) row.findViewById(R.id.noti_item_name);
		TextView tvReply = (TextView) row.findViewById(R.id.noti_item_reply);
		ImageView ivNoti=(ImageView)row.findViewById(R.id.noti_item_image);
		MyNotification dataRow=notifications.get(position);
		tvName.setText(dataRow.getNameSender());
		tvReply.setText(GcmIntentService.handleImageName(dataRow.getReply()));
		if (dataRow.getChatType().equals("Group")) {
			ivNoti.setImageResource(R.drawable.ic_action_group);
		}
		return row;
	}
}
