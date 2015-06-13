package com.hiennguyen.gcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterActivity extends Activity {
	static final String TAG = "RegisterActivity";
	// ---sample
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;

	String regid;
	// ---

	// UI elements
	EditText etUsername, etPassword, etPasswordCon;
	Button btnRegister;
	static String sUsername, sPassword;
	ProgressDialog pd;
	Controller aController;

	private static final String CHECK_REGISTER_URL = Config.SERVER_URL
			+ "login.php";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_REGISTRATION_MESSAGE_ACTION));
		// Intialize Database
		aController = (Controller) getApplicationContext();
		setContentView(R.layout.activity_register);

		context = getApplicationContext();

		final Controller aController = (Controller) getApplicationContext();
		pd = new ProgressDialog(RegisterActivity.this);
		etUsername = (EditText) findViewById(R.id.txtName);
		etPassword = (EditText) findViewById(R.id.txtPassword);
		etPasswordCon = (EditText) findViewById(R.id.txtPasswordRetype);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Check internet connection
				if (!aController.isConnectingToInternet()) {
					aController.showAlertEnableWifi(RegisterActivity.this);
					return;
				}
				if (!checkForm()) {
					return;
				} else {
					new AsyncTaskCheckUserName().execute();
					pd = ProgressDialog.show(RegisterActivity.this,
							getResources().getString(R.string.wait_reg),
							getResources().getString(R.string.wait_network),
							true);
					// Execute some code after 3 seconds have passed
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							pd.dismiss();
							if (hasError) {
								Toast.makeText(
										RegisterActivity.this,
										getResources().getString(
												R.string.change_pass_err),
										Toast.LENGTH_LONG).show();
								return;
							}
							if (usernameNotExists) {
								// Check device for Play Services APK. If check
								// succeeds, proceed with
								// GCM registration.
								if (checkPlayServices()) {
									gcm = GoogleCloudMessaging
											.getInstance(RegisterActivity.this);
									regid = getRegistrationId(context);

									Toast.makeText(RegisterActivity.this,
											resultFromAsyncTaskCheckUserName,
											Toast.LENGTH_SHORT).show();
									if (regid.isEmpty()) {
										registerInBackground();
									} else {
										aController
												.showHelpDialog(RegisterActivity.this);
									}
								} else {
									Log.e(TAG,
											"No valid Google Play Services APK found.");
								}
							} else {
								Toast.makeText(RegisterActivity.this,
										resultFromAsyncTaskCheckUserName,
										Toast.LENGTH_SHORT).show();
							}
						}
					}, 3000);

				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();
	}

	public void showErrorOnForm(String err) {
		aController.showError(RegisterActivity.this,
				getResources().getString(R.string.reg_err), err);
	}

	public boolean checkForm() {
		sUsername = etUsername.getText().toString().trim();
		sPassword = etPassword.getText().toString().trim();
		if (sUsername.length() < 1 || sPassword.length() < 1) {
			showErrorOnForm(getResources().getString(R.string.reg_err_ask));
			return false;
		}
		String passwordCon = etPasswordCon.getText().toString().trim();
		if (!sPassword.equals(passwordCon)) {
			showErrorOnForm(getResources().getString(R.string.pass_wrong));

			etPassword.setText("");
			etPasswordCon.setText("");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.e(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	// @return Application's {@code SharedPreferences}.
	private SharedPreferences getGcmPreferences(Context context) {
		return getSharedPreferences(RegisterActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.e(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.e(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.e(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(Config.GOOGLE_SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend(regid);

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.e(TAG + " onPostExecute here", msg + "\n");
			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend(String regId) {
		// Your implementation here.
		aController.register(getApplicationContext(),
				RegisterActivity.sUsername, regId, RegisterActivity.sPassword);
		notiForSuccessReg();
		finish();
	}

	boolean usernameNotExists = false;
	boolean hasError = false;
	String resultFromAsyncTaskCheckUserName = "";

	class AsyncTaskCheckUserName extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... args) {

			try {

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", sUsername));
				params.add(new BasicNameValuePair("check_username_exist",
						"true"));
				JSONParser jsonPaster = new JSONParser();
				JSONObject json = jsonPaster.performPostCall(
						CHECK_REGISTER_URL, params, true);

				if (json.getInt("success") == 0) {
					usernameNotExists = true;
					resultFromAsyncTaskCheckUserName = getResources()
							.getString(R.string.reg_not_exist_username);
					return "success";
				} else {
					resultFromAsyncTaskCheckUserName = getResources()
							.getString(R.string.reg_err_exist_username);
					return getResources().getString(
							R.string.reg_err_exist_username);
				}
			} catch (Exception e) {
				hasError = true;
				return "JSONException";
			}
		}
	}

	public void notiForSuccessReg() {
		Intent intent = new Intent(Config.DISPLAY_REGISTRATION_MESSAGE_ACTION);
		sendBroadcast(intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void createNotification() {
		PendingIntent notifyPIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, new Intent(), 0);
		Notification noti = new Notification.Builder(this)
				.setContentTitle(getResources().getString(R.string.reg_succ))
				.setContentIntent(notifyPIntent)
				.setSmallIcon(R.drawable.ic_noti).build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);

	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			createNotification();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(this, LogRegActivity.class);
			startActivity(intent);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mHandleMessageReceiver);
	}
}
