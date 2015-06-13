package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends Activity {
	private static final String CHANGE_PASS_URL = Config.SERVER_URL
			+ "change_account_password.php";
	Controller ctlCheckNetState;
	EditText etOldPass, etNewPass, etNewPassConfirm;
	String oldPassEntered, newPass, newPassConfrim;
	Button btnChangePass;
	SessionManager sm;
	UserData loggedUserInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		sm=new SessionManager(getApplicationContext());
		
		loggedUserInfo=sm.getUserDetails();
		
		etOldPass = (EditText) findViewById(R.id.etOldPass);
		etNewPass = (EditText) findViewById(R.id.etNewPass);
		etNewPassConfirm = (EditText) findViewById(R.id.etNewPassConfirm);
		btnChangePass = (Button) findViewById(R.id.btnChangePass);
		btnChangePass.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				oldPassEntered = etOldPass.getText().toString().trim();
				newPass = etNewPass.getText().toString().trim();
				newPassConfrim = etNewPassConfirm.getText().toString().trim();
				if (!oldPassIsAppropriate()) {
					showError(R.string.pass_wrong);
					return;
				}
				if (!newPass.equals(newPassConfrim)) {
					showError(R.string.pass_wrong);
					etNewPass.setText("");
					etNewPassConfirm.setText("");
					return;
				}
				if (etNewPass.getText().length() < 1) {
					showError(R.string.change_pass_err_ask);
					return;
				} else {
					new AsyncTaskChangePassword().execute();
				}
			}
		});
	}

	public void showError(int resourcesId) {
		Toast.makeText(ChangePasswordActivity.this,
				getResources().getString(resourcesId), Toast.LENGTH_LONG)
				.show();
	}


	public boolean oldPassIsAppropriate() {
		
		if (loggedUserInfo.get_password().equals(oldPassEntered)) {
			return true;
		} else {
			return false;
		}
	}

	public class AsyncTaskChangePassword extends AsyncTask<Void, Void, String> {
		ProgressDialog pDialog = new ProgressDialog(ChangePasswordActivity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(getResources().getString(R.string.wait_sending));
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(Void... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", loggedUserInfo.getName()));
			params.add(new BasicNameValuePair("password", newPass));
			JSONParser jsonPaster = new JSONParser();
			try {
				JSONObject json = jsonPaster.performPostCall(CHANGE_PASS_URL,params, true);
				if (json.getInt("success") == 1) {
					sm.changePassword(newPass);
					pDialog.dismiss();
					finish();
					return getResources().getString(R.string.change_pass_succ);
				} else {
					return getResources().getString(R.string.change_pass_err);
				}
			} catch (Exception e) {
				Log.e("AsyncTaskChangePassword doInBackground", e.toString());
				return getResources().getString(R.string.change_pass_err);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			pDialog.dismiss();
			Toast.makeText(ChangePasswordActivity.this, result,
					Toast.LENGTH_LONG).show();
		}
	}
}
