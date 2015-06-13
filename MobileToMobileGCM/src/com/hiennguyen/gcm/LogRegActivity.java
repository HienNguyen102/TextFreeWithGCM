package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LogRegActivity extends Activity implements OnClickListener {

	private static final String LOGIN_URL = Config.SERVER_URL + "login.php";
//	public static final String REMEMBER_LOGIN="Remember";
//	public static final String SENDER_DATA="SenderData";
	// public static final String REMEMBER_USERNAME="username_remember";
	// public static final String REMEMBER_PASSWORD="password_remember";
	Controller ctlCheckNetState;

	private Button btnLogin, btnRegister;
	private EditText etUsername, etPassword;
	private CheckBox ckRemember;
	SessionManager sm;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_log_reg);
		sm=new SessionManager(this);
		etUsername = (EditText) findViewById(R.id.etUsername);
		etPassword = (EditText) findViewById(R.id.etPassword);
		ckRemember = (CheckBox) findViewById(R.id.ckRemember);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnRegister = (Button) findViewById(R.id.btnRegister);

		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		// Get Global Controller Class object (see application tag in
		// androidManifest.xml)
		ctlCheckNetState = (Controller) getApplicationContext();
		if (!ctlCheckNetState.isConnectingToInternet()) {
			ctlCheckNetState.showAlertEnableWifi(LogRegActivity.this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		if (!ctlCheckNetState.isConnectingToInternet()) {
			ctlCheckNetState.showAlertEnableWifi(LogRegActivity.this);
			return;
		}
		switch (v.getId()) {
		case R.id.btnLogin:
			new AsyncTaskLogin().execute();
			break;
		case R.id.btnRegister:
			Intent intent = new Intent(LogRegActivity.this,
					RegisterActivity.class);
			startActivity(intent);
			finish();
			break;
		}
	}


	class AsyncTaskLogin extends AsyncTask<String, String, String> {
		ProgressDialog pDialog = new ProgressDialog(LogRegActivity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage(getResources().getString(R.string.wait_login));
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			String username = etUsername.getText().toString().trim();
			String password = etPassword.getText().toString().trim();
			try {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));
				params.add(new BasicNameValuePair("password", password));
				JSONParser jsonPaster = new JSONParser();
				JSONObject json = jsonPaster.performPostCall(LOGIN_URL, params,
						true);

				if (json.getInt("success") == 1) {
					sm.createLoginSession(username, password,ckRemember.isChecked(),json.getInt("id"),json.getString("gcm_regid"));
					Intent i = new Intent(getApplicationContext(),
							HomeActivity.class);
					startActivity(i);
					pDialog.dismiss();
					finish();
					return getResources().getString(R.string.login_suc);
				} else {
					return getResources().getString(R.string.login_fai);

				}
			} catch (Exception e) {
				Log.e("AsyncTaskLogin doInBackground", e.toString());
				return getResources().getString(R.string.change_pass_err);
			}
		}

		protected void onPostExecute(String loginResult) {
			pDialog.dismiss();
			Toast.makeText(LogRegActivity.this, loginResult, Toast.LENGTH_SHORT)
					.show();
		}
	}

}
