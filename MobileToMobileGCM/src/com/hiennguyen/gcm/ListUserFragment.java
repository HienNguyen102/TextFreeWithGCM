package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ListUserFragment extends Fragment {

	Controller aController = null;
	ListUserAdapter mAdapter;

	GridView gridViewUsers;
	EditText etSearchFriend;
	ImageView btnBackMenu;
	UserData loggedUser;
	private ArrayList<UserData> arrUser = new ArrayList<UserData>();
	public static final String CHAT_TYPE = "CHAT_TYPE";
	//public static final String SENDER_DATA = "SENDER_DATA";
	public static final String RECEIVER_DATA = "RECEIVER_DATA";
	public static final String LIST_RECEIVER_DATA = "LIST_RECEIVER_DATA";
	SessionManager sm;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.grid_view_android_example,
				container, false);

		gridViewUsers = (GridView) rootView.findViewById(R.id.gridView1);
		registerForContextMenu(gridViewUsers);
		gridViewUsers.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		gridViewUsers.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			private int nr = 0;

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mAdapter.clearSelection();
				nr = 0;
				mAdapter.clearSelection();
				arrSelected.clear();
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				nr = 0;
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.contextual_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				nr = 0;
				mAdapter.clearSelection();
				Intent i = new Intent(getActivity(), ShowMessage.class);
				i.putExtra(CHAT_TYPE, "Group");
				ArrayList<UserData> temp = arrSelected;
				i.putExtra(LIST_RECEIVER_DATA, temp);
				startActivity(i);
				arrSelected.clear();
				mode.finish();
				return true;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				if (checked) {
					nr++;
					mAdapter.setNewSelection(position, checked);
					arrSelected.add(arrUser.get(position));
				} else {
					nr--;
					mAdapter.removeSelection(position);
					arrSelected.remove(arrUser.get(position));
				}
				mode.setTitle(nr + " "
						+ getResources().getString(R.string.grid_item_selected));

			}
		});
		etSearchFriend = (EditText) rootView.findViewById(R.id.etSearchFriend);

		// Get Global variable instance
		aController = (Controller) getActivity().getApplicationContext();

		// WebServer Request URL to get All registered devices
		String serverURL = Config.SERVER_URL + "userdata.php";
		sm=new SessionManager(getActivity());
		loggedUser = sm.getUserDetails();
		if (aController.isConnectingToInternet()) {
			new AsyncTaskGetAllUser().execute(serverURL, loggedUser.getName(),
					loggedUser.getID() + "");
		} else {
			aController.showAlertEnableWifi(getActivity());
		}

		gridViewUsers.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				UserData userSelected = (UserData) mAdapter.getItem(position);
				// View view_select = gridView.getChildAt(position);
				Intent i = new Intent(getActivity(), ShowMessage.class);
				i.putExtra(CHAT_TYPE, "One");

				i.putExtra(RECEIVER_DATA, userSelected);
				startActivity(i);
			}
		});
		etSearchFriend.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (etSearchFriend.getText().toString().trim().length() > 0) {
					applySearch(etSearchFriend.getText().toString().trim());
				} else {
					mAdapter.setarrUser(arrUser);
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		return rootView;
	}

	private void applySearch(String searchStr) {
		ArrayList<UserData> searchedUser = new ArrayList<UserData>();

		Log.e("ThongBao dd", searchStr);
		for (int i = 0; i < arrUser.size(); i++) {
			if (arrUser.get(i).getName().contains(searchStr)) {
				Log.e("Thoa", arrUser.get(i).getName());
				searchedUser.add(arrUser.get(i));
			}
		}

		mAdapter.setarrUser(searchedUser);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, v.getId(), 0, R.string.add_to_group);
	}

	static ArrayList<UserData> arrSelected = new ArrayList<UserData>();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	// Class with extends AsyncTask class
	public class AsyncTaskGetAllUser extends
			AsyncTask<String, Void, JSONObject> {
		ProgressDialog pD = new ProgressDialog(getActivity());

		protected void onPreExecute() {
			pD.setMessage(getResources().getString(R.string.wait_sending));
			pD.show();
		}

		protected JSONObject doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// args[1] -> usernameLogged
			params.add(new BasicNameValuePair("usernameLogged", args[1]));
			params.add(new BasicNameValuePair("IdLogged", args[2]));
			JSONParser jsonPaster = new JSONParser();
			// args[0] -> server url
			JSONObject jsonDataOfAllUser = null;
			try {
				jsonDataOfAllUser = jsonPaster.performPostCall(args[0], params,
						true);
				return jsonDataOfAllUser;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			pD.dismiss();
			if (result != null) {

				arrUser = parseJson(result);
				if (arrUser != null) {
					mAdapter = new ListUserAdapter(getActivity(), arrUser);
					gridViewUsers.setAdapter(mAdapter);
				}
			}else {
				Toast.makeText(getActivity(), getResources().getString(R.string.change_pass_err), Toast.LENGTH_LONG).show();
			}
		}

		public ArrayList<UserData> parseJson(JSONObject pResult) {
			ArrayList<UserData> arrUserParsed = new ArrayList<UserData>();
			try {

				// Returns the value mapped by name if it exists and is a
				// JSONArray.
				JSONArray arrAllUserInJson = pResult.optJSONArray("Friends");
				// Process each JSON Node
				for (int i = 0; i < arrAllUserInJson.length(); i++) {
					// Get Object for each JSON node.
					JSONObject user = arrAllUserInJson.getJSONObject(i);
					// Fetch node values
					String Name = user.optString("name").toString();
					String _regId = user.optString("regid").toString();
					int idUser = user.optInt("id");
					if (idUser == 0) {
						return arrUserParsed;
					}
					UserData userdata = new UserData();
					userdata.setID(idUser);
					userdata.setName(Name);
					userdata.set_regId(_regId);

					arrUserParsed.add(userdata);
				}
				return arrUserParsed;

			} catch (JSONException e) {
				Log.e("AsyncTaskGetAllUser-onPostExecute", e.toString());
				return null;
			}
		}
	}
}