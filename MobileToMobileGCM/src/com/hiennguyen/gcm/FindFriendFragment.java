package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.hiennguyen.gcm.FindFriendAdapter.ListenerUpdateStatus;

public class FindFriendFragment extends Fragment implements
		ListenerUpdateStatus {
	Controller ctlCheckNetState;
	ListView lvUserFound;
	EditText etSearchUsername;
	String searchUsername, idOfLoggedUser, idOfSelectedFriend;
	private static final String FIND_USER_URL = Config.SERVER_URL
			+ "search_user.php";
	public static final String ADD_UNFRIEND_URL = Config.SERVER_URL
			+ "add_and_unfriend.php";
	SessionManager sm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm = new SessionManager(getActivity());
		idOfLoggedUser = sm.getUserDetails().getID() + "";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_find_friend, container,
				false);
		ctlCheckNetState = (Controller) getActivity().getApplicationContext();
		lvUserFound = (ListView) view.findViewById(R.id.lvUserFound);
		registerForContextMenu(lvUserFound);
		etSearchUsername = (EditText) view.findViewById(R.id.etSearchUsername);
		ImageButton ibSearch = (ImageButton) view.findViewById(R.id.ibSearch);
		ibSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!ctlCheckNetState.isConnectingToInternet()) {
					ctlCheckNetState.showAlertEnableWifi(getActivity());
					return;
				}
				searchUsername = etSearchUsername.getText().toString().trim();
				new AsyncTaskFindUser().execute();
			}
		});
		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(
				getActivity().getResources().getString(R.string.ask_unfriend))
				.setHeaderIcon(R.drawable.indicator_input_error);
		menu.add(0, v.getId(), 0,
				getActivity().getResources().getString(R.string.unfriend));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int index = info.position;
		idOfSelectedFriend = arrForAdapter.get(index).getID() + "";
		new AsyncTaskUnfriend().execute();
		return super.onContextItemSelected(item);
	}

	ArrayList<UserData> arrForAdapter = new ArrayList<UserData>();
	FindFriendAdapter adapter;

	public class AsyncTaskFindUser extends AsyncTask<Void, Void, String> {
		ProgressDialog pDialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(getResources().getString(R.string.wait_sending));
			pDialog.show();
		}

		@Override
		protected String doInBackground(Void... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("keyword", searchUsername));
			params.add(new BasicNameValuePair("id_of_logged_user",
					idOfLoggedUser));
			JSONParser jsonPaster = new JSONParser();
			JSONObject json = null;
			try {
				json = jsonPaster.performPostCall(FIND_USER_URL, params, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (json != null) {
				arrForAdapter = parseJson(json);
				return "succ";
			} else {
				return getResources().getString(R.string.change_pass_err);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();
			if (result.equals("succ")) {

				adapter = new FindFriendAdapter(getActivity(), arrForAdapter,
						idOfLoggedUser, FindFriendFragment.this);
				lvUserFound.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
			}

		}

		public ArrayList<UserData> parseJson(JSONObject pResult) {
			ArrayList<UserData> arrUserParsed = new ArrayList<UserData>();
			try {
				JSONArray arrAllUserInJson = pResult.optJSONArray("Users");
				for (int i = 0; i < arrAllUserInJson.length(); i++) {
					JSONObject user = arrAllUserInJson.getJSONObject(i);
					String name = user.optString("name").toString();
					String regId = user.optString("regid").toString();
					int idUser = user.optInt("id");
					if (name.length() == 0 && regId.length() == 0) {
						return arrUserParsed;
					}
					int isFriend = user.optInt("is_friend");
					UserData userdata = new UserData();
					userdata.setID(idUser);
					userdata.setName(name);
					userdata.set_regId(regId);
					if (isFriend == 1) {
						userdata.setFriend(true);
					} else {
						userdata.setFriend(false);
					}
					arrUserParsed.add(userdata);
				}
				return arrUserParsed;

			} catch (JSONException e) {
				Log.e("AsyncTaskGetAllUser-onPostExecute", e.toString());
				return null;
			}
		}
	}

	public class AsyncTaskUnfriend extends AsyncTask<Void, Void, String> {
		ProgressDialog pDialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(getActivity().getResources().getString(
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
			params.add(new BasicNameValuePair("unfriend", "yes"));
			JSONParser jsonPaster = new JSONParser();
			try {
				JSONObject json = jsonPaster.performPostCall(ADD_UNFRIEND_URL,
						params, true);
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
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.unfriend_succ), Toast.LENGTH_LONG)
						.show();
				searchUsername = etSearchUsername.getText().toString().trim();
				new AsyncTaskFindUser().execute();
			} else {
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.change_pass_err), Toast.LENGTH_LONG)
						.show();
			}
			pDialog.dismiss();
		}

	}

	@Override
	public void updateStatus(String nameOfFriendNeedToAdd) {
		searchUsername = etSearchUsername.getText().toString().trim();
		new AsyncTaskFindUser().execute();
		Toast.makeText(
				getActivity(),
				getResources().getString(R.string.add_friend_success,
						nameOfFriendNeedToAdd), Toast.LENGTH_LONG).show();
	}
}
