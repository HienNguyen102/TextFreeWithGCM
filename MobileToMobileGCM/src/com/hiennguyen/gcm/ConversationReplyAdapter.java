package com.hiennguyen.gcm;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ConversationReplyAdapter extends BaseAdapter{
	Context context;
	LayoutInflater inflater;
	ArrayList<ConversationReply> arrCR;
	
	public ConversationReplyAdapter(Context c, ArrayList<ConversationReply> pArrCR) {
		this.context = c;
		this.arrCR = pArrCR;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return arrCR.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
		ViewHolder vh;
		if (row==null) {
			row=inflater.inflate(R.layout.list_reply_item, parent,false);
			vh=new ViewHolder();
			vh.tvReply=(TextView)row.findViewById(R.id.tvReply);
			row.setTag(vh);
		} else {
			vh=(ViewHolder)row.getTag();
			row=convertView;
		}
		vh.tvReply.setText(arrCR.get(position).getUsername()+": "+arrCR.get(position).getReply());
		return row;
	}
	public static class ViewHolder {
		TextView tvReply;
	}
}
