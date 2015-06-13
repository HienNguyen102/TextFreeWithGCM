package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListUserAdapter extends BaseAdapter {
	Context context;
	LayoutInflater inflater;
	ArrayList<UserData> arrUser;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();

	public void setarrUser(ArrayList<UserData> pArrUser) {
		this.arrUser = pArrUser;
	}

	public ListUserAdapter(Context c, ArrayList<UserData> pArrUser) {
		this.context = c;
		this.arrUser = pArrUser;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return arrUser.size();
	}

	@Override
	public Object getItem(int pos) {
		return arrUser.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
	
	@SuppressLint("UseSparseArrays")
	public void clearSelection() {
		mSelection = new HashMap<Integer, Boolean>();
		notifyDataSetChanged();
	}

	public void setNewSelection(int position, boolean value) {
		mSelection.put(position, value);
		notifyDataSetChanged();
	}

	public void removeSelection(int position) {
		mSelection.remove(position);
		notifyDataSetChanged();
	}

	public boolean isPositionChecked(int position) {
		Boolean result = mSelection.get(position);
		return result == null ? false : result;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder vh;
		if (row == null) {
			row = inflater.inflate(R.layout.grid_item, parent, false);
			vh = new ViewHolder();
			vh.pname = (TextView) row.findViewById(R.id.grid_item_label);
			row.setTag(vh);
		} else {
			vh = (ViewHolder) row.getTag();
			row = convertView;
		}
		String nameOfUser = arrUser.get(position).getName();
		vh.pname.setText(nameOfUser);
		row.setBackgroundColor(context.getResources().getColor(
				android.R.color.background_light)); // default color
		if (mSelection.get(position) != null) {
			row.setBackgroundColor(context.getResources().getColor(
					android.R.color.holo_blue_light));
		}
		return row;
	}

	public static class ViewHolder {
		TextView pname;
	}

}
