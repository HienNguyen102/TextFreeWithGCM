package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FindFriendAdapter extends ArrayAdapter<UserData> {
	Controller ctlCheckNetState;
	Context context;
	ArrayList<UserData> users;
	String idOfLoggedUser, idOfSelectedFriend, nameOfFriendNeedToAdd;

	public FindFriendAdapter(Context context, ArrayList<UserData> objects,
			String pIdOfLoggedUser, ListenerUpdateStatus pMl) {
		super(context, R.layout.list_find_result_item, objects);
		ctlCheckNetState = (Controller) context.getApplicationContext();
		this.context = context;
		this.users = objects;
		this.idOfLoggedUser = pIdOfLoggedUser;
		this.ml = pMl;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.list_find_result_item, parent,
				false);
		TextView tvNameUser = (TextView) row.findViewById(R.id.tvNameUser);
		TextView tvStatus = (TextView) row.findViewById(R.id.tvStatus);
		ImageButton ibAddFriend = (ImageButton) row
				.findViewById(R.id.ibAddFriend);
		final UserData dataRow = users.get(position);
		tvNameUser.setText(dataRow.getName());
		if (dataRow.isFriend) {
			tvStatus.setText(context.getResources().getString(
					R.string.friend_tv));
			ibAddFriend.setVisibility(View.GONE);
		} else {
			tvStatus.setText(context.getResources().getString(
					R.string.add_friend));
			ibAddFriend.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!ctlCheckNetState.isConnectingToInternet()) {
						ctlCheckNetState.showAlertEnableWifi(context);
						return;
					} else {
						idOfSelectedFriend = dataRow.getID() + "";
						nameOfFriendNeedToAdd = dataRow.getName();
						new AsyncTaskAddFriend().execute();
					}
				}
			});
		}
		return row;
	}

	public class AsyncTaskAddFriend extends AsyncTask<Void, Void, String> {
		ProgressDialog pDialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(context.getResources().getString(
					R.string.wait_sending));
			pDialog.show();
		}

		@Override
		protected String doInBackground(Void... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id_of_selected_friend",
					idOfSelectedFriend));
			params.add(new BasicNameValuePair("id_of_logged_user",
					idOfLoggedUser));
			JSONParser jsonPaster = new JSONParser();
			try {
				JSONObject json = jsonPaster.performPostCall(
						FindFriendFragment.ADD_UNFRIEND_URL, params, true);
				if (json.getInt("success") == 1) {
					return "succ";
				} else {
					return "fail";
				}
			} catch (Exception e) {
				Log.e("AsyncTaskAddFriend-JSONException fail", e.toString());
				return "fail";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("succ")) {
				ml.updateStatus(nameOfFriendNeedToAdd);
			} else {
				Toast.makeText(
						context,
						context.getResources().getString(
								R.string.change_pass_err), Toast.LENGTH_LONG)
						.show();
			}
			pDialog.dismiss();
		}

	}

	ListenerUpdateStatus ml;

	public interface ListenerUpdateStatus {
		public void updateStatus(String friendName);
	}
}
