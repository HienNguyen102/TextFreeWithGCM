package com.hiennguyen.gcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Controller extends Application {
	// Register this account with the server.
	void register(final Context context, String name, final String regId,
			final String password) {

		// Server url to post gcm registration data
		String serverUrl = Config.SERVER_URL + "register.php";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("regId", regId));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("password", password));

		// Once GCM returns a registration id, we need to register on our server
		// As the server might be down, we will retry it a couple times.

		try {
			// Post registration values to web server
			JSONParser jsonParser = new JSONParser();
			jsonParser.performPostCall(serverUrl, params, false);
			Intent i1 = new Intent(context, LogRegActivity.class);
			i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i1);
		} catch (Exception e) {
			Log.e("Controller-register", "Failed to register" + e);
		}
	}

	public boolean isConnectingToInternet() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	// Function to display simple Alert Dialog
	public void showAlertEnableWifi(Context context) {
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);

		adBuilder.setTitle(context.getResources().getString(
				R.string.net_con_err));

		adBuilder.setMessage(context.getResources().getString(
				R.string.net_con_err_suggest));

		adBuilder.setIcon(R.drawable.ic_alert_fail);

		adBuilder.setPositiveButton(getResources()
				.getString(R.string.ok_dialog),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

						Intent intentEnableWifi = new Intent(
								android.provider.Settings.ACTION_WIFI_SETTINGS);
						intentEnableWifi
								.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intentEnableWifi);
					}
				});
		adBuilder.setNegativeButton(
				getResources().getString(R.string.cancel_dialog), null);
		AlertDialog dialog = adBuilder.create();
		dialog.show();
	}

	public void showError(Context context, String title, String message) {
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);

		adBuilder.setTitle(title);

		adBuilder.setMessage(message);

		adBuilder.setIcon(R.drawable.ic_alert_fail);

		adBuilder.setNegativeButton(
				getResources().getString(R.string.cancel_dialog), null);
		AlertDialog dialog = adBuilder.create();
		dialog.show();
	}

	public void showAppSendSMSOrEnableWifi(Context context,
			final String textToSMSApp) {
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(context);

		adBuilder.setTitle(context.getResources().getString(
				R.string.net_con_err));

		adBuilder.setMessage(context.getResources().getString(
				R.string.net_con_err_suggest2));

		adBuilder.setIcon(R.drawable.ic_alert_fail);

		adBuilder.setNegativeButton(
				getResources().getString(R.string.cancel_dialog), null);
		adBuilder.setPositiveButton(
				getResources().getString(R.string.enable_wifi),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intentEnableWifi = new Intent(
								android.provider.Settings.ACTION_WIFI_SETTINGS);
						intentEnableWifi
								.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intentEnableWifi);
					}
				});
		adBuilder.setNeutralButton(
				getResources().getString(R.string.enable_sms_app),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent smsIntent = new Intent(Intent.ACTION_VIEW);

						smsIntent.putExtra("sms_body", textToSMSApp);
						smsIntent.putExtra("address", ""); // "0123456789");
						smsIntent.setType("vnd.android-dir/mms-sms");
						smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(smsIntent);
					}
				});
		AlertDialog dialog = adBuilder.create();
		dialog.show();
	}

	public void showHelpDialog(Context context) {
		new AlertDialog.Builder(context)
				.setCancelable(false)
				.setTitle(getResources().getString(R.string.reg_err))
				.setMessage(
						getResources().getString(
								R.string.reg_err_gcm_registered))
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(
										getApplicationContext(),
										RetrieveAccountActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}).setNegativeButton(android.R.string.no, null)
				.setIcon(android.R.drawable.ic_dialog_alert).show();
	}
}
