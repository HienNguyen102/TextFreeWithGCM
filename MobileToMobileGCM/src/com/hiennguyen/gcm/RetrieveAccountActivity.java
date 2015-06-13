package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RetrieveAccountActivity extends Activity {
	private static final String RETRIEVE_URL = Config.SERVER_URL + "retrieve_account.php";
	EditText etNameRetrieve;
	Button btnRetrieve;
	TextView tvAccountRetrieve;
	Controller ctlCheckNetState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_retrieve_account);
		etNameRetrieve = (EditText) findViewById(R.id.etNameRetrieve);
		btnRetrieve = (Button) findViewById(R.id.btnRetrieve);
		tvAccountRetrieve = (TextView) findViewById(R.id.tvAccountRetrieve);
		ctlCheckNetState = (Controller) getApplicationContext();
		btnRetrieve.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!ctlCheckNetState.isConnectingToInternet()) {
					ctlCheckNetState.showAlertEnableWifi(RetrieveAccountActivity.this);
					return;
				}
				new AsyncTaskRetrieveAccount().execute();
			}
		});
	}

	public SharedPreferences getRegisterActivityPref(){
		return getSharedPreferences(RegisterActivity.class.getSimpleName(),Context.MODE_PRIVATE);
	}
	class AsyncTaskRetrieveAccount extends AsyncTask<Void, String, String> {
		ProgressDialog pDialog = new ProgressDialog(RetrieveAccountActivity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(getResources().getString(R.string.wait_sending));
			pDialog.show();
		}

		@Override
		protected String doInBackground(Void... args) {
			String username = etNameRetrieve.getText().toString().trim();
			try {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				
				SharedPreferences prefs = getRegisterActivityPref();
				String gcmId=prefs.getString(RegisterActivity.PROPERTY_REG_ID, "abc");
				
				params.add(new BasicNameValuePair("gcm_regid", gcmId));
				JSONParser jsonPaster = new JSONParser();
				JSONObject json = jsonPaster.performPostCall(RETRIEVE_URL, params,true);
				String password=json.getString("password");
				if (!password.equals("")) {
					return getResources().getString(R.string.retreive_info,username,password);
				} else {
					return getResources().getString(R.string.retreive_fai);

				}
			} catch (Exception e) {
				Log.e("AsyncTaskRetrieveAccount",e.toString());
				return getResources().getString(R.string.change_pass_err);
			}
		}

		protected void onPostExecute(String retreiveResult) {
			pDialog.dismiss();
			tvAccountRetrieve.setText(retreiveResult);
		}
	}
}
