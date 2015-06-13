package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MyNotificationFragment extends Fragment {
	ListView lvNotification;
	MyNotificationAdapter notiAdapter;
	ArrayList<MyNotification> arrNoti = new ArrayList<MyNotification>();
	String serverURL;
	UserData senderData;
	public static final String GROUP_ID = "GROUP_ID";
	public static final String GROUP_NAME = "GROUP_NAME";
	SessionManager sm;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("MyNotificationFragment", "onCreate");
		serverURL = Config.SERVER_URL + "get_conversation_of_one_user.php";
		sm=new SessionManager(getActivity());
		senderData = sm.getUserDetails();
		// LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
		// (mReceiver_LoadNotiWhileSendMessage),
		// new IntentFilter(ShowMessage.IF_SHOW_MESSAGE));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				(mReceiver_LoadNotiFromService),
				new IntentFilter(GcmIntentService.INTENT_FILTER));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("MyNotificationFragment", "onCreateView");
		View view = inflater.inflate(R.layout.fragment_notification, container,
				false);
		lvNotification = (ListView) view.findViewById(R.id.lvNotification);
		lvNotification
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent i = new Intent(getActivity(), ShowMessage.class);
						MyNotification notiClicked = notiAdapter
								.getItem(position);
						String chatType = notiClicked.getChatType();
						i.putExtra(ListUserFragment.CHAT_TYPE, chatType);
						if (chatType.equals("Group")) {
							i.putExtra(GROUP_ID, notiClicked.getGroupId());
							i.putExtra(GROUP_NAME, notiClicked.getNameSender());
							i.putExtra(ListUserFragment.LIST_RECEIVER_DATA,
									notiClicked.getReceiver());
						} else {
							i.putExtra(ListUserFragment.RECEIVER_DATA,
									notiClicked.getReceiver().get(0));
						}
						startActivity(i);
					}
				});
		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.e("NotificationFragment", "onDetach");
		// LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
		// mReceiver_LoadNotiWhileSendMessage);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mReceiver_LoadNotiFromService);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e("NotificationFragment", "onResume");
		new AsyncTaskGetAllNoti().execute(serverURL, senderData.getID() + "");
	}

	// BroadcastReceiver mReceiver_LoadNotiWhileSendMessage = new
	// BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// getNotification(intent.getStringExtra("SenderId"));
	// }
	// };
	BroadcastReceiver mReceiver_LoadNotiFromService = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AsyncTaskGetAllNoti().execute(serverURL, senderData.getID()
					+ "");
		}
	};

	public class AsyncTaskGetAllNoti extends
			AsyncTask<String, Void, JSONObject> {

		protected JSONObject doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", args[1]));
			JSONParser jsonPaster = new JSONParser();
			JSONObject jsonDataOfAllNoti = null;
			try {
				jsonDataOfAllNoti = jsonPaster.performPostCall(args[0], params,
						true);
				return jsonDataOfAllNoti;
			} catch (Exception e) {
				Log.e("AsyncTaskGetAllNoti doInBackground", e.toString());
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {

				arrNoti = parseJson(result);
				notiAdapter = new MyNotificationAdapter(getActivity(), arrNoti);
				lvNotification.setAdapter(notiAdapter);
				notiAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getActivity(),
						getResources().getString(R.string.change_pass_err),
						Toast.LENGTH_LONG).show();
			}

		}

		public ArrayList<MyNotification> parseJson(JSONObject pResult) {
			ArrayList<MyNotification> arrNotiParsed = new ArrayList<MyNotification>();
			try {
				JSONArray arrAllNotiInJson = pResult.optJSONArray("NotiOfUser");
				for (int i = 0; i < arrAllNotiInJson.length(); i++) {
					JSONObject objNoti = arrAllNotiInJson.getJSONObject(i);
					String checkNull = objNoti.optString("null");
					if (checkNull.length() > 0) {
						return arrNotiParsed;
					}
				String reply = objNoti.optString("reply");
					int fromGroup = objNoti.getInt("fromGroup");
					MyNotification noti = null;
					if (fromGroup == 1) {
						String groupId = objNoti.optInt("g_id") + "";
						ArrayList<UserData> groupUserSelected = new ArrayList<UserData>();
						JSONArray arrUserInConversation = objNoti
								.getJSONArray("sender");
						String groupName = objNoti.getString("group_name");
						for (int j = 0; j < arrUserInConversation.length(); j++) {
							JSONObject objNotiUser = arrUserInConversation
									.getJSONObject(j);
							int idRe = objNotiUser.getInt("id");
							if (idRe != senderData.getID()) {
								String regIdRe = objNotiUser
										.getString("gcm_regid");
								String username = objNotiUser.getString("name");
								UserData receiverData = new UserData(idRe,
										regIdRe, username, "NoNeed");
								groupUserSelected.add(receiverData);
							}
						}
						noti = new MyNotification(groupName, reply, "Group",
								groupUserSelected, groupId);
					} else {
						JSONArray arrUserInConversation = objNoti
								.getJSONArray("sender");
						for (int j = 0; j < arrUserInConversation.length(); j++) {
							JSONObject objNotiUser = arrUserInConversation
									.getJSONObject(j);
							int idRe = objNotiUser.getInt("id");
							if (idRe != senderData.getID()) {
								String regIdRe = objNotiUser
										.getString("gcm_regid");
								String username = objNotiUser.getString("name");
								UserData receiverData = new UserData(idRe,
										regIdRe, username, "NoNeed");
								ArrayList<UserData> temp = new ArrayList<UserData>();
								temp.add(receiverData);
								noti = new MyNotification(username, reply,
										"One", temp, "NoNeed");
							}
						}
					}
					arrNotiParsed.add(noti);
				}
				return arrNotiParsed;
			} catch (JSONException e) {
				Log.e("AsyncTaskGetAllNoti parseJson haha",
						e.toString());
				return null;
			}
		}
	}
}
